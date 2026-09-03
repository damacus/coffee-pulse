package com.damacus.coffeepulse.domain.model

data class BrewHistoryEntry(
    val id: String,
    val startedAtMillis: Long,
    val finishedAtMillis: Long,
    val totalSeconds: Int,
    val bloomSeconds: Int,
    val pulseIntervalSeconds: Int,
    val coffeeGrams: Double,
    val waterRatio: Double,
    val totalWaterGrams: Int,
    val themeId: String,
    val rating: Int?,
    val notes: String,
    val grindSetting: String = "",
    val beanOrigin: String = "",
    val roastLevel: String = "",
    val flavorTags: List<String> = emptyList(),
)
