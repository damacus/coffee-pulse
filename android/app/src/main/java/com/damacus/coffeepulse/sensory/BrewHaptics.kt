package com.damacus.coffeepulse.sensory

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class BrewHaptics(context: Context) {
    private val vibratorManager: VibratorManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)
    } else {
        null
    }

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun start(enabled: Boolean) {
        play(BrewHapticCue.Bloom, enabled)
    }

    fun bloomComplete(enabled: Boolean) {
        play(BrewHapticCue.PourStart, enabled)
    }

    fun pourComplete(enabled: Boolean) {
        play(BrewHapticCue.StopPourRelax, enabled)
    }

    fun waitComplete(enabled: Boolean) {
        play(BrewHapticCue.PourStart, enabled)
    }

    fun finish(enabled: Boolean) {
        play(BrewHapticCue.Finish, enabled)
    }

    fun cancel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            vibratorManager?.cancel()
        } else {
            vibrator?.cancel()
        }
    }

    private fun play(cue: BrewHapticCue, enabled: Boolean) {
        if (!enabled) return
        val vibrator = vibrator ?: return
        if (!vibrator.hasVibrator()) return
        val pattern = BrewHapticPattern.forCue(cue, vibrator.hasAmplitudeControl())
        val effect = envelopeEffect(vibrator, pattern) ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pattern.amplitudes == null) {
                VibrationEffect.createWaveform(pattern.timings, -1)
            } else {
                VibrationEffect.createWaveform(pattern.timings, pattern.amplitudes, -1)
            }
        } else {
            @Suppress("DEPRECATION")
            return vibrator.vibrate(pattern.timings, -1)
        }
        vibrator.vibrate(effect)
    }

    private fun envelopeEffect(
        vibrator: Vibrator,
        pattern: BrewHapticPattern,
    ): VibrationEffect? {
        if (Build.VERSION.SDK_INT < 36 || !vibrator.areEnvelopeEffectsSupported()) return null
        val envelope = pattern.envelope ?: return null
        return runCatching {
            val builder = VibrationEffect.BasicEnvelopeBuilder()
                .setInitialSharpness(envelope.initialSharpness)
            envelope.controlPoints.forEach { point ->
                builder.addControlPoint(
                    point.intensity,
                    point.sharpness,
                    point.durationMillis,
                )
            }
            builder.build()
        }.getOrNull()
    }
}
