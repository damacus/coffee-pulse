package com.damacus.coffeepulse.domain

import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

enum class TimerCue {
    BLOOM_COMPLETE,
    POUR_COMPLETE,
    WAIT_COMPLETE,
}

data class TimerTransition(
    val session: TimerSession,
    val cue: TimerCue? = null,
    val countdownSecondCue: Int? = null,
)

object TimerEngine {
    fun idle(config: BrewConfig = BrewConfig()): TimerSession {
        return TimerSession(
            phase = TimerPhase.IDLE,
            phaseRemainingSeconds = config.bloomSeconds,
            progress = 1f,
            config = config,
        )
    }

    fun start(config: BrewConfig, nowMillis: Long): TimerSession {
        return TimerSession(
            phase = TimerPhase.BLOOM,
            startedAtMillis = nowMillis,
            pausedAccumulatedMillis = 0L,
            pausedStartedAtMillis = null,
            lastCueElapsedMillis = -1L,
            lastCountdownSecond = -1,
            elapsedSeconds = 0,
            phaseRemainingSeconds = config.bloomSeconds,
            progress = 1f,
            isRunning = true,
            config = config,
        )
    }

    fun resume(session: TimerSession): TimerSession {
        return resume(session, System.currentTimeMillis())
    }

    fun resume(session: TimerSession, nowMillis: Long): TimerSession {
        if (session.phase == TimerPhase.IDLE) {
            return start(session.config, nowMillis)
        }
        if (session.isRunning) return snapshot(session, nowMillis).session
        val pausedStartedAt = session.pausedStartedAtMillis
        val pausedDuration = if (pausedStartedAt == null) {
            0L
        } else {
            (nowMillis - pausedStartedAt).coerceAtLeast(0L)
        }
        return session.copy(
            isRunning = true,
            pausedAccumulatedMillis = session.pausedAccumulatedMillis + pausedDuration,
            pausedStartedAtMillis = null,
        )
    }

    fun pause(session: TimerSession): TimerSession {
        return pause(session, System.currentTimeMillis())
    }

    fun pause(session: TimerSession, nowMillis: Long): TimerSession {
        if (!session.isRunning || session.phase == TimerPhase.IDLE) return session
        return snapshot(session, nowMillis).session.copy(
            isRunning = false,
            pausedStartedAtMillis = nowMillis,
        )
    }

    fun reset(session: TimerSession): TimerSession {
        return idle(session.config)
    }

    fun snapshot(session: TimerSession, nowMillis: Long): TimerTransition {
        if (!session.isRunning || session.phase == TimerPhase.IDLE) {
            return TimerTransition(session)
        }

        val elapsedMillis = activeElapsedMillis(session, nowMillis)
        val resolved = resolve(session.config, elapsedMillis)
        val boundaryCue = resolved.boundaryElapsedMillis?.let { boundaryElapsed ->
            if (boundaryElapsed > session.lastCueElapsedMillis) {
                resolved.cue
            } else {
                null
            }
        }

        // Countdown chime (3, 2, 1) seconds before boundary change
        val remaining = resolved.phaseRemainingSeconds
        val countdownCue = if (remaining in 1..3 && remaining != session.lastCountdownSecond) {
            remaining
        } else {
            null
        }

        val nextSession = session.copy(
            phase = resolved.phase,
            elapsedSeconds = (elapsedMillis / MILLIS_PER_SECOND).toInt(),
            phaseRemainingSeconds = resolved.phaseRemainingSeconds,
            progress = resolved.progress,
            lastCueElapsedMillis = if (boundaryCue == null) {
                session.lastCueElapsedMillis
            } else {
                resolved.boundaryElapsedMillis
            },
            lastCountdownSecond = if (countdownCue != null) {
                countdownCue
            } else if (remaining > 3 || boundaryCue != null) {
                -1
            } else {
                session.lastCountdownSecond
            },
        )
        return TimerTransition(nextSession, boundaryCue, countdownCue)
    }

    fun tick(session: TimerSession): TimerTransition {
        val startedAt = session.startedAtMillis ?: return TimerTransition(session)
        val nowMillis = startedAt +
            session.pausedAccumulatedMillis +
            (session.elapsedSeconds + 1) * MILLIS_PER_SECOND
        return snapshot(session, nowMillis)
    }

