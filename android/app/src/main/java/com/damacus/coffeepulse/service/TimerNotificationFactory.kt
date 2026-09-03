package com.damacus.coffeepulse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.SystemClock
import com.damacus.coffeepulse.MainActivity
import com.damacus.coffeepulse.R
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.domain.model.presentation

class TimerNotificationFactory(
    private val context: Context,
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.timer_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.timer_channel_description)
            setShowBadge(false)
        }
        manager?.createNotificationChannel(channel)
    }

    fun build(session: TimerSession): Notification {
        ensureChannel()

        val openIntent = PendingIntent.getActivity(
            context,
            REQUEST_OPEN_APP,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val label = session.phase.presentation().label
        val remaining = formatSeconds(session.phaseRemainingSeconds)
        val title = when {
            session.phase == TimerPhase.IDLE -> "Coffee Pulse"
            !session.isRunning -> "Timer paused • $remaining"
            else -> "$label • $remaining"
        }

        val targetWeight = if (session.config.showCumulativeWeightTarget) {
            val targetGrams = BrewMath.cumulativeTargetGrams(
                session.config.coffeeGrams,
                session.config.waterRatio,
                session.pulseIndex,
            )
            "Target: ${targetGrams}g on scale"
        } else {
            null
        }

        val text = listOfNotNull(
            when {
                session.phase == TimerPhase.IDLE -> session.phase.presentation().hint
                session.isRunning -> session.phase.presentation().hint
                else -> "Resume to continue ${label.lowercase()}"
            },
            targetWeight,
        ).joinToString(" • ")

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(context, CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(context)
        }

        builder
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setSubText("Coffee Pulse • ${formatSeconds(session.elapsedSeconds)}")
            .setContentIntent(openIntent)
            .setOngoing(session.isRunning)
            .setOnlyAlertOnce(true)
            .setShowWhen(session.isRunning)
            .setColor(phaseColor(session.phase))
            .setProgress(session.phaseDurationSeconds, session.phaseDurationSeconds - session.phaseRemainingSeconds, false)
            .addAction(
                notificationAction(
                    title = if (session.isRunning) "Pause" else "Resume",
                    action = if (session.isRunning) {
                        BrewTimerService.ACTION_PAUSE
                    } else {
                        BrewTimerService.ACTION_RESUME
                    },
                ),
            )
            .addAction(notificationAction("End brew", BrewTimerService.ACTION_FINISH))

        if (session.isRunning && session.startedAtMillis != null) {
            builder.setUsesChronometer(true)
            val baseTime = SystemClock.elapsedRealtime() - (session.elapsedSeconds * 1000L)
            builder.setWhen(baseTime)
        }

        progressStyle(session)?.let { builder.setStyle(it) }

        return builder.build()
    }

    private fun actionIntent(action: String): PendingIntent {
        val intent = Intent(context, BrewTimerService::class.java).setAction(action)
        return PendingIntent.getService(
            context,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    private fun notificationAction(title: String, action: String): Notification.Action {
        return Notification.Action.Builder(
            null,
            title,
            actionIntent(action),
        ).build()
    }

    private fun progressStyle(session: TimerSession): Notification.BigTextStyle? {
        val label = session.phase.presentation().label
        val remaining = formatSeconds(session.phaseRemainingSeconds)
        val elapsed = formatSeconds(session.elapsedSeconds)
        val totalWater = BrewMath.totalWaterGrams(session.config.coffeeGrams, session.config.waterRatio)
        val targetWeight = if (session.config.showCumulativeWeightTarget) {
            val targetGrams = BrewMath.cumulativeTargetGrams(
                session.config.coffeeGrams,
                session.config.waterRatio,
                session.pulseIndex,
            )
            "\nTarget water on scale: ${targetGrams}g / ${totalWater}g"
        } else {
            ""
        }
        val detail = "Phase: $label ($remaining)\nElapsed: $elapsed • ${session.config.coffeeGrams.toInt()}g coffee$targetWeight"
        return Notification.BigTextStyle().bigText(detail)
    }

    private fun phaseColor(phase: TimerPhase): Int {
        return when (phase) {
            TimerPhase.BLOOM -> Color.rgb(203, 137, 76)
            TimerPhase.POUR -> Color.rgb(180, 58, 48)
            TimerPhase.WAIT -> Color.rgb(78, 126, 118)
            TimerPhase.IDLE -> Color.rgb(122, 102, 88)
        }
    }

    private fun formatSeconds(seconds: Int): String {
        return "%d:%02d".format(seconds / 60, seconds % 60)
    }

    companion object {
        const val CHANNEL_ID = "coffee_pulse_timer"
        private const val REQUEST_OPEN_APP = 101
    }
}
