package com.damacus.coffeepulse.domain

import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.TimerPhase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimerEngineTest {
    private val config = BrewConfig(
        bloomSeconds = 3,
        pulseIntervalSeconds = 2,
        coffeeGrams = 15.0,
        waterRatio = 15.5,
        themeId = "instrument",
        soundEnabled = true,
        hapticsEnabled = true,
    )

    @Test
    fun startMovesIdleSessionIntoBloom() {
        val session = TimerEngine.start(config, nowMillis = 1_000L)

        assertEquals(TimerPhase.BLOOM, session.phase)
        assertEquals(3, session.phaseRemainingSeconds)
        assertEquals(0, session.elapsedSeconds)
        assertTrue(session.isRunning)
    }

    @Test
    fun bloomMovesToPourAfterConfiguredDuration() {
        var transition = TimerTransition(TimerEngine.start(config, nowMillis = 1_000L))

        repeat(3) {
            transition = TimerEngine.tick(transition.session)
        }

        assertEquals(TimerPhase.POUR, transition.session.phase)
        assertEquals(2, transition.session.phaseRemainingSeconds)
        assertEquals(3, transition.session.elapsedSeconds)
        assertEquals(TimerCue.BLOOM_COMPLETE, transition.cue)
    }

    @Test
    fun pourAndWaitAlternateUntilUserStops() {
        var transition = TimerTransition(TimerEngine.start(config, nowMillis = 1_000L))
        repeat(3) { transition = TimerEngine.tick(transition.session) }
        repeat(2) { transition = TimerEngine.tick(transition.session) }

        assertEquals(TimerPhase.WAIT, transition.session.phase)
        assertEquals(TimerCue.POUR_COMPLETE, transition.cue)

        repeat(2) { transition = TimerEngine.tick(transition.session) }

        assertEquals(TimerPhase.POUR, transition.session.phase)
        assertEquals(TimerCue.WAIT_COMPLETE, transition.cue)
    }

    @Test
    fun pausePreventsTickFromAdvancingTime() {
        val paused = TimerEngine.pause(TimerEngine.start(config, nowMillis = 1_000L))
        val transition = TimerEngine.tick(paused)

        assertEquals(paused, transition.session)
        assertEquals(null, transition.cue)
        assertFalse(transition.session.isRunning)
    }

    @Test
    fun resetReturnsToIdleWithConfiguredBloomDuration() {
        val running = TimerEngine.tick(TimerEngine.start(config, nowMillis = 1_000L)).session
        val reset = TimerEngine.reset(running)

        assertEquals(TimerPhase.IDLE, reset.phase)
        assertEquals(config.bloomSeconds, reset.phaseRemainingSeconds)
        assertEquals(0, reset.elapsedSeconds)
        assertFalse(reset.isRunning)
    }

    @Test
    fun snapshotSwitchesFromBloomToPourAtExactBoundary() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)

        val transition = TimerEngine.snapshot(started, nowMillis = 4_000L)

        assertEquals(TimerPhase.POUR, transition.session.phase)
        assertEquals(2, transition.session.phaseRemainingSeconds)
        assertEquals(3, transition.session.elapsedSeconds)
        assertEquals(1f, transition.session.progress)
        assertEquals(TimerCue.BLOOM_COMPLETE, transition.cue)
    }

    @Test
    fun snapshotSwitchesFromPourToWaitWithoutOneSecondLag() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)
        val pour = TimerEngine.snapshot(started, nowMillis = 4_000L).session

        val transition = TimerEngine.snapshot(pour, nowMillis = 6_000L)

        assertEquals(TimerPhase.WAIT, transition.session.phase)
        assertEquals(2, transition.session.phaseRemainingSeconds)
        assertEquals(5, transition.session.elapsedSeconds)
        assertEquals(1f, transition.session.progress)
        assertEquals(TimerCue.POUR_COMPLETE, transition.cue)
    }

    @Test
    fun snapshotSwitchesFromWaitToPourWithoutOneSecondLag() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)
        val wait = TimerEngine.snapshot(
            TimerEngine.snapshot(started, nowMillis = 4_000L).session,
            nowMillis = 6_000L,
        ).session

        val transition = TimerEngine.snapshot(wait, nowMillis = 8_000L)

        assertEquals(TimerPhase.POUR, transition.session.phase)
        assertEquals(2, transition.session.phaseRemainingSeconds)
        assertEquals(7, transition.session.elapsedSeconds)
        assertEquals(TimerCue.WAIT_COMPLETE, transition.cue)
    }

    @Test
    fun snapshotUsesCeilRemainingSecondsForDisplay() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)

        val transition = TimerEngine.snapshot(started, nowMillis = 1_001L)

        assertEquals(TimerPhase.BLOOM, transition.session.phase)
        assertEquals(3, transition.session.phaseRemainingSeconds)
        assertEquals(0, transition.session.elapsedSeconds)
        assertTrue(transition.session.progress < 1f)
    }

    @Test
    fun snapshotCueFiresOncePerBoundary() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)
        val first = TimerEngine.snapshot(started, nowMillis = 4_000L)

        val second = TimerEngine.snapshot(first.session, nowMillis = 4_000L)

        assertEquals(TimerCue.BLOOM_COMPLETE, first.cue)
        assertNull(second.cue)
        assertEquals(first.session, second.session)
    }

    @Test
    fun pauseResumeExcludesPausedDuration() {
        val started = TimerEngine.start(config, nowMillis = 1_000L)
        val paused = TimerEngine.pause(started, nowMillis = 2_200L)
        val resumed = TimerEngine.resume(paused, nowMillis = 7_200L)

        val transition = TimerEngine.snapshot(resumed, nowMillis = 8_200L)

        assertEquals(TimerPhase.BLOOM, transition.session.phase)
        assertEquals(1, transition.session.phaseRemainingSeconds)
        assertEquals(2, transition.session.elapsedSeconds)
        assertNull(transition.cue)
    }

    @Test
    fun restoredSessionDoesNotReplayAlreadyCrossedBoundaryCue() {
        val restored = TimerEngine.start(config, nowMillis = 1_000L).copy(
            phase = TimerPhase.POUR,
            elapsedSeconds = 3,
            phaseRemainingSeconds = 2,
            lastCueElapsedMillis = 3_000L,
        )

        val transition = TimerEngine.snapshot(restored, nowMillis = 4_000L)

        assertEquals(TimerPhase.POUR, transition.session.phase)
        assertNull(transition.cue)
    }
}
