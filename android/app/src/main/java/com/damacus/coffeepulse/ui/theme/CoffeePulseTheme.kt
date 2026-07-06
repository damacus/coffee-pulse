package com.damacus.coffeepulse.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

data class PhaseColors(
    val idle: Color,
    val bloom: Color,
    val pour: Color,
    val wait: Color,
)

data class CoffeePulsePalette(
    val id: String,
    val name: String,
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,
    val text: Color,
    val mutedText: Color,
    val phases: PhaseColors,
)

val CoffeePulsePalettes = listOf(
    CoffeePulsePalette(
        id = "instrument",
        name = "Instrument",
        background = Color(0xFF0B0704),
        surface = Color(0xFF17100B),
        surfaceHigh = Color(0xFF211811),
        text = Color(0xFFF5E6C8),
        mutedText = Color(0x99F5E6C8),
        phases = PhaseColors(
            idle = Color(0xFFB0A89C),
            bloom = Color(0xFF88B4E0),
            pour = Color(0xFFE8943A),
            wait = Color(0xFF6AAE7E),
        ),
    ),
    CoffeePulsePalette(
        id = "slate",
        name = "Slate",
        background = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        surfaceHigh = Color(0xFF202833),
        text = Color(0xFFC9D1D9),
        mutedText = Color(0x99C9D1D9),
        phases = PhaseColors(
            idle = Color(0xFF8B9DC3),
            bloom = Color(0xFF58A6FF),
            pour = Color(0xFF3FB950),
            wait = Color(0xFFE3B341),
        ),
    ),
    CoffeePulsePalette(
        id = "matcha",
        name = "Matcha",
        background = Color(0xFF0A0F0A),
        surface = Color(0xFF111811),
        surfaceHigh = Color(0xFF1B241B),
        text = Color(0xFFD4E8C4),
        mutedText = Color(0x99D4E8C4),
        phases = PhaseColors(
            idle = Color(0xFF7AAD7A),
            bloom = Color(0xFFA8D4A8),
            pour = Color(0xFFD4C850),
            wait = Color(0xFF5AAA8A),
        ),
    ),
    CoffeePulsePalette(
        id = "night",
        name = "Night",
        background = Color(0xFF050810),
        surface = Color(0xFF0C1020),
        surfaceHigh = Color(0xFF171D33),
        text = Color(0xFFE0E8F8),
        mutedText = Color(0x99E0E8F8),
        phases = PhaseColors(
            idle = Color(0xFF7B8FC4),
            bloom = Color(0xFFC084FC),
            pour = Color(0xFFF472B6),
            wait = Color(0xFF38BDF8),
        ),
    ),
)

fun paletteFor(themeId: String): CoffeePulsePalette {
    return CoffeePulsePalettes.firstOrNull { it.id == themeId } ?: CoffeePulsePalettes.first()
}

fun CoffeePulsePalette.phaseColor(phaseName: String): Color {
    return when (phaseName) {
        "BLOOM" -> phases.bloom
        "POUR" -> phases.pour
        "WAIT" -> phases.wait
        else -> phases.idle
    }
}

@Composable
fun CoffeePulseTheme(
    palette: CoffeePulsePalette,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(context)
    } else {
        darkColorScheme()
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = palette.background,
            surface = palette.surface,
            surfaceContainer = palette.surfaceHigh,
            surfaceContainerLow = systemScheme.surfaceContainerLow.copy(alpha = 0.72f),
            surfaceContainerHigh = palette.surfaceHigh,
            surfaceVariant = systemScheme.surfaceVariant.copy(alpha = 0.82f),
            primary = palette.phases.pour,
            onPrimary = palette.background,
            onBackground = palette.text,
            onSurface = palette.text,
            onSurfaceVariant = palette.mutedText,
            secondary = palette.phases.bloom,
            onSecondary = palette.background,
            tertiary = palette.phases.wait,
            onTertiary = palette.background,
            outline = systemScheme.outline.copy(alpha = 0.54f),
            outlineVariant = systemScheme.outlineVariant.copy(alpha = 0.46f),
        ),
        typography = CoffeePulseTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(6.dp),
            small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp),
            extraLarge = RoundedCornerShape(16.dp),
        ),
        content = content,
    )
}
