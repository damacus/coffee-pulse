package com.damacus.coffeepulse.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.damacus.coffeepulse.CoffeePulseApplication
import com.damacus.coffeepulse.data.ConfigRepository
import com.damacus.coffeepulse.domain.TimerCue
import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.sensory.BrewAudioPlayer
import com.damacus.coffeepulse.sensory.BrewHaptics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BrewTimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var tickerJob: Job? = null
    private var session: TimerSession? = null
    private lateinit var notificationFactory: TimerNotificationFactory
    private lateinit var configRepository: ConfigRepository
    private lateinit var audioPlayer: BrewAudioPlayer
    private lateinit var haptics: BrewHaptics

    override fun onCreate() {
        super.onCreate()
        val container = (application as CoffeePulseApplication).container
        notificationFactory = container.notificationFactory
        configRepository = container.configRepository
        audioPlayer = container.audioPlayer
        haptics = container.haptics
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startFromIntent(intent)
            ACTION_PAUSE -> updateSession { TimerEngine.pause(it, System.currentTimeMillis()) }
            ACTION_RESUME -> updateSession { TimerEngine.resume(it, System.currentTimeMillis()) }
            ACTION_RESET -> stopTimer(cancelHaptics = true)
            ACTION_FINISH -> finishTimer()
            else -> session?.let { publish(it) } ?: run {
                scope.launch {
                    val restored = configRepository.activeSession.first()
                    if (restored != null && restored.phase != TimerPhase.IDLE) {
                        session = if (restored.isRunning) {
                            TimerEngine.snapshot(restored, System.currentTimeMillis()).session
                        } else {
                            restored
                        }
                        publish(session!!)
                        if (session!!.isRunning) {
                            startTicker()
                        }
                    } else {
                        stopSelf()
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun startFromIntent(intent: Intent) {
        val config = BrewConfig(
            bloomSeconds = intent.getIntExtra(EXTRA_BLOOM, 30),
            pulseIntervalSeconds = intent.getIntExtra(EXTRA_PULSE, 5),
            coffeeGrams = intent.getDoubleExtra(EXTRA_COFFEE, 15.0),
            waterRatio = intent.getDoubleExtra(EXTRA_RATIO, 15.5),
            themeId = intent.getStringExtra(EXTRA_THEME) ?: "instrument",
            soundEnabled = intent.getBooleanExtra(EXTRA_SOUND, true),
            hapticsEnabled = intent.getBooleanExtra(EXTRA_HAPTICS, true),
            countdownAudioEnabled = intent.getBooleanExtra(EXTRA_COUNTDOWN_AUDIO, true),
            showCumulativeWeightTarget = intent.getBooleanExtra(EXTRA_SHOW_TARGET, true),
            keepScreenOn = intent.getBooleanExtra(EXTRA_KEEP_SCREEN_ON, true),
            advancedTastingWorkflow = intent.getBooleanExtra(EXTRA_ADVANCED_TASTING, true),
        )
        session = TimerEngine.start(config, System.currentTimeMillis())
        haptics.start(config.hapticsEnabled)
        publish(session ?: return)
        persist(session)
        startTicker()
    }

    private fun updateSession(transform: (TimerSession) -> TimerSession) {
        val current = session
        if (current != null) {
            applySessionUpdate(transform(current))
        } else {
            scope.launch {
                val restored = configRepository.activeSession.first()
                if (restored != null && restored.phase != TimerPhase.IDLE) {
                    val base = if (restored.isRunning) {
                        TimerEngine.snapshot(restored, System.currentTimeMillis()).session
                    } else {
                        restored
                    }
                    applySessionUpdate(transform(base))
                } else {
                    stopSelf()
                }
            }
        }
    }

    private fun applySessionUpdate(newSession: TimerSession) {
        session = newSession
        publish(newSession)
        persist(newSession)
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        val current = session ?: return
        if (!current.isRunning) return

        tickerJob = scope.launch {
            var lastPersistedSecond = -1
            while (isActive) {
                val current = session ?: break
                if (!current.isRunning) break
                val nowMillis = System.currentTimeMillis()
                val next = TimerEngine.snapshot(current, nowMillis)
                session = next.session
                handleCues(next.cue, next.countdownSecondCue, next.session.config)
                publish(next.session)

                // Only persist on integer second changes or boundary cues
                if (next.session.elapsedSeconds != lastPersistedSecond || next.cue != null) {
                    lastPersistedSecond = next.session.elapsedSeconds
                    persist(next.session)
                }
                delay(TimerEngine.millisUntilNextUpdate(next.session, System.currentTimeMillis()))
            }
        }
    }

    private fun publish(session: TimerSession) {
        val notification = notificationFactory.build(session)
        if (session.elapsedSeconds == 0 && session.isRunning) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            getSystemService(NotificationManager::class.java)?.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun finishTimer() {
        session?.config?.let { config ->
            haptics.finish(config.hapticsEnabled)
        }
        stopTimer(cancelHaptics = false)
    }

    private fun stopTimer(cancelHaptics: Boolean) {
        tickerJob?.cancel()
        tickerJob = null
        session = null
        if (cancelHaptics) haptics.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        scope.launch {
            configRepository.saveActiveSession(null)
            stopSelf()
        }
    }

    private fun handleCues(cue: TimerCue?, countdownCue: Int?, config: BrewConfig) {
        if (countdownCue != null && config.soundEnabled && config.countdownAudioEnabled) {
            audioPlayer.playCountdownPip(countdownCue, true)
        }
        when (cue) {
            TimerCue.BLOOM_COMPLETE -> {
                audioPlayer.playBloomArpeggio(config.soundEnabled)
                haptics.bloomComplete(config.hapticsEnabled)
            }
            TimerCue.POUR_COMPLETE -> {
                audioPlayer.playLowPing(config.soundEnabled)
                haptics.pourComplete(config.hapticsEnabled)
            }
            TimerCue.WAIT_COMPLETE -> {
                audioPlayer.playHighPing(config.soundEnabled)
                haptics.waitComplete(config.hapticsEnabled)
            }
            null -> Unit
        }
    }

    private fun persist(session: TimerSession?) {
        scope.launch {
            configRepository.saveActiveSession(session)
        }
    }

    companion object {
        const val ACTION_START = "com.damacus.coffeepulse.action.START"
        const val ACTION_PAUSE = "com.damacus.coffeepulse.action.PAUSE"
        const val ACTION_RESUME = "com.damacus.coffeepulse.action.RESUME"
        const val ACTION_RESET = "com.damacus.coffeepulse.action.RESET"
        const val ACTION_FINISH = "com.damacus.coffeepulse.action.FINISH"

        private const val EXTRA_BLOOM = "bloom"
        private const val EXTRA_PULSE = "pulse"
        private const val EXTRA_COFFEE = "coffee"
        private const val EXTRA_RATIO = "ratio"
        private const val EXTRA_THEME = "theme"
        private const val EXTRA_SOUND = "sound"
        private const val EXTRA_HAPTICS = "haptics"
        private const val EXTRA_COUNTDOWN_AUDIO = "countdown_audio"
        private const val EXTRA_SHOW_TARGET = "show_target"
        private const val EXTRA_KEEP_SCREEN_ON = "keep_screen_on"
        private const val EXTRA_ADVANCED_TASTING = "advanced_tasting"

        private const val NOTIFICATION_ID = 404

        fun start(context: Context, config: BrewConfig) {
            val intent = Intent(context, BrewTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_BLOOM, config.bloomSeconds)
                putExtra(EXTRA_PULSE, config.pulseIntervalSeconds)
                putExtra(EXTRA_COFFEE, config.coffeeGrams)
                putExtra(EXTRA_RATIO, config.waterRatio)
                putExtra(EXTRA_THEME, config.themeId)
                putExtra(EXTRA_SOUND, config.soundEnabled)
                putExtra(EXTRA_HAPTICS, config.hapticsEnabled)
                putExtra(EXTRA_COUNTDOWN_AUDIO, config.countdownAudioEnabled)
                putExtra(EXTRA_SHOW_TARGET, config.showCumulativeWeightTarget)
                putExtra(EXTRA_KEEP_SCREEN_ON, config.keepScreenOn)
                putExtra(EXTRA_ADVANCED_TASTING, config.advancedTastingWorkflow)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.startService(Intent(context, BrewTimerService::class.java).setAction(ACTION_RESET))
        }

        fun finish(context: Context) {
            context.startService(Intent(context, BrewTimerService::class.java).setAction(ACTION_FINISH))
        }
    }
}
