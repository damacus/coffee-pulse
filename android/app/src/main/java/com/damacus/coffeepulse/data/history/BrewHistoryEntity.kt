package com.damacus.coffeepulse.data.history

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry

@Entity(tableName = "brew_history")
data class BrewHistoryEntity(
    @PrimaryKey val id: String,
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
)

fun BrewHistoryEntry.toEntity(): BrewHistoryEntity {
    return BrewHistoryEntity(
        id = id,
        startedAtMillis = startedAtMillis,
        finishedAtMillis = finishedAtMillis,
        totalSeconds = totalSeconds,
        bloomSeconds = bloomSeconds,
        pulseIntervalSeconds = pulseIntervalSeconds,
        coffeeGrams = coffeeGrams,
        waterRatio = waterRatio,
        totalWaterGrams = totalWaterGrams,
        themeId = themeId,
        rating = rating,
        notes = notes,
    )
}

fun BrewHistoryEntity.toDomain(): BrewHistoryEntry {
    return BrewHistoryEntry(
        id = id,
        startedAtMillis = startedAtMillis,
        finishedAtMillis = finishedAtMillis,
        totalSeconds = totalSeconds,
        bloomSeconds = bloomSeconds,
        pulseIntervalSeconds = pulseIntervalSeconds,
        coffeeGrams = coffeeGrams,
        waterRatio = waterRatio,
        totalWaterGrams = totalWaterGrams,
        themeId = themeId,
        rating = rating,
        notes = notes,
    )
}
