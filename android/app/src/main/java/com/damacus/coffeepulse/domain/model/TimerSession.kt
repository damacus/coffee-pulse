package com.damacus.coffeepulse.domain.model

data class TimerSession(
    val phase: TimerPhase = TimerPhase.IDLE,
    val startedAtMillis: Long? = null,
    val pausedAccumulatedMillis: Long = 0L,
    val pausedStartedAtMillis: Long? = null,
    val lastCueElapsedMillis: Long = -1L,
    val lastCountdownSecond: Int = -1,
    val elapsedSeconds: Int = 0,
    val phaseRemainingSeconds: Int = 0,
    val progress: Float = 1f,
    val isRunning: Boolean = false,
    val config: BrewConfig = BrewConfig(),
) {
    val isIdle: Boolean = phase == TimerPhase.IDLE

    val phaseDurationSeconds: Int
        get() = if (phase == TimerPhase.BLOOM) {
            config.bloomSeconds
        } else {
            config.pulseIntervalSeconds
        }

    val pulseIndex: Int
        get() = if (phase == TimerPhase.BLOOM || phase == TimerPhase.IDLE) {
            0
        } else {
            val bloomSeconds = config.bloomSeconds.coerceAtLeast(1)
            val pulseSeconds = config.pulseIntervalSeconds.coerceAtLeast(1)
            val afterBloomSeconds = (elapsedSeconds - bloomSeconds).coerceAtLeast(0)
            (afterBloomSeconds / pulseSeconds) + 1
        }
}
