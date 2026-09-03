package com.damacus.coffeepulse.domain

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

object BrewMath {
    fun totalWaterGrams(coffeeGrams: Double, waterRatio: Double): Int {
        return (coffeeGrams * waterRatio).roundToInt()
    }

    fun bloomWaterGrams(coffeeGrams: Double): Int {
        return (coffeeGrams * 2.0).roundToInt()
    }

    fun mainPourGrams(coffeeGrams: Double, waterRatio: Double): Int {
        return max(0, totalWaterGrams(coffeeGrams, waterRatio) - bloomWaterGrams(coffeeGrams))
    }

    fun cumulativeTargetGrams(
        coffeeGrams: Double,
        waterRatio: Double,
        pulseIndex: Int, // 0 for bloom, 1 for pulse 1, 2 for pulse 2...
        estimatedTotalPulses: Int = 4,
    ): Int {
        val totalWater = totalWaterGrams(coffeeGrams, waterRatio)
        val bloomWater = min(bloomWaterGrams(coffeeGrams), totalWater)
        if (pulseIndex <= 0) return bloomWater

        val remainingWater = totalWater - bloomWater
        val totalPourSteps = (estimatedTotalPulses / 2).coerceAtLeast(1)
        val pourStepIndex = ((pulseIndex + 1) / 2).coerceIn(1, totalPourSteps)
        val waterPerStep = remainingWater.toDouble() / totalPourSteps
        return min(totalWater, (bloomWater + pourStepIndex * waterPerStep).roundToInt())
    }

    fun validate(
        bloomSeconds: Int,
        pulseIntervalSeconds: Int,
        coffeeGrams: Double,
        waterRatio: Double,
    ): List<String> {
        val errors = mutableListOf<String>()
        if (bloomSeconds < 1 || pulseIntervalSeconds < 1) {
            errors += "Durations must be at least 1 second."
        }
        if (coffeeGrams <= 0.0 || waterRatio <= 0.0) {
            errors += "Coffee weight and ratio must be positive."
        }
        return errors
    }
}
