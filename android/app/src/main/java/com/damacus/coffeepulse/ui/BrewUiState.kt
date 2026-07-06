package com.damacus.coffeepulse.ui

import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import com.damacus.coffeepulse.domain.model.TimerSession

data class BrewUiState(
    val config: BrewConfig = BrewConfig(),
    val session: TimerSession = TimerEngine.idle(),
    val history: List<BrewHistoryEntry> = emptyList(),
    val pendingFinish: TimerSession? = null,
)

enum class CoffeePulseDestination {
    BREW,
    HISTORY,
}
