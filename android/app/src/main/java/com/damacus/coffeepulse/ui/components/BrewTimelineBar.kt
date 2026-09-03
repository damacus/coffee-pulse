package com.damacus.coffeepulse.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.ui.theme.CoffeePulsePalette

@Composable
fun BrewTimelineBar(
    session: TimerSession,
    palette: CoffeePulsePalette,
    modifier: Modifier = Modifier,
) {
    val bloomSeconds = session.config.bloomSeconds.coerceAtLeast(1)
    val pulseSeconds = session.config.pulseIntervalSeconds.coerceAtLeast(1)
    // 1 bloom segment + 4 alternating pulse/wait segments
    val totalTargetDuration = bloomSeconds + pulseSeconds * 4
    val overallProgress = (session.elapsedSeconds.toFloat() / totalTargetDuration.toFloat()).coerceIn(0f, 1f)
    val animatedOverallProgress by animateFloatAsState(
        targetValue = overallProgress,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "overallTimelineProgress",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(palette.surface.copy(alpha = 0.85f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "TIMELINE",
                color = palette.phases.pour,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = when {
                    session.isIdle -> "Ready"
                    session.phase == TimerPhase.BLOOM -> "Bloom (${session.elapsedSeconds}s / ${bloomSeconds}s)"
                    else -> "Pulse #${session.pulseIndex} (${session.phase.name})"
                },
                color = palette.mutedText,
                fontSize = 11.sp,
            )
        }

        // Multi-segment timeline bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(palette.background.copy(alpha = 0.5f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // Bloom segment
                Box(
                    modifier = Modifier
                        .weight(bloomSeconds.toFloat())
                        .fillMaxHeight()
                        .background(palette.phases.bloom.copy(alpha = if (session.phase == TimerPhase.BLOOM) 0.8f else 0.35f)),
                )
                // Pulses 1 to 4
                repeat(4) { index ->
                    val isPour = index % 2 == 0
                    val segColor = if (isPour) palette.phases.pour else palette.phases.wait
                    val isCurrentSegment = session.pulseIndex == (index + 1)
                    Box(
                        modifier = Modifier
                            .weight(pulseSeconds.toFloat())
                            .fillMaxHeight()
                            .background(segColor.copy(alpha = if (isCurrentSegment) 0.8f else 0.35f)),
                    )
                }
            }

            // Playhead indicator
            if (!session.isIdle && animatedOverallProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedOverallProgress)
                        .fillMaxHeight()
                        .background(Color.White.copy(alpha = 0.35f)),
                )
            }
        }
    }
}
