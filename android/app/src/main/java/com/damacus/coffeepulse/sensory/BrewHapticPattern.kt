package com.damacus.coffeepulse.sensory

enum class BrewHapticCue {
    PourStart,
    StopPourRelax,
    Bloom,
    Finish,
}

data class BrewHapticPattern(
    val timings: LongArray,
    val amplitudes: IntArray?,
) {
    companion object {
        fun forCue(cue: BrewHapticCue, hasAmplitudeControl: Boolean): BrewHapticPattern {
            val amplitudes = when (cue) {
                BrewHapticCue.PourStart -> intArrayOf(0, 185, 0, 215, 0, 235, 0, 250, 0, 255)
                BrewHapticCue.StopPourRelax -> intArrayOf(0, 255, 0, 205, 0, 145, 0, 90)
                BrewHapticCue.Bloom -> intArrayOf(0, 150, 0, 115)
                BrewHapticCue.Finish -> intArrayOf(0, 180, 0, 210, 0, 160)
            }
            return BrewHapticPattern(
                timings = timingsFor(cue),
                amplitudes = amplitudes.takeIf { hasAmplitudeControl },
            )
        }

        private fun timingsFor(cue: BrewHapticCue): LongArray = when (cue) {
            BrewHapticCue.PourStart -> longArrayOf(0, 130, 35, 95, 25, 70, 18, 48, 14, 34)
            BrewHapticCue.StopPourRelax -> longArrayOf(0, 42, 18, 60, 28, 90, 45, 130)
            BrewHapticCue.Bloom -> longArrayOf(0, 45, 35, 75)
            BrewHapticCue.Finish -> longArrayOf(0, 55, 35, 55, 35, 90)
        }
    }
}
