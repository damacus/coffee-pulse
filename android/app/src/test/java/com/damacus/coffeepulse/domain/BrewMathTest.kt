package com.damacus.coffeepulse.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class BrewMathTest {
    @Test
    fun totalWaterRoundsCoffeeByRatio() {
        assertEquals(233, BrewMath.totalWaterGrams(coffeeGrams = 15.0, waterRatio = 15.5))
    }

    @Test
    fun bloomWaterUsesTwoToOneRatio() {
        assertEquals(30, BrewMath.bloomWaterGrams(coffeeGrams = 15.0))
    }

    @Test
    fun mainPourNeverDropsBelowZero() {
        assertEquals(0, BrewMath.mainPourGrams(coffeeGrams = 15.0, waterRatio = 1.0))
    }

    @Test
    fun configValidationRejectsInvalidDurationsAndWeights() {
        assertEquals(emptyList<String>(), BrewMath.validate(30, 5, 15.0, 15.5))
        assertEquals(
            listOf("Durations must be at least 1 second.", "Coffee weight and ratio must be positive."),
            BrewMath.validate(0, 0, 0.0, -1.0),
        )
    }
}
