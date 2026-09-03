package com.damacus.coffeepulse.sensory

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BrewHapticPatternTest {
    @Test
    fun allPatternsHaveMatchingTimingAndAmplitudeLengths() {
        BrewHapticCue.entries.forEach { cue ->
            val pattern = BrewHapticPattern.forCue(cue, hasAmplitudeControl = true)

            assertEquals(pattern.timings.size, pattern.amplitudes?.size)
        }
    }

    @Test
    fun pourStartNonZeroAmplitudesTrendUpward() {
        val amplitudes = BrewHapticPattern.forCue(
            BrewHapticCue.PourStart,
            hasAmplitudeControl = true,
        ).nonZeroAmplitudes()

        assertEquals(listOf(130, 200, 255), amplitudes)
        assertTrue(amplitudes.zipWithNext().all { (left, right) -> left < right })
    }

    @Test
    fun stopPourRelaxNonZeroAmplitudesTrendDownward() {
        val amplitudes = BrewHapticPattern.forCue(
            BrewHapticCue.StopPourRelax,
            hasAmplitudeControl = true,
        ).nonZeroAmplitudes()

        assertEquals(listOf(255, 160, 80), amplitudes)
        assertTrue(amplitudes.zipWithNext().all { (left, right) -> left > right })
    }

    @Test
    fun fallbackOmitsAmplitudesWhenAmplitudeControlUnavailable() {
        val pattern = BrewHapticPattern.forCue(
            BrewHapticCue.PourStart,
            hasAmplitudeControl = false,
        )

        assertArrayEquals(longArrayOf(0, 48, 28, 62, 22, 82), pattern.timings)
        assertNull(pattern.amplitudes)
    }

    @Test
    fun pourStartEnvelopeRisesFromLowToHighSharpness() {
        val envelope = BrewHapticPattern.forCue(
            BrewHapticCue.PourStart,
            hasAmplitudeControl = true,
        ).envelope ?: error("Expected pour envelope")

        val sharpness = listOf(envelope.initialSharpness) + envelope.controlPoints.map { it.sharpness }
        assertTrue(sharpness.zipWithNext().all { (left, right) -> left <= right })
        assertEquals(0f, envelope.controlPoints.last().intensity)
    }

    @Test
    fun stopPourEnvelopeFallsFromHighToLowSharpness() {
        val envelope = BrewHapticPattern.forCue(
            BrewHapticCue.StopPourRelax,
            hasAmplitudeControl = true,
        ).envelope ?: error("Expected stop envelope")

        val sharpness = listOf(envelope.initialSharpness) + envelope.controlPoints.map { it.sharpness }
        assertTrue(sharpness.zipWithNext().all { (left, right) -> left >= right })
        assertEquals(0f, envelope.controlPoints.last().intensity)
    }

    private fun BrewHapticPattern.nonZeroAmplitudes(): List<Int> {
        return amplitudes?.toList().orEmpty().filter { it > 0 }
    }
}
