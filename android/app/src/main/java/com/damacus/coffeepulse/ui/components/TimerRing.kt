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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.domain.model.presentation
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette
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
    val finalCountdownScale by animateFloatAsState(
        targetValue = when {
            !session.isRunning || session.phaseRemainingSeconds > 3 -> 1f
            session.phaseRemainingSeconds % 2 == 0 -> 1.04f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "timer-final-countdown",
    )
    val animatedProgress by animateFloatAsState(
        targetValue = session.progress,
        animationSpec = tween(durationMillis = 320, easing = LinearEasing),
        label = "timer-progress",
    )
    val animatedPhaseColor by animateColorAsState(
        targetValue = phaseColor,
        animationSpec = tween(durationMillis = 260, easing = LinearEasing),
        label = "timer-phase-color",
    )
    val phaseGlow by animateFloatAsState(
        targetValue = when {
            !session.isRunning -> 0f
            session.phaseRemainingSeconds <= 3 -> 0.18f
            else -> 0.08f
        },
        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
        label = "timer-phase-glow",
    )
    val dialVectors = remember {
        List(12) { index ->
            val angle = Math.toRadians((index * 30 - 90).toDouble())
            Offset(cos(angle).toFloat(), sin(angle).toFloat())
        }
    }
    val seconds = if (session.isIdle) session.config.bloomSeconds else session.phaseRemainingSeconds

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = finalCountdownScale
                scaleY = finalCountdownScale
            }
            .semantics {
                contentDescription = if (session.isIdle) {
                    "$seconds second bloom timer"
                } else {
                    "${session.phase.presentation().label}, $seconds seconds remaining"
                }
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = 1f - session.progress,
                    range = 0f..1f,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val side = min(size.width, size.height)
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = side * 0.39f
            val tickOuter = side * 0.48f
            val tickInner = side * 0.445f

            drawCircle(
                color = animatedPhaseColor.copy(alpha = 0.08f + phaseGlow),
                radius = side * 0.46f,
                center = center,
            )

            dialVectors.forEach { vector ->
                drawLine(
                    color = animatedPhaseColor.copy(alpha = 0.62f),
                    start = Offset(
                        center.x + tickOuter * vector.x,
                        center.y + tickOuter * vector.y,
                    ),
                    end = Offset(
                        center.x + tickInner * vector.x,
                        center.y + tickInner * vector.y,
                    ),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round,
                )
            }

            drawCircle(
                color = palette.text.copy(alpha = 0.08f),
                radius = radius,
                center = center,
                style = Stroke(width = side * 0.032f, cap = StrokeCap.Round),
            )

            val arcSize = Size(radius * 2f, radius * 2f)
            drawArc(
                color = animatedPhaseColor.copy(alpha = 0.22f + phaseGlow),
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = arcSize,
                style = Stroke(width = side * 0.056f, cap = StrokeCap.Round),
            )
            drawArc(
                color = animatedPhaseColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = arcSize,
                style = Stroke(width = side * 0.029f, cap = StrokeCap.Round),
            )

            if (!session.isIdle) {
                val sparkAngle = Math.toRadians((-90f + animatedProgress * 360f).toDouble())
                val spark = Offset(
                    center.x + radius * cos(sparkAngle).toFloat(),
                    center.y + radius * sin(sparkAngle).toFloat(),
                )
                drawCircle(
                    color = palette.text.copy(alpha = 0.88f),
                    radius = side * 0.015f,
                    center = spark,
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = seconds.toString(),
                modifier = Modifier
                    .fillMaxWidth(0.58f)
                    .height(92.dp),
                color = palette.text,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 42.sp,
                    maxFontSize = 82.sp,
                    stepSize = 2.sp,
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "SECONDS",
                color = palette.mutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
