package com.damacus.coffeepulse.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishBrewSheet(
    session: TimerSession,
    palette: CoffeePulsePalette,
    onDismiss: () -> Unit,
    onSave: (rating: Int?, notes: String) -> Unit,
) {
    var rating by rememberSaveable { mutableStateOf<Int?>(null) }
    var notes by rememberSaveable { mutableStateOf("") }

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
            Text("Finish Brew", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(
                text = "${formatTime(session.elapsedSeconds)} / ${trimNumber(session.config.coffeeGrams)}g coffee / " +
                    "${BrewMath.totalWaterGrams(session.config.coffeeGrams, session.config.waterRatio)}g water",
                color = palette.mutedText,
            )

            Text("Rating", color = palette.phases.pour, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { value ->
                    FilterChip(
                        selected = rating == value,
                        onClick = { rating = if (rating == value) null else value },
                        label = { Text(value.toString()) },
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = { onSave(rating, notes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
            ) {
                Text("Save Brew", fontWeight = FontWeight.Bold)
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
