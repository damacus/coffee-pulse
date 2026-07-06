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

        assertEquals(listOf(185, 215, 235, 250, 255), amplitudes)
        assertTrue(amplitudes.zipWithNext().all { (left, right) -> left < right })
    }

    @Test
    fun stopPourRelaxNonZeroAmplitudesTrendDownward() {
        val amplitudes = BrewHapticPattern.forCue(
            BrewHapticCue.StopPourRelax,
            hasAmplitudeControl = true,
        ).nonZeroAmplitudes()

        assertEquals(listOf(255, 205, 145, 90), amplitudes)
        assertTrue(amplitudes.zipWithNext().all { (left, right) -> left > right })
    }

    @Test
    fun fallbackOmitsAmplitudesWhenAmplitudeControlUnavailable() {
        val pattern = BrewHapticPattern.forCue(
            BrewHapticCue.PourStart,
            hasAmplitudeControl = false,
        )

        assertArrayEquals(longArrayOf(0, 130, 35, 95, 25, 70, 18, 48, 14, 34), pattern.timings)
        assertNull(pattern.amplitudes)
    }

    private fun BrewHapticPattern.nonZeroAmplitudes(): List<Int> {
        return amplitudes?.toList().orEmpty().filter { it > 0 }
    }
}
