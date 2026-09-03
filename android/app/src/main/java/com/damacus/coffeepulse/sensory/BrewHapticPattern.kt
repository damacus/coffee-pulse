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
    val envelope: BrewHapticEnvelope?,
) {
    companion object {
        fun forCue(cue: BrewHapticCue, hasAmplitudeControl: Boolean): BrewHapticPattern {
            val amplitudes = when (cue) {
                BrewHapticCue.PourStart -> intArrayOf(0, 130, 0, 200, 0, 255)
                BrewHapticCue.StopPourRelax -> intArrayOf(0, 255, 0, 160, 0, 80)
                BrewHapticCue.Bloom -> intArrayOf(0, 150, 0, 115)
                BrewHapticCue.Finish -> intArrayOf(0, 180, 0, 210, 0, 160)
            }
            return BrewHapticPattern(
                timings = timingsFor(cue),
                amplitudes = amplitudes.takeIf { hasAmplitudeControl },
                envelope = envelopeFor(cue),
            )
        }

        private fun timingsFor(cue: BrewHapticCue): LongArray = when (cue) {
            BrewHapticCue.PourStart -> longArrayOf(0, 48, 28, 62, 22, 82)
            BrewHapticCue.StopPourRelax -> longArrayOf(0, 54, 24, 86, 34, 132)
            BrewHapticCue.Bloom -> longArrayOf(0, 45, 35, 75)
            BrewHapticCue.Finish -> longArrayOf(0, 55, 35, 55, 35, 90)
        }

        private fun envelopeFor(cue: BrewHapticCue): BrewHapticEnvelope? = when (cue) {
            BrewHapticCue.PourStart -> BrewHapticEnvelope(
                initialSharpness = 0.05f,
                controlPoints = listOf(
                    BrewHapticControlPoint(intensity = 0.35f, sharpness = 0.15f, durationMillis = 40L),
                    BrewHapticControlPoint(intensity = 0.78f, sharpness = 0.55f, durationMillis = 70L),
                    BrewHapticControlPoint(intensity = 1.00f, sharpness = 1.00f, durationMillis = 90L),
                    BrewHapticControlPoint(intensity = 0.00f, sharpness = 1.00f, durationMillis = 50L),
                ),
            )
            BrewHapticCue.StopPourRelax -> BrewHapticEnvelope(
                initialSharpness = 1.00f,
                controlPoints = listOf(
                    BrewHapticControlPoint(intensity = 1.00f, sharpness = 0.95f, durationMillis = 45L),
                    BrewHapticControlPoint(intensity = 0.70f, sharpness = 0.58f, durationMillis = 75L),
                    BrewHapticControlPoint(intensity = 0.35f, sharpness = 0.22f, durationMillis = 100L),
                    BrewHapticControlPoint(intensity = 0.00f, sharpness = 0.05f, durationMillis = 80L),
                ),
            )
            BrewHapticCue.Bloom,
            BrewHapticCue.Finish,
            -> null
        }
    }
}

data class BrewHapticEnvelope(
    val initialSharpness: Float,
    val controlPoints: List<BrewHapticControlPoint>,
)

data class BrewHapticControlPoint(
    val intensity: Float,
    val sharpness: Float,
    val durationMillis: Long,
)
