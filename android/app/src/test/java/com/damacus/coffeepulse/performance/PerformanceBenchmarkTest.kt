package com.damacus.coffeepulse.performance

import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.BrewPreset
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.system.measureNanoTime

class PerformanceBenchmarkTest {

    private val config = BrewConfig(
        bloomSeconds = 45,
        pulseIntervalSeconds = 15,
        coffeeGrams = 20.0,
        waterRatio = 15.0,
    )

    @Test
    fun benchmarkTimerEngineSnapshotThroughput() {
        val started = TimerEngine.start(config, nowMillis = 1_000_000L)
        val iterations = 100_000

        // Warm up
        repeat(1_000) {
            TimerEngine.snapshot(started, nowMillis = 1_000_000L + it * 100L)
        }

        val totalDurationNanos = measureNanoTime {
            var current = started
            for (i in 0 until iterations) {
                val transition = TimerEngine.snapshot(current, nowMillis = 1_000_000L + i * 20L)
                current = transition.session
            }
        }

        val avgNanosPerCall = totalDurationNanos.toDouble() / iterations
        println("PerformanceBenchmark: TimerEngine.snapshot throughput = ${avgNanosPerCall} ns/op (iterations = $iterations)")

        // Must execute under 5 microseconds (5,000 ns) per call
        assertTrue("TimerEngine snapshot took too long: $avgNanosPerCall ns", avgNanosPerCall < 5_000)
    }

    @Test
    fun benchmarkBrewMathCalculations() {
        val iterations = 200_000
        val totalDurationNanos = measureNanoTime {
            var sum = 0
            for (i in 0 until iterations) {
                sum += BrewMath.cumulativeTargetGrams(20.0, 15.0, pulseIndex = i % 10)
            }
        }
        val avgNanosPerCall = totalDurationNanos.toDouble() / iterations
        println("PerformanceBenchmark: BrewMath.cumulativeTargetGrams = ${avgNanosPerCall} ns/op")
        assertTrue("BrewMath calculation took too long: $avgNanosPerCall ns", avgNanosPerCall < 1_000)
    }

    @Test
    fun benchmarkPresetSearchAndFiltering() {
        val presets = BrewPreset.DEFAULT_PRESETS
        val iterations = 20_000
        val searchQueries = listOf("V60", "chemex", "aeropress", "coarse", "hoffmann", "flatbed", "nonexistent")

        var matchCount = 0
        val totalDurationNanos = measureNanoTime {
            for (i in 0 until iterations) {
                val query = searchQueries[i % searchQueries.size]
                val filtered = presets.filter { preset ->
                    preset.name.contains(query, ignoreCase = true) ||
                        preset.description.contains(query, ignoreCase = true) ||
                        preset.grindGuide.contains(query, ignoreCase = true) ||
                        preset.brewerType.contains(query, ignoreCase = true)
                }
                matchCount += filtered.size
            }
        }
        assertTrue(matchCount > 0)
        val avgNanosPerCall = totalDurationNanos.toDouble() / iterations
        println("PerformanceBenchmark: Preset search & filter throughput = ${avgNanosPerCall} ns/op")
        assertTrue("Preset filtering took too long: $avgNanosPerCall ns", avgNanosPerCall < 50_000)
    }
}
