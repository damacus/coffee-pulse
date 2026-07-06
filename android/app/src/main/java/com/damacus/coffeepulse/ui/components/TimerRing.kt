package com.damacus.coffeepulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun TimerRing(
    session: TimerSession,
    palette: CoffeePulsePalette,
    phaseColor: Color,
    modifier: Modifier = Modifier,
) {
    val tickScale by animateFloatAsState(
        targetValue = when {
            !session.isRunning -> 1f
            session.phaseRemainingSeconds <= 3 -> if (session.phaseRemainingSeconds % 2 == 0) 1.08f else 0.99f
            session.phaseRemainingSeconds % 2 == 0 -> 1.035f
            else -> 0.995f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "timer-tick-bounce",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = session.progress,
        animationSpec = tween(durationMillis = 620, easing = FastOutSlowInEasing),
        label = "timer-progress",
    )
    val animatedPhaseColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(durationMillis = 420, easing = LinearEasing),
        label = "timer-phase-color",
    )
    val phaseGlow by animateFloatAsState(
        targetValue = when {
            !session.isRunning -> 0f
            session.phaseRemainingSeconds <= 3 -> 0.22f
            else -> 0.12f
        },
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "timer-phase-glow",
    )

    Box(
        modifier = modifier.graphicsLayer {
            scaleX = tickScale
            scaleY = tickScale
        },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val side = min(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = side * 0.39f
            val tickOuter = side * 0.48f
            val tickMajorInner = side * 0.43f
            val tickMinorInner = side * 0.46f

            drawCircle(
                color = animatedPhaseColor.copy(alpha = 0.10f + phaseGlow),
                radius = side * 0.44f,
                center = center,
            )
            drawCircle(
                color = animatedPhaseColor.copy(alpha = 0.06f + phaseGlow),
                radius = side * (0.47f + phaseGlow * 0.03f),
                center = center,
            )

            repeat(60) { index ->
                val angle = Math.toRadians((index * 6 - 90).toDouble())
                val major = index % 5 == 0
                val outer = Offset(
                    center.x + tickOuter * cos(angle).toFloat(),
                    center.y + tickOuter * sin(angle).toFloat(),
                )
                val innerRadius = if (major) tickMajorInner else tickMinorInner
                val inner = Offset(
                    center.x + innerRadius * cos(angle).toFloat(),
                    center.y + innerRadius * sin(angle).toFloat(),
                )
                drawLine(
                    color = if (major) {
                        animatedPhaseColor.copy(alpha = 0.78f)
                    } else {
                        animatedPhaseColor.copy(alpha = 0.24f + phaseGlow * 0.16f)
                    },
                    start = outer,
                    end = inner,
                    strokeWidth = if (major) 2.4f else 1.4f,
                    cap = StrokeCap.Round,
                )
            }

            drawCircle(
                color = palette.text.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style = Stroke(width = side * 0.026f, cap = StrokeCap.Round),
            )

            val arcSize = Size(radius * 2f, radius * 2f)
            drawArc(
                color = animatedPhaseColor.copy(alpha = 0.18f + phaseGlow),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = arcSize,
                style = Stroke(width = side * 0.052f, cap = StrokeCap.Round),
            )
            drawArc(
                color = animatedPhaseColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = arcSize,
                style = Stroke(width = side * 0.028f, cap = StrokeCap.Round),
            )

            if (!session.isIdle) {
                val sparkAngle = Math.toRadians((-90f + animatedProgress * 360f).toDouble())
                val spark = Offset(
                    center.x + radius * cos(sparkAngle).toFloat(),
                    center.y + radius * sin(sparkAngle).toFloat(),
                )
                drawCircle(
                    color = palette.text.copy(alpha = 0.82f),
                    radius = side * (0.014f + phaseGlow * 0.012f),
                    center = spark,
                )
            }

            drawCircle(
                color = animatedPhaseColor.copy(alpha = 0.54f),
                radius = side * 0.013f,
                center = center,
            )
        }

        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = when (session.phase) {
                    TimerPhase.POUR -> Icons.Default.LocalDrink
                    TimerPhase.WAIT -> Icons.Default.HourglassBottom
                    else -> Icons.Default.Coffee
                },
                contentDescription = null,
                tint = animatedPhaseColor,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = if (session.isIdle) {
                    session.config.bloomSeconds.toString()
                } else {
                    session.phaseRemainingSeconds.toString()
                },
                color = palette.text,
                fontSize = 86.sp,
                lineHeight = 86.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "seconds",
                color = palette.mutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
