package com.damacus.coffeepulse.domain.model

data class BrewConfig(
    val bloomSeconds: Int = 30,
    val pulseIntervalSeconds: Int = 5,
    val coffeeGrams: Double = 15.0,
    val waterRatio: Double = 15.5,
    val themeId: String = "instrument",
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val countdownAudioEnabled: Boolean = true,
    val showCumulativeWeightTarget: Boolean = true,
    val keepScreenOn: Boolean = true,
    val advancedTastingWorkflow: Boolean = true,
)
