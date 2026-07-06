package com.damacus.coffeepulse.sensory

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.tanh

class BrewAudioPlayer {
    fun playBloomArpeggio(enabled: Boolean) {
        if (!enabled) return
        thread(name = "coffee-pulse-bloom-cue") {
            playCue(
                listOf(
                    CueNote(frequencyHz = 261.63, startMs = 0, durationMs = 520, volume = 0.16),
                    CueNote(frequencyHz = 329.63, startMs = 120, durationMs = 520, volume = 0.15),
                    CueNote(frequencyHz = 392.0, startMs = 240, durationMs = 540, volume = 0.14),
                    CueNote(frequencyHz = 523.25, startMs = 360, durationMs = 920, volume = 0.12),
                ),
            )
        }
    }

    fun playHighPing(enabled: Boolean) {
        if (!enabled) return
        thread(name = "coffee-pulse-pour-cue") {
            playCue(
                listOf(
                    CueNote(frequencyHz = 880.0, startMs = 0, durationMs = 520, volume = 0.12),
                    CueNote(frequencyHz = 1_760.0, startMs = 8, durationMs = 180, volume = 0.018),
                ),
            )
        }
    }

    fun playLowPing(enabled: Boolean) {
        if (!enabled) return
        thread(name = "coffee-pulse-wait-cue") {
            playCue(
                listOf(
                    CueNote(frequencyHz = 440.0, startMs = 0, durationMs = 740, volume = 0.13),
                    CueNote(frequencyHz = 880.0, startMs = 14, durationMs = 220, volume = 0.016),
                ),
            )
        }
    }

    private fun playCue(notes: List<CueNote>) {
        val sampleRate = 44_100
        val durationMs = notes.maxOf { it.startMs + it.durationMs } + 80
        val sampleCount = (sampleRate * durationMs) / 1_000
        val samples = ShortArray(sampleCount)

        for (sampleIndex in samples.indices) {
            val timeMs = (sampleIndex.toDouble() / sampleRate.toDouble()) * 1_000.0
            var mixed = 0.0

            notes.forEach { note ->
                val noteTimeMs = timeMs - note.startMs.toDouble()
                if (noteTimeMs >= 0.0 && noteTimeMs <= note.durationMs.toDouble()) {
                    val noteTimeSeconds = noteTimeMs / 1_000.0
                    val wave = sin(2.0 * PI * noteTimeSeconds * note.frequencyHz)
                    val shimmer = sin(2.0 * PI * noteTimeSeconds * note.frequencyHz * 2.0) * 0.05
                    mixed += (wave + shimmer) * note.envelope(noteTimeMs) * note.volume
                }
            }

            val softLimited = tanh(mixed * 1.4) / 1.4
            samples[sampleIndex] = (softLimited * Short.MAX_VALUE).toInt().toShort()
        }

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(samples.size * Short.SIZE_BYTES)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(samples, 0, samples.size)
        audioTrack.play()
        Thread.sleep(durationMs.toLong() + 80L)
        audioTrack.release()
    }

    private data class CueNote(
        val frequencyHz: Double,
        val startMs: Int,
        val durationMs: Int,
        val volume: Double,
    ) {
        fun envelope(noteTimeMs: Double): Double {
            val attackMs = 38.0
            val releaseMs = 28.0
            val duration = durationMs.toDouble()
            val attack = (noteTimeMs / attackMs).coerceIn(0.0, 1.0)
            val decayProgress = ((noteTimeMs - attackMs) / (duration - attackMs)).coerceIn(0.0, 1.0)
            val decay = exp(-5.4 * decayProgress)
            val release = ((duration - noteTimeMs) / releaseMs).coerceIn(0.0, 1.0)
            return attack * decay * release
        }
    }
}
