package com.damacus.coffeepulse.ui.brew

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.ui.BrewUiState
import com.damacus.coffeepulse.ui.components.BrewStat
import com.damacus.coffeepulse.ui.components.BrewTimelineBar
import com.damacus.coffeepulse.ui.components.TimerRing
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

@Composable
fun BrewScreen(
    state: BrewUiState,
    palette: CoffeePulsePalette,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenPresets: () -> Unit = {},
) {
    val view = LocalView.current
    val context = LocalContext.current

    // Keep screen on when brewing if configured
    DisposableEffect(state.session.isRunning, state.config.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (state.session.isRunning && state.config.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            view.keepScreenOn = true
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            view.keepScreenOn = false
        }
    }

    val phaseColor = when (state.session.phase) {
        TimerPhase.BLOOM -> palette.phases.bloom
        TimerPhase.POUR -> palette.phases.pour
        TimerPhase.WAIT -> palette.phases.wait
        TimerPhase.IDLE -> palette.surfaceHigh
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = Color.Transparent,
        topBar = {
            Header(
                palette = palette,
                showExit = !state.session.isIdle,
                soundEnabled = state.config.soundEnabled,
                onExit = onFinish,
                onToggleSound = onToggleSound,
                onOpenSettings = onOpenSettings,
                onOpenPresets = onOpenPresets,
            )
        },
        bottomBar = {
            Controls(
                isIdle = state.session.isIdle,
                isRunning = state.session.isRunning,
                palette = palette,
                onStart = onStart,
                onPause = onPause,
                onFinish = onFinish,
            )
        },
    ) { contentPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            val compactHeight = maxHeight < 620.dp
            val availableRingWidth = (maxWidth - 32.dp).coerceAtLeast(160.dp)
            val ringSize = minOf(availableRingWidth, if (compactHeight) 210.dp else 260.dp)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = if (compactHeight) 6.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                PhaseInstruction(
                    phase = state.session.phase,
                    isRunning = state.session.isRunning,
                    isIdle = state.session.isIdle,
                    color = phaseColor,
                    onColor = palette.background,
                )

                Spacer(Modifier.height(if (compactHeight) 8.dp else 14.dp))

                TimerRing(
                    session = state.session,
                    palette = palette,
                    phaseColor = phaseColor,
                    modifier = Modifier.size(ringSize),
                )

                Spacer(Modifier.height(8.dp))

                NextCue(
                    phase = state.session.phase,
                    isRunning = state.session.isRunning,
                    isIdle = state.session.isIdle,
                    palette = palette,
                )

                // Cumulative Pour Weight target banner (if active & enabled)
                if (state.config.showCumulativeWeightTarget) {
                    Spacer(Modifier.height(10.dp))
                    val targetGrams = BrewMath.cumulativeTargetGrams(
                        state.config.coffeeGrams,
                        state.config.waterRatio,
                        state.session.pulseIndex,
                    )
                    val totalWater = BrewMath.totalWaterGrams(state.config.coffeeGrams, state.config.waterRatio)

                    Card(
                        modifier = Modifier.fillMaxWidth(0.92f),
                        colors = CardDefaults.cardColors(
                            containerColor = palette.surfaceHigh.copy(alpha = 0.82f),
                        ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Scale,
                                contentDescription = null,
                                tint = palette.phases.pour,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = if (state.session.isIdle) {
                                    "Target bloom: ${targetGrams}g on scale (Total: ${totalWater}g)"
                                } else {
                                    "Pour up to: ${targetGrams}g on scale (Total: ${totalWater}g)"
                                },
                                color = palette.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Timeline Bar
                BrewTimelineBar(
                    session = state.session,
                    palette = palette,
                    modifier = Modifier.fillMaxWidth(0.92f),
                )

                Spacer(Modifier.height(if (compactHeight) 10.dp else 16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                    BrewStat("Coffee", "${trimNumber(state.config.coffeeGrams)}g", palette)
                    BrewStat(
                        "Water",
                        "${BrewMath.totalWaterGrams(state.config.coffeeGrams, state.config.waterRatio)}g",
                        palette,
                    )
                }

                if (!state.session.isIdle) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Total elapsed: ${formatTime(state.session.elapsedSeconds)}",
                        color = palette.mutedText,
                        fontSize = 12.sp,
                    )
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun Header(
    palette: CoffeePulsePalette,
    showExit: Boolean,
    soundEnabled: Boolean,
    onExit: () -> Unit,
    onToggleSound: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPresets: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Coffee Pulse",
                color = palette.text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Brew timer",
                color = palette.mutedText,
                fontSize = 11.sp,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            HeaderIconButton(onClick = onOpenPresets, palette = palette) {
                Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Recipes & Presets")
            }
            if (showExit) {
                HeaderIconButton(onClick = onExit, palette = palette) {
                    Icon(Icons.Default.Close, contentDescription = "End brew")
                }
            }
            HeaderIconButton(onClick = onToggleSound, palette = palette) {
                Icon(
                    imageVector = if (soundEnabled) {
                        Icons.AutoMirrored.Filled.VolumeUp
                    } else {
                        Icons.AutoMirrored.Filled.VolumeOff
                    },
                    contentDescription = if (soundEnabled) "Mute" else "Unmute",
                )
            }
            HeaderIconButton(onClick = onOpenSettings, palette = palette) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    onClick: () -> Unit,
    palette: CoffeePulsePalette,
    content: @Composable () -> Unit,
) {
    FilledTonalIconButton(
        onClick = onClick,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.82f),
            contentColor = palette.text,
        ),
        content = content,
    )
}

@Composable
private fun PhaseInstruction(
    phase: TimerPhase,
    isRunning: Boolean,
    isIdle: Boolean,
    color: Color,
    onColor: Color,
) {
    val instruction = phaseInstruction(phase, isRunning, isIdle)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isIdle) MaterialTheme.colorScheme.surfaceContainerHigh else color,
        contentColor = if (isIdle) MaterialTheme.colorScheme.onSurface else onColor,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(
                        color = if (isIdle) {
                            MaterialTheme.colorScheme.surfaceContainerHighest
                        } else {
                            Color.White.copy(alpha = 0.18f)
                        },
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = instruction.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Column {
                Text(
                    text = instruction.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = instruction.hint,
                    fontSize = 12.sp,
                    color = (if (isIdle) MaterialTheme.colorScheme.onSurfaceVariant else onColor).copy(alpha = 0.86f),
                )
            }
        }
    }
}

@Composable
private fun NextCue(
    phase: TimerPhase,
    isRunning: Boolean,
    isIdle: Boolean,
    palette: CoffeePulsePalette,
) {
    val text = when {
        isIdle -> "First: bloom"
        !isRunning -> "Resume to continue"
        phase == TimerPhase.BLOOM -> "Next: pour"
        phase == TimerPhase.POUR -> "Next: stop pouring"
        else -> "Next: pour"
    }
    val cueColor = when {
        !isRunning || isIdle -> palette.mutedText
        phase == TimerPhase.POUR -> palette.phases.wait
        else -> palette.phases.pour
    }
    Text(
        text = text.uppercase(),
        color = cueColor,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Controls(
    isIdle: Boolean,
    isRunning: Boolean,
    palette: CoffeePulsePalette,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onFinish: () -> Unit,
) {
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isIdle) palette.phases.pour else palette.text,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonContainerColor",
    )

    Surface(color = palette.background.copy(alpha = 0.96f)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = if (isRunning) onPause else onStart,
                modifier = Modifier
                    .weight(1f)
                    .height(58.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = animatedContainerColor,
                    contentColor = palette.background,
                ),
            ) {
                Row(
                    modifier = Modifier.animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AnimatedContent(
                        targetState = isRunning,
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 2 }) togetherWith
                                (fadeOut() + slideOutVertically { -it / 2 })
                        },
                        label = "buttonIcon",
                    ) { running ->
                        Icon(
                            imageVector = if (running) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                        )
                    }

                    AnimatedContent(
                        targetState = when {
                            isIdle -> "Start Brew"
                            isRunning -> "Pause Timer"
                            else -> "Resume Timer"
                        },
                        transitionSpec = {
                            (fadeIn() + slideInVertically { it / 2 }) togetherWith
                                (fadeOut() + slideOutVertically { -it / 2 })
                        },
                        label = "buttonText",
                    ) { labelText ->
                        Text(
                            text = labelText,
                            modifier = Modifier.padding(start = 8.dp),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !isIdle,
                enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    expandHorizontally(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow,
                        ),
                        expandFrom = Alignment.End,
                    ),
                exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                    shrinkHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        shrinkTowards = Alignment.End,
                    ),
            ) {
                OutlinedButton(
                    onClick = onFinish,
                    modifier = Modifier.height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Text("End", modifier = Modifier.padding(start = 6.dp))
                }
            }
        }
    }
}

private data class PhaseInstructionUi(
    val label: String,
    val hint: String,
    val icon: ImageVector,
)

private fun phaseInstruction(
    phase: TimerPhase,
    isRunning: Boolean,
    isIdle: Boolean,
): PhaseInstructionUi = when {
    isIdle -> PhaseInstructionUi("READY", "Set up your brewer, then start.", Icons.Default.Coffee)
    !isRunning -> PhaseInstructionUi("TIMER PAUSED", "Resume when you are ready.", Icons.Default.PauseCircle)
    phase == TimerPhase.BLOOM -> PhaseInstructionUi(
        "BLOOM",
        "Wet the grounds, then let them bloom.",
        Icons.Default.Coffee,
    )
    phase == TimerPhase.POUR -> PhaseInstructionUi(
        "POUR NOW",
        "Keep the stream slow and even.",
        Icons.Default.WaterDrop,
    )
    else -> PhaseInstructionUi(
        "STOP POURING",
        "Hands off while the coffee drains.",
        Icons.Default.StopCircle,
    )
}

private fun formatTime(seconds: Int): String {
    return "%d:%02d".format(seconds / 60, seconds % 60)
}

private fun trimNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
