package com.damacus.coffeepulse.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette
import java.text.DateFormat
import java.util.Date

@Composable
fun HistoryScreen(
    entries: List<BrewHistoryEntry>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    palette: CoffeePulsePalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(24.dp),
    ) {
        Text(
            text = "History",
            color = palette.text,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Manual saves from finished brews",
            color = palette.mutedText,
            fontSize = 13.sp,
        )

        Spacer(Modifier.height(22.dp))

        if (entries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No brews saved yet", color = palette.text, fontWeight = FontWeight.Bold)
                Text("Finish a timer to add notes and rating.", color = palette.mutedText)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(entries, key = { it.id }) { entry ->
                    HistoryCard(
                        entry = entry,
                        selected = entry.id == selectedId,
                        palette = palette,
                        onClick = { onSelect(entry.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    entry: BrewHistoryEntry,
    selected: Boolean,
    palette: CoffeePulsePalette,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) palette.surfaceHigh else palette.surface.copy(alpha = 0.78f),
        ),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.finishedAtMillis)),
                    color = palette.text,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = formatTime(entry.totalSeconds),
                    color = palette.phases.pour,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "${trimNumber(entry.coffeeGrams)}g coffee / ${entry.totalWaterGrams}g water / 1:${trimNumber(entry.waterRatio)}",
                color = palette.mutedText,
            )
            if (entry.rating != null) {
                Text("Rating ${entry.rating}/5", color = palette.text)
            }
            if (entry.notes.isNotBlank()) {
                Text(entry.notes, color = palette.mutedText)
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