    fun millisUntilNextUpdate(session: TimerSession, nowMillis: Long): Long {
        if (!session.isRunning || session.phase == TimerPhase.IDLE) return MILLIS_PER_SECOND
        val elapsedMillis = activeElapsedMillis(session, nowMillis)
        val resolved = resolve(session.config, elapsedMillis)
        val untilNextSecond = MILLIS_PER_SECOND - (elapsedMillis % MILLIS_PER_SECOND)
        return min(untilNextSecond, resolved.phaseRemainingMillis)
            .coerceIn(MIN_UPDATE_MILLIS, MILLIS_PER_SECOND)
    }

    private fun activeElapsedMillis(session: TimerSession, nowMillis: Long): Long {
        val startedAt = session.startedAtMillis ?: return 0L
        val effectiveNow = if (session.isRunning) {
            nowMillis
        } else {
            session.pausedStartedAtMillis ?: nowMillis
        }
        return (effectiveNow - startedAt - session.pausedAccumulatedMillis).coerceAtLeast(0L)
    }

    private fun resolve(config: BrewConfig, elapsedMillis: Long): ResolvedTimer {
        val bloomMillis = max(config.bloomSeconds, 1) * MILLIS_PER_SECOND
        val pulseMillis = max(config.pulseIntervalSeconds, 1) * MILLIS_PER_SECOND
        if (elapsedMillis < bloomMillis) {
            return resolvedPhase(
                phase = TimerPhase.BLOOM,
                phaseStartElapsedMillis = 0L,
                phaseDurationMillis = bloomMillis,
                elapsedMillis = elapsedMillis,
                cue = null,
            )
        }

        val afterBloom = elapsedMillis - bloomMillis
        val intervalIndex = afterBloom / pulseMillis
        val phaseStartElapsedMillis = bloomMillis + intervalIndex * pulseMillis
        val phase = if (intervalIndex % 2L == 0L) TimerPhase.POUR else TimerPhase.WAIT
        val cue = when (phase) {
            TimerPhase.POUR -> if (phaseStartElapsedMillis == bloomMillis) {
                TimerCue.BLOOM_COMPLETE
            } else {
                TimerCue.WAIT_COMPLETE
            }
            TimerPhase.WAIT -> TimerCue.POUR_COMPLETE
            TimerPhase.BLOOM, TimerPhase.IDLE -> null
        }
        return resolvedPhase(
            phase = phase,
            phaseStartElapsedMillis = phaseStartElapsedMillis,
            phaseDurationMillis = pulseMillis,
            elapsedMillis = elapsedMillis,
            cue = cue,
        )
    }

    private fun resolvedPhase(
        phase: TimerPhase,
        phaseStartElapsedMillis: Long,
        phaseDurationMillis: Long,
        elapsedMillis: Long,
        cue: TimerCue?,
    ): ResolvedTimer {
        val phaseElapsedMillis = (elapsedMillis - phaseStartElapsedMillis).coerceAtLeast(0L)
        val phaseRemainingMillis = (phaseDurationMillis - phaseElapsedMillis).coerceIn(0L, phaseDurationMillis)
        val progress = if (phaseDurationMillis <= 0L) {
            1f
        } else {
            (phaseRemainingMillis.toFloat() / phaseDurationMillis.toFloat()).coerceIn(0f, 1f)
        }
        val phaseRemainingSeconds = ceil(phaseRemainingMillis / MILLIS_PER_SECOND.toDouble())
            .toInt()
            .coerceAtLeast(0)
        val boundaryElapsedMillis = phaseStartElapsedMillis.takeIf { it > 0L }
        return ResolvedTimer(
            phase = phase,
            phaseRemainingSeconds = phaseRemainingSeconds,
            phaseRemainingMillis = phaseRemainingMillis,
            progress = progress,
            boundaryElapsedMillis = boundaryElapsedMillis,
            cue = cue,
        )
    }

    private data class ResolvedTimer(
        val phase: TimerPhase,
        val phaseRemainingSeconds: Int,
        val phaseRemainingMillis: Long,
        val progress: Float,
        val boundaryElapsedMillis: Long?,
        val cue: TimerCue?,
    )

    private const val MILLIS_PER_SECOND = 1_000L
    private const val MIN_UPDATE_MILLIS = 50L
}
