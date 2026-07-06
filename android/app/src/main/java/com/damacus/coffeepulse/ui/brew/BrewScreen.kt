package com.damacus.coffeepulse.ui.brew

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.presentation
import com.damacus.coffeepulse.ui.BrewUiState
import com.damacus.coffeepulse.ui.components.BrewStat
import com.damacus.coffeepulse.ui.components.TimerRing
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

@Composable
fun BrewScreen(
    state: BrewUiState,
    palette: CoffeePulsePalette,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onFinish: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    DisposableEffect(state.session.isRunning) {
        view.keepScreenOn = state.session.isRunning
        onDispose { view.keepScreenOn = false }
    }

    val phaseColor = when (state.session.phase) {
        TimerPhase.IDLE -> palette.phases.idle
        TimerPhase.BLOOM -> palette.phases.bloom
        TimerPhase.POUR -> palette.phases.pour
        TimerPhase.WAIT -> palette.phases.wait
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Header(
            palette = palette,
            soundEnabled = state.config.soundEnabled,
            onToggleSound = onToggleSound,
            onOpenSettings = onOpenSettings,
        )

        Spacer(Modifier.weight(0.36f))

        Surface(
            color = phaseColor.copy(alpha = 0.14f),
            contentColor = phaseColor,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Text(
                text = state.session.phase.presentation().label,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(Modifier.height(18.dp))

        TimerRing(
            session = state.session,
            palette = palette,
            phaseColor = phaseColor,
            modifier = Modifier.size(292.dp),
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = state.session.phase.presentation().hint,
            color = palette.mutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(22.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            BrewStat("Coffee", "${trimNumber(state.config.coffeeGrams)}g", palette)
            BrewStat(
                "Water",
                "${BrewMath.totalWaterGrams(state.config.coffeeGrams, state.config.waterRatio)}g",
                palette,
            )
        }

        if (!state.session.isIdle) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Total ${formatTime(state.session.elapsedSeconds)}",
                color = palette.mutedText,
                fontSize = 12.sp,
            )
        }

        Spacer(Modifier.weight(0.64f))

        Controls(
            isIdle = state.session.isIdle,
            isRunning = state.session.isRunning,
            palette = palette,
            phaseColor = phaseColor,
            onStart = onStart,
            onPause = onPause,
            onReset = onReset,
            onFinish = onFinish,
        )
    }
}

@Composable
private fun Header(
    palette: CoffeePulsePalette,
    soundEnabled: Boolean,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Coffee Pulse",
                color = palette.text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Cafe instrument",
                color = palette.mutedText,
                fontSize = 11.sp,
            )
        }
        Row {
            FilledTonalIconButton(
                onClick = onToggleSound,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                    contentColor = palette.text,
                ),
            ) {
                Icon(
                    imageVector = if (soundEnabled) {
                        Icons.AutoMirrored.Filled.VolumeUp
                    } else {
                        Icons.AutoMirrored.Filled.VolumeOff
                    },
                    contentDescription = if (soundEnabled) "Mute" else "Unmute",
                )
            }
            FilledTonalIconButton(
                onClick = onOpenSettings,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
                    contentColor = palette.text,
                ),
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
private fun Controls(
    isIdle: Boolean,
    isRunning: Boolean,
    palette: CoffeePulsePalette,
    phaseColor: Color,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onFinish: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!isIdle) {
            FilledTonalIconButton(
                onClick = onReset,
                modifier = Modifier.size(58.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = palette.text,
                ),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset")
            }
        }

        Button(
            onClick = if (isRunning) onPause else onStart,
            modifier = Modifier
                .weight(1f)
                .height(58.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) Color(0xFF9E3F35) else phaseColor,
                contentColor = if (isRunning) Color(0xFFFFDAD0) else palette.background,
            ),
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
            )
            Text(
                text = if (isRunning) "Pause" else if (isIdle) "Start Brew" else "Resume",
                modifier = Modifier.padding(start = 8.dp),
                fontWeight = FontWeight.Bold,
            )
        }

        if (!isIdle) {
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.height(58.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
            ) {
                Icon(Icons.Default.Stop, contentDescription = null)
                Text("Finish", modifier = Modifier.padding(start = 6.dp))
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

private fun trimNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
