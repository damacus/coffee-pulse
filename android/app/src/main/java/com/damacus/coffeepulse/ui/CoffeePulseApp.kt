package com.damacus.coffeepulse.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.damacus.coffeepulse.ui.brew.BrewScreen
import com.damacus.coffeepulse.ui.history.HistoryScreen
import com.damacus.coffeepulse.ui.settings.FinishBrewSheet
import com.damacus.coffeepulse.ui.settings.SettingsSheet
import com.damacus.coffeepulse.ui.theme.CoffeePulseTheme
import com.damacus.coffeepulse.ui.theme.paletteFor

@Composable
fun CoffeePulseApp(
    onRequestNotificationPermission: () -> Unit,
    viewModel: BrewViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val palette = paletteFor(state.config.themeId)
    var destination by rememberSaveable { mutableStateOf(CoffeePulseDestination.BREW) }
    var settingsOpen by rememberSaveable { mutableStateOf(false) }
    var selectedHistoryId by rememberSaveable { mutableStateOf<String?>(null) }

    CoffeePulseTheme(palette) {
        val colorScheme = MaterialTheme.colorScheme
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colorScheme.surfaceVariant.copy(alpha = 0.34f),
                            palette.surfaceHigh,
                            palette.background,
                        ),
                        radius = 1_200f,
                    ),
                ),
        ) {
            val expanded = maxWidth >= 840.dp
            if (expanded) {
                Row(Modifier.fillMaxSize()) {
                    NavigationRail(
                        containerColor = colorScheme.surfaceContainer.copy(alpha = 0.82f),
                        contentColor = palette.text,
                    ) {
                        NavigationRailItem(
                            selected = destination == CoffeePulseDestination.BREW,
                            onClick = { destination = CoffeePulseDestination.BREW },
                            icon = { Icon(Icons.Default.Timer, contentDescription = null) },
                            label = { Text("Brew") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = palette.background,
                                selectedTextColor = colorScheme.primary,
                                indicatorColor = colorScheme.primary,
                                unselectedIconColor = palette.mutedText,
                                unselectedTextColor = palette.mutedText,
                            ),
                        )
                        NavigationRailItem(
                            selected = destination == CoffeePulseDestination.HISTORY,
                            onClick = { destination = CoffeePulseDestination.HISTORY },
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text("History") },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = palette.background,
                                selectedTextColor = colorScheme.primary,
                                indicatorColor = colorScheme.primary,
                                unselectedIconColor = palette.mutedText,
                                unselectedTextColor = palette.mutedText,
                            ),
                        )
                    }
                    BrewScreen(
                        state = state,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onStart = {
                            onRequestNotificationPermission()
                            viewModel.startBrew()
                        },
                        onPause = viewModel::pauseBrew,
                        onReset = viewModel::resetBrew,
                        onFinish = viewModel::requestFinish,
                        onOpenSettings = { settingsOpen = true },
                        onToggleSound = viewModel::toggleSound,
                    )
                    HistoryScreen(
                        entries = state.history,
                        selectedId = selectedHistoryId,
                        onSelect = { selectedHistoryId = it },
                        palette = palette,
                        modifier = Modifier.weight(0.9f),
                    )
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        when (destination) {
                            CoffeePulseDestination.BREW -> BrewScreen(
                                state = state,
                                palette = palette,
                                onStart = {
                                    onRequestNotificationPermission()
                                    viewModel.startBrew()
                                },
                                onPause = viewModel::pauseBrew,
                                onReset = viewModel::resetBrew,
                                onFinish = viewModel::requestFinish,
                                onOpenSettings = { settingsOpen = true },
                                onToggleSound = viewModel::toggleSound,
                            )

                            CoffeePulseDestination.HISTORY -> HistoryScreen(
                                entries = state.history,
                                selectedId = selectedHistoryId,
                                onSelect = { selectedHistoryId = it },
                                palette = palette,
                            )
                        }
                    }
                    NavigationBar(
                        containerColor = colorScheme.surfaceContainer.copy(alpha = 0.88f),
                        contentColor = palette.text,
                    ) {
                        NavigationBarItem(
                            selected = destination == CoffeePulseDestination.BREW,
                            onClick = { destination = CoffeePulseDestination.BREW },
                            icon = { Icon(Icons.Default.Coffee, contentDescription = null) },
                            label = { Text("Brew") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = palette.background,
                                selectedTextColor = colorScheme.primary,
                                indicatorColor = colorScheme.primary,
                                unselectedIconColor = palette.mutedText,
                                unselectedTextColor = palette.mutedText,
                            ),
                        )
                        NavigationBarItem(
                            selected = destination == CoffeePulseDestination.HISTORY,
                            onClick = { destination = CoffeePulseDestination.HISTORY },
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text("History") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = palette.background,
                                selectedTextColor = colorScheme.primary,
                                indicatorColor = colorScheme.primary,
                                unselectedIconColor = palette.mutedText,
                                unselectedTextColor = palette.mutedText,
                            ),
                        )
                    }
                }
            }

            if (settingsOpen) {
                SettingsSheet(
                    config = state.config,
                    palette = palette,
                    onDismiss = { settingsOpen = false },
                    onSave = {
                        viewModel.saveConfig(it)
                        settingsOpen = false
                    },
                )
            }

            state.pendingFinish?.let { session ->
                FinishBrewSheet(
                    session = session,
                    palette = palette,
                    onDismiss = viewModel::dismissFinish,
                    onSave = viewModel::saveFinishedBrew,
                )
            }
        }
    }
}
