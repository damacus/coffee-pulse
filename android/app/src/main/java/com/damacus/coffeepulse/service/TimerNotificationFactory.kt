package com.damacus.coffeepulse.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import com.damacus.coffeepulse.MainActivity
import com.damacus.coffeepulse.R
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
        val title = if (session.phase == TimerPhase.IDLE) "Coffee Pulse" else "$label - $remaining"
        val text = if (session.isRunning) {
            session.phase.presentation().hint
        } else {
            "Paused at $remaining"
        }

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
            .setSubText("Coffee Pulse")
            .setContentIntent(openIntent)
            .setOngoing(session.isRunning)
            .setOnlyAlertOnce(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setColor(phaseColor(session.phase))
            .setProgress(session.phaseDurationSeconds, session.phaseDurationSeconds - session.phaseRemainingSeconds, false)
            .addAction(
                0,
                if (session.isRunning) "Pause" else "Resume",
                actionIntent(if (session.isRunning) BrewTimerService.ACTION_PAUSE else BrewTimerService.ACTION_RESUME),
            )
            .addAction(0, "Finish", actionIntent(BrewTimerService.ACTION_FINISH))
            .addAction(0, "Reset", actionIntent(BrewTimerService.ACTION_RESET))

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

    private fun progressStyle(session: TimerSession): Notification.Style? {
        if (Build.VERSION.SDK_INT < 36 || session.phase == TimerPhase.IDLE) return null
        return runCatching {
            val styleClass = Class.forName("android.app.Notification\$ProgressStyle")
            val style = styleClass.getDeclaredConstructor().newInstance() as Notification.Style
            styleClass.methods.firstOrNull { method ->
                method.name == "setProgress" && method.parameterTypes.size == 1
            }?.invoke(style, session.phaseDurationSeconds - session.phaseRemainingSeconds)
            styleClass.methods.firstOrNull { method ->
                method.name == "setStyledByProgress" && method.parameterTypes.size == 1
            }?.invoke(style, false)
            style
        }.getOrNull()
    }

    private fun phaseColor(phase: TimerPhase): Int = when (phase) {
        TimerPhase.IDLE -> Color.rgb(176, 168, 156)
        TimerPhase.BLOOM -> Color.rgb(136, 180, 224)
        TimerPhase.POUR -> Color.rgb(232, 148, 58)
        TimerPhase.WAIT -> Color.rgb(106, 174, 126)
    }

    private fun formatSeconds(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%d:%02d".format(mins, secs)
    }

    companion object {
        const val CHANNEL_ID = "active_brew_timer"
        private const val REQUEST_OPEN_APP = 1001
    }
}
