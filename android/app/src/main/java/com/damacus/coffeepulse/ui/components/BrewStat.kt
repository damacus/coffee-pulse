package com.damacus.coffeepulse.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

@Composable
fun BrewStat(
    label: String,
    value: String,
    palette: CoffeePulsePalette,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = palette.mutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = value,
            color = palette.text,
            fontSize = 30.sp,
            fontWeight = FontWeight.Light,
        )
    }
}
