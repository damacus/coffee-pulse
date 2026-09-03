package com.damacus.coffeepulse.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalettes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    config: BrewConfig,
    palette: CoffeePulsePalette,
    onDismiss: () -> Unit,
    onSave: (BrewConfig) -> Unit,
    onOpenPresets: () -> Unit = {},
) {
    var bloom by remember(config) { mutableStateOf(config.bloomSeconds.toString()) }
    var pulse by remember(config) { mutableStateOf(config.pulseIntervalSeconds.toString()) }
    var coffee by remember(config) { mutableStateOf(trimNumber(config.coffeeGrams)) }
    var ratio by remember(config) { mutableStateOf(trimNumber(config.waterRatio)) }
    var themeId by remember(config) { mutableStateOf(config.themeId) }
    var sound by remember(config) { mutableStateOf(config.soundEnabled) }
    var haptics by remember(config) { mutableStateOf(config.hapticsEnabled) }
    var countdownAudio by remember(config) { mutableStateOf(config.countdownAudioEnabled) }
    var showCumulativeWeight by remember(config) { mutableStateOf(config.showCumulativeWeightTarget) }
    var keepScreenOn by remember(config) { mutableStateOf(config.keepScreenOn) }
    var advancedTasting by remember(config) { mutableStateOf(config.advancedTastingWorkflow) }
    var errors by remember { mutableStateOf(emptyList<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Settings", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedButton(
                    onClick = onOpenPresets,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.phases.pour),
                ) {
                    Text("Browse Recipes")
                }
            }

            SectionTitle("Appearance", palette)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                CoffeePulsePalettes.forEach { candidate ->
                    ThemeSwatch(
                        palette = candidate,
                        active = candidate.id == themeId,
                        onClick = { themeId = candidate.id },
                    )
                }
            }

            SectionTitle("Brew Calculator", palette)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(15.0, 20.0, 30.0, 45.0, 60.0).forEach { grams ->
                    FilterChip(
                        selected = coffee.toDoubleOrNull() == grams,
                        onClick = { coffee = trimNumber(grams) },
                        label = { Text("${grams.toInt()}g") },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "Coffee (g)",
                    value = coffee,
                    onValueChange = { coffee = it },
                    modifier = Modifier.weight(1f),
                    isDecimal = true,
                )
                NumberField(
                    label = "Ratio (1:?)",
                    value = ratio,
                    onValueChange = { ratio = it },
                    modifier = Modifier.weight(1f),
                    isDecimal = true,
                )
            }
            Text(
                text = "Total water ${BrewMath.totalWaterGrams(coffee.toDoubleOrNull() ?: 0.0, ratio.toDoubleOrNull() ?: 0.0)}g",
                color = palette.mutedText,
            )

            SectionTitle("Timer Configuration", palette)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    label = "Bloom seconds",
                    value = bloom,
                    onValueChange = { bloom = it },
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    label = "Pulse seconds",
                    value = pulse,
                    onValueChange = { pulse = it },
                    modifier = Modifier.weight(1f),
                )
            }

            SectionTitle("Brew Experience & Cues", palette)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Phase transition sound", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Arpeggios and tone alerts on phase switch", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = sound, onCheckedChange = { sound = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("3-2-1 Audio Countdown Cue", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Chimes 3 seconds before next phase for eyes-free brewing", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = countdownAudio, onCheckedChange = { countdownAudio = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Haptics", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Tactile feedback at phase transitions", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = haptics, onCheckedChange = { haptics = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cumulative Pour Target", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Displays target water weight on scale per pulse", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = showCumulativeWeight, onCheckedChange = { showCumulativeWeight = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Keep Screen On", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Prevents screen lock during an active brew session", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = keepScreenOn, onCheckedChange = { keepScreenOn = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Advanced Tasting & Dial-In", color = palette.text, fontWeight = FontWeight.Medium)
                    Text("Record grind size, bean origin, roast level & flavor tags", color = palette.mutedText, fontSize = 12.sp)
                }
                Switch(checked = advancedTasting, onCheckedChange = { advancedTasting = it })
            }

            if (errors.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x33B43A30), MaterialTheme.shapes.medium)
                        .padding(12.dp),
                ) {
                    errors.forEach { Text(it, color = Color(0xFFFFDAD0), fontSize = 12.sp) }
                }
            }

            Button(
                onClick = {
                    val bloomInt = bloom.toIntOrNull() ?: 0
                    val pulseInt = pulse.toIntOrNull() ?: 0
                    val coffeeDouble = coffee.toDoubleOrNull() ?: 0.0
                    val ratioDouble = ratio.toDoubleOrNull() ?: 0.0
                    val validation = BrewMath.validate(bloomInt, pulseInt, coffeeDouble, ratioDouble)
                    if (validation.isNotEmpty()) {
                        errors = validation
                    } else {
                        onSave(
                            BrewConfig(
                                bloomSeconds = bloomInt,
                                pulseIntervalSeconds = pulseInt,
                                coffeeGrams = coffeeDouble,
                                waterRatio = ratioDouble,
                                themeId = themeId,
                                soundEnabled = sound,
                                hapticsEnabled = haptics,
                                countdownAudioEnabled = countdownAudio,
                                showCumulativeWeightTarget = showCumulativeWeight,
                                keepScreenOn = keepScreenOn,
                                advancedTastingWorkflow = advancedTasting,
                            ),
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text("Save Configuration", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, palette: CoffeePulsePalette) {
    Text(
        text = text,
        color = palette.phases.pour,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val sanitized = if (isDecimal) {
                input.filter { it.isDigit() || it == '.' }
            } else {
                input.filter { it.isDigit() }
            }
            onValueChange(sanitized)
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
        ),
        singleLine = true,
        modifier = modifier,
    )
}

@Composable
private fun ThemeSwatch(
    palette: CoffeePulsePalette,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(palette.background)
                .border(
                    width = if (active) 3.dp else 1.dp,
                    color = if (active) palette.text else palette.mutedText.copy(alpha = 0.4f),
                    shape = CircleShape,
                )
                .padding(6.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(palette.phases.pour),
            )
        }
        Text(
            text = palette.name,
            color = if (active) palette.text else palette.mutedText,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private fun trimNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
