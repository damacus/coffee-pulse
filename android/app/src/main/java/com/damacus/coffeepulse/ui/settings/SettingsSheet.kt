package com.damacus.coffeepulse.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
) {
    var bloom by remember(config) { mutableStateOf(config.bloomSeconds.toString()) }
    var pulse by remember(config) { mutableStateOf(config.pulseIntervalSeconds.toString()) }
    var coffee by remember(config) { mutableStateOf(trimNumber(config.coffeeGrams)) }
    var ratio by remember(config) { mutableStateOf(trimNumber(config.waterRatio)) }
    var themeId by remember(config) { mutableStateOf(config.themeId) }
    var sound by remember(config) { mutableStateOf(config.soundEnabled) }
    var haptics by remember(config) { mutableStateOf(config.hapticsEnabled) }
    var errors by remember { mutableStateOf(emptyList<String>()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)

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
                listOf(15.0, 30.0, 45.0, 60.0, 75.0).forEach { grams ->
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
                )
                NumberField(
                    label = "Ratio (1:?)",
                    value = ratio,
                    onValueChange = { ratio = it },
                    modifier = Modifier.weight(1f),
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Sound cues", color = palette.text)
                Switch(checked = sound, onCheckedChange = { sound = it })
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Haptics", color = palette.text)
                Switch(checked = haptics, onCheckedChange = { haptics = it })
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
private fun ThemeSwatch(
    palette: CoffeePulsePalette,
    active: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(palette.background)
                .border(
                    width = if (active) 3.dp else 1.dp,
                    color = if (active) palette.phases.pour else palette.text.copy(alpha = 0.24f),
                    shape = CircleShape,
                )
                .padding(11.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            listOf(palette.phases.bloom, palette.phases.pour, palette.phases.wait).forEach { color ->
                Spacer(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }
        Text(
            text = palette.name,
            color = palette.text,
            fontSize = 10.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = modifier,
    )
}

private fun trimNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
