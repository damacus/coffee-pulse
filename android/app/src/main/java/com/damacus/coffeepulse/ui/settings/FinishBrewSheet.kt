package com.damacus.coffeepulse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

private val QUICK_FLAVOR_TAGS = listOf(
    "Fruity", "Floral", "Sweet", "Citrus", "Chocolate", "Nutty", "Bright", "Sour", "Bitter", "Astringent",
)

private val ROAST_LEVELS = listOf("Light", "Medium-Light", "Medium", "Medium-Dark", "Dark")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FinishBrewSheet(
    session: TimerSession,
    palette: CoffeePulsePalette,
    onDismiss: () -> Unit,
    onFinishWithoutSaving: () -> Unit,
    onSave: (
        rating: Int?,
        notes: String,
        grindSetting: String,
        beanOrigin: String,
        roastLevel: String,
        flavorTags: List<String>,
    ) -> Unit,
) {
    var showSaveDetails by rememberSaveable { mutableStateOf(false) }
    var rating by rememberSaveable { mutableStateOf<Int?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }
    var grindSetting by rememberSaveable { mutableStateOf("") }
    var beanOrigin by rememberSaveable { mutableStateOf("") }
    var roastLevel by rememberSaveable { mutableStateOf("") }
    var selectedTags by rememberSaveable { mutableStateOf(setOf<String>()) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("End this brew?", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            val totalWater = BrewMath.totalWaterGrams(session.config.coffeeGrams, session.config.waterRatio)
            Text(
                text = "${formatTime(session.elapsedSeconds)} / ${trimNumber(session.config.coffeeGrams)}g coffee / " +
                    "${totalWater}g water",
                color = palette.mutedText,
            )

            Button(
                onClick = onFinishWithoutSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.phases.pour,
                    contentColor = palette.background,
                ),
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Text(
                    "Finish without saving",
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold,
                )
            }

            if (!showSaveDetails) {
                OutlinedButton(
                    onClick = { showSaveDetails = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.text),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text("Save to history & dial-in", modifier = Modifier.padding(start = 8.dp))
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            } else {
                HorizontalDivider(color = palette.mutedText.copy(alpha = 0.24f))
                Text("Save to history", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text("Rating", color = palette.phases.pour, fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    (1..5).forEach { value ->
                        FilterChip(
                            selected = rating == value,
                            onClick = { rating = if (rating == value) null else value },
                            label = { Text(value.toString()) },
                        )
                    }
                }

                if (session.config.advancedTastingWorkflow) {
                    Text("Dial-In Parameters", color = palette.phases.pour, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedTextField(
                            value = grindSetting,
                            onValueChange = { grindSetting = it },
                            label = { Text("Grind (Clicks/Size)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = beanOrigin,
                            onValueChange = { beanOrigin = it },
                            label = { Text("Bean / Roaster") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                        )
                    }

                    Text("Roast Level", color = palette.phases.pour, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ROAST_LEVELS.forEach { level ->
                            FilterChip(
                                selected = roastLevel == level,
                                onClick = { roastLevel = if (roastLevel == level) "" else level },
                                label = { Text(level) },
                            )
                        }
                    }

                    Text("Taste & Flavor Attributes", color = palette.phases.pour, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        QUICK_FLAVOR_TAGS.forEach { tag ->
                            val isSelected = tag in selectedTags
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedTags = if (isSelected) selectedTags - tag else selectedTags + tag
                                },
                                label = { Text(tag) },
                            )
                        }
                    }

                    // Dial-In feedback tip if sour or bitter
                    if ("Sour" in selectedTags || "Bitter" in selectedTags || "Astringent" in selectedTags) {
                        val tip = when {
                            "Sour" in selectedTags -> "Tip: Sourness usually means under-extraction. Try grinding finer, using hotter water, or pouring slower."
                            "Bitter" in selectedTags || "Astringent" in selectedTags -> "Tip: Bitterness/astringency indicates over-extraction or channeling. Try grinding coarser or reducing agitation."
                            else -> ""
                        }
                        Text(
                            text = tip,
                            color = palette.phases.pour,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("General Notes") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                Button(
                    onClick = {
                        onSave(
                            rating,
                            notes,
                            grindSetting,
                            beanOrigin,
                            roastLevel,
                            selectedTags.toList(),
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.phases.pour,
                        contentColor = palette.background,
                    ),
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Text(
                        "Save brew",
                        modifier = Modifier.padding(start = 8.dp),
                        fontWeight = FontWeight.Bold,
                    )
                }
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
