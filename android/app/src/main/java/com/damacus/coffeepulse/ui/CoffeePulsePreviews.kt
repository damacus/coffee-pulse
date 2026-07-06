package com.damacus.coffeepulse.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import com.damacus.coffeepulse.ui.brew.BrewScreen
import com.damacus.coffeepulse.ui.history.HistoryScreen
import com.damacus.coffeepulse.ui.theme.CoffeePulseTheme
import com.damacus.coffeepulse.ui.theme.paletteFor

@PreviewScreenSizes
@PreviewFontScale
@Composable
private fun BrewScreenPreview() {
    val config = BrewConfig()
    val session = TimerEngine.start(config, nowMillis = 1_000L)
    val palette = paletteFor(config.themeId)
    CoffeePulseTheme(palette) {
        BrewScreen(
            state = BrewUiState(config = config, session = session),
            palette = palette,
            onStart = {},
            onPause = {},
            onReset = {},
            onFinish = {},
            onOpenSettings = {},
            onToggleSound = {},
        )
    }
}

@PreviewScreenSizes
@Composable
private fun HistoryScreenPreview() {
    val palette = paletteFor("instrument")
    CoffeePulseTheme(palette) {
        HistoryScreen(
            entries = listOf(
                BrewHistoryEntry(
                    id = "preview",
                    startedAtMillis = 1_000L,
                    finishedAtMillis = 151_000L,
                    totalSeconds = 150,
                    bloomSeconds = 30,
                    pulseIntervalSeconds = 5,
                    coffeeGrams = 15.0,
                    waterRatio = 15.5,
                    totalWaterGrams = 233,
                    themeId = "instrument",
                    rating = 4,
                    notes = "Sweet cup with a clean finish.",
                ),
            ),
            selectedId = "preview",
            onSelect = {},
            palette = palette,
        )
    }
}
