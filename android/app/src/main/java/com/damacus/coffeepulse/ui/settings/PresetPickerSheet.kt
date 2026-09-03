package com.damacus.coffeepulse.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.BrewPreset
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PresetPickerSheet(
    palette: CoffeePulsePalette,
    onDismiss: () -> Unit,
    onSelectPreset: (BrewConfig) -> Unit,
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPresets = remember(selectedCategory, searchQuery) {
        BrewPreset.DEFAULT_PRESETS.filter { preset ->
            val matchesCategory = (selectedCategory == "All") || (preset.brewerType == selectedCategory)
            val query = searchQuery.trim().lowercase()
            val matchesSearch = query.isEmpty() ||
                preset.name.lowercase().contains(query) ||
                preset.description.lowercase().contains(query) ||
                preset.grindGuide.lowercase().contains(query) ||
                preset.brewerType.lowercase().contains(query)
            matchesCategory && matchesSearch
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface,
        contentColor = palette.text,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Brew Recipes & Presets",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = palette.text,
            )

            // Search text box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search V60, Chemex, AeroPress, grind...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
            )

            // Filter chips by Brewer Type
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BrewPreset.ALL_BREWER_TYPES.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                    )
                }
            }

            if (filteredPresets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "No matching recipes found",
                        color = palette.text,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "Try clearing filters or search terms",
                        color = palette.mutedText,
                        fontSize = 12.sp,
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false),
                ) {
                    items(filteredPresets, key = { it.id }) { preset ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectPreset(preset.config)
                                    onDismiss()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = palette.surfaceHigh.copy(alpha = 0.75f),
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = preset.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = palette.text,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                    )
                                    SuggestionChip(
                                        onClick = {},
                                        label = {
                                            Text(
                                                text = preset.brewerType,
                                                fontSize = 11.sp,
                                                color = palette.phases.pour,
                                                fontWeight = FontWeight.Bold,
                                            )
                                        },
                                    )
                                }

                                Text(
                                    text = preset.description,
                                    fontSize = 13.sp,
                                    color = palette.mutedText,
                                )

                                val water = BrewMath.totalWaterGrams(
                                    preset.config.coffeeGrams,
                                    preset.config.waterRatio,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "Ratio: 1:${trimNumber(preset.config.waterRatio)} • ${preset.config.coffeeGrams.toInt()}g / ${water}g",
                                        fontSize = 12.sp,
                                        color = palette.text.copy(alpha = 0.85f),
                                        fontWeight = FontWeight.Medium,
                                    )
                                    Text(
                                        text = "Grind: ${preset.grindGuide}",
                                        fontSize = 12.sp,
                                        color = palette.phases.pour,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun trimNumber(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else "%.1f".format(value)
}
