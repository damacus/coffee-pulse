package com.damacus.coffeepulse.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.configDataStore by preferencesDataStore(name = "coffee_pulse_config")

class ConfigRepository(context: Context) {
    private val dataStore = context.applicationContext.configDataStore

    val config: Flow<BrewConfig> = dataStore.data.map { prefs ->
        BrewConfig(
            bloomSeconds = prefs[Keys.BLOOM_SECONDS] ?: 30,
            pulseIntervalSeconds = prefs[Keys.PULSE_INTERVAL_SECONDS] ?: 5,
            coffeeGrams = prefs[Keys.COFFEE_GRAMS] ?: 15.0,
            waterRatio = prefs[Keys.WATER_RATIO] ?: 15.5,
            themeId = prefs[Keys.THEME_ID] ?: "instrument",
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            countdownAudioEnabled = prefs[Keys.COUNTDOWN_AUDIO_ENABLED] ?: true,
            showCumulativeWeightTarget = prefs[Keys.SHOW_CUMULATIVE_WEIGHT_TARGET] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            advancedTastingWorkflow = prefs[Keys.ADVANCED_TASTING_WORKFLOW] ?: true,
        )
    }

    val activeSession: Flow<TimerSession?> = dataStore.data.map { prefs ->
        readSession(prefs)
    }

    suspend fun saveConfig(config: BrewConfig) {
        dataStore.edit { prefs ->
            prefs[Keys.BLOOM_SECONDS] = config.bloomSeconds
            prefs[Keys.PULSE_INTERVAL_SECONDS] = config.pulseIntervalSeconds
            prefs[Keys.COFFEE_GRAMS] = config.coffeeGrams
            prefs[Keys.WATER_RATIO] = config.waterRatio
            prefs[Keys.THEME_ID] = config.themeId
            prefs[Keys.SOUND_ENABLED] = config.soundEnabled
            prefs[Keys.HAPTICS_ENABLED] = config.hapticsEnabled
            prefs[Keys.COUNTDOWN_AUDIO_ENABLED] = config.countdownAudioEnabled
            prefs[Keys.SHOW_CUMULATIVE_WEIGHT_TARGET] = config.showCumulativeWeightTarget
            prefs[Keys.KEEP_SCREEN_ON] = config.keepScreenOn
            prefs[Keys.ADVANCED_TASTING_WORKFLOW] = config.advancedTastingWorkflow
        }
    }

    suspend fun saveActiveSession(session: TimerSession?) {
        dataStore.edit { prefs ->
            if (session == null || session.phase == TimerPhase.IDLE) {
                Keys.activeSessionKeys.forEach { prefs.remove(it) }
            } else {
                prefs[Keys.ACTIVE_PHASE] = session.phase.name
                prefs[Keys.ACTIVE_STARTED_AT] = session.startedAtMillis ?: 0L
                prefs[Keys.ACTIVE_PAUSED_ACCUMULATED] = session.pausedAccumulatedMillis
                prefs[Keys.ACTIVE_PAUSED_STARTED_AT] = session.pausedStartedAtMillis ?: 0L
                prefs[Keys.ACTIVE_LAST_CUE_ELAPSED] = session.lastCueElapsedMillis
                prefs[Keys.ACTIVE_LAST_COUNTDOWN_SECOND] = session.lastCountdownSecond
                prefs[Keys.ACTIVE_ELAPSED] = session.elapsedSeconds
                prefs[Keys.ACTIVE_REMAINING] = session.phaseRemainingSeconds
                prefs[Keys.ACTIVE_PROGRESS] = session.progress.toDouble()
                prefs[Keys.ACTIVE_RUNNING] = session.isRunning
                prefs[Keys.ACTIVE_BLOOM] = session.config.bloomSeconds
                prefs[Keys.ACTIVE_PULSE] = session.config.pulseIntervalSeconds
                prefs[Keys.ACTIVE_COFFEE] = session.config.coffeeGrams
                prefs[Keys.ACTIVE_RATIO] = session.config.waterRatio
                prefs[Keys.ACTIVE_THEME] = session.config.themeId
                prefs[Keys.ACTIVE_SOUND] = session.config.soundEnabled
                prefs[Keys.ACTIVE_HAPTICS] = session.config.hapticsEnabled
                prefs[Keys.ACTIVE_COUNTDOWN_AUDIO] = session.config.countdownAudioEnabled
                prefs[Keys.ACTIVE_SHOW_TARGET] = session.config.showCumulativeWeightTarget
                prefs[Keys.ACTIVE_KEEP_SCREEN_ON] = session.config.keepScreenOn
                prefs[Keys.ACTIVE_ADVANCED_TASTING] = session.config.advancedTastingWorkflow
            }
        }
    }

    private fun readSession(prefs: Preferences): TimerSession? {
        val phaseName = prefs[Keys.ACTIVE_PHASE] ?: return null
        val phase = runCatching { TimerPhase.valueOf(phaseName) }.getOrNull() ?: return null
        val config = BrewConfig(
            bloomSeconds = prefs[Keys.ACTIVE_BLOOM] ?: 30,
            pulseIntervalSeconds = prefs[Keys.ACTIVE_PULSE] ?: 5,
            coffeeGrams = prefs[Keys.ACTIVE_COFFEE] ?: 15.0,
            waterRatio = prefs[Keys.ACTIVE_RATIO] ?: 15.5,
            themeId = prefs[Keys.ACTIVE_THEME] ?: "instrument",
            soundEnabled = prefs[Keys.ACTIVE_SOUND] ?: true,
            hapticsEnabled = prefs[Keys.ACTIVE_HAPTICS] ?: true,
            countdownAudioEnabled = prefs[Keys.ACTIVE_COUNTDOWN_AUDIO] ?: true,
            showCumulativeWeightTarget = prefs[Keys.ACTIVE_SHOW_TARGET] ?: true,
            keepScreenOn = prefs[Keys.ACTIVE_KEEP_SCREEN_ON] ?: true,
            advancedTastingWorkflow = prefs[Keys.ACTIVE_ADVANCED_TASTING] ?: true,
        )
        if (phase == TimerPhase.IDLE) return TimerEngine.idle(config)
        return TimerSession(
            phase = phase,
            startedAtMillis = prefs[Keys.ACTIVE_STARTED_AT]?.takeIf { it > 0L },
            pausedAccumulatedMillis = prefs[Keys.ACTIVE_PAUSED_ACCUMULATED] ?: 0L,
            pausedStartedAtMillis = prefs[Keys.ACTIVE_PAUSED_STARTED_AT]?.takeIf { it > 0L },
            lastCueElapsedMillis = prefs[Keys.ACTIVE_LAST_CUE_ELAPSED] ?: -1L,
            lastCountdownSecond = prefs[Keys.ACTIVE_LAST_COUNTDOWN_SECOND] ?: -1,
            elapsedSeconds = prefs[Keys.ACTIVE_ELAPSED] ?: 0,
            phaseRemainingSeconds = prefs[Keys.ACTIVE_REMAINING] ?: config.bloomSeconds,
            progress = (prefs[Keys.ACTIVE_PROGRESS] ?: 1.0).toFloat(),
            isRunning = prefs[Keys.ACTIVE_RUNNING] ?: false,
            config = config,
        )
    }

    private object Keys {
        val BLOOM_SECONDS = intPreferencesKey("bloom_seconds")
        val PULSE_INTERVAL_SECONDS = intPreferencesKey("pulse_interval_seconds")
        val COFFEE_GRAMS = doublePreferencesKey("coffee_grams")
        val WATER_RATIO = doublePreferencesKey("water_ratio")
        val THEME_ID = stringPreferencesKey("theme_id")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val COUNTDOWN_AUDIO_ENABLED = booleanPreferencesKey("countdown_audio_enabled")
        val SHOW_CUMULATIVE_WEIGHT_TARGET = booleanPreferencesKey("show_cumulative_weight_target")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val ADVANCED_TASTING_WORKFLOW = booleanPreferencesKey("advanced_tasting_workflow")

        val ACTIVE_PHASE = stringPreferencesKey("active_phase")
        val ACTIVE_STARTED_AT = longPreferencesKey("active_started_at")
        val ACTIVE_PAUSED_ACCUMULATED = longPreferencesKey("active_paused_accumulated")
        val ACTIVE_PAUSED_STARTED_AT = longPreferencesKey("active_paused_started_at")
        val ACTIVE_LAST_CUE_ELAPSED = longPreferencesKey("active_last_cue_elapsed")
        val ACTIVE_LAST_COUNTDOWN_SECOND = intPreferencesKey("active_last_countdown_second")
        val ACTIVE_ELAPSED = intPreferencesKey("active_elapsed")
        val ACTIVE_REMAINING = intPreferencesKey("active_remaining")
        val ACTIVE_PROGRESS = doublePreferencesKey("active_progress")
        val ACTIVE_RUNNING = booleanPreferencesKey("active_running")
        val ACTIVE_BLOOM = intPreferencesKey("active_bloom")
        val ACTIVE_PULSE = intPreferencesKey("active_pulse")
        val ACTIVE_COFFEE = doublePreferencesKey("active_coffee")
        val ACTIVE_RATIO = doublePreferencesKey("active_ratio")
        val ACTIVE_THEME = stringPreferencesKey("active_theme")
        val ACTIVE_SOUND = booleanPreferencesKey("active_sound")
        val ACTIVE_HAPTICS = booleanPreferencesKey("active_haptics")
        val ACTIVE_COUNTDOWN_AUDIO = booleanPreferencesKey("active_countdown_audio")
        val ACTIVE_SHOW_TARGET = booleanPreferencesKey("active_show_target")
        val ACTIVE_KEEP_SCREEN_ON = booleanPreferencesKey("active_keep_screen_on")
        val ACTIVE_ADVANCED_TASTING = booleanPreferencesKey("active_advanced_tasting")

        val activeSessionKeys = listOf(
            ACTIVE_PHASE,
            ACTIVE_STARTED_AT,
            ACTIVE_PAUSED_ACCUMULATED,
            ACTIVE_PAUSED_STARTED_AT,
            ACTIVE_LAST_CUE_ELAPSED,
            ACTIVE_LAST_COUNTDOWN_SECOND,
            ACTIVE_ELAPSED,
            ACTIVE_REMAINING,
            ACTIVE_PROGRESS,
            ACTIVE_RUNNING,
            ACTIVE_BLOOM,
            ACTIVE_PULSE,
            ACTIVE_COFFEE,
            ACTIVE_RATIO,
            ACTIVE_THEME,
            ACTIVE_SOUND,
            ACTIVE_HAPTICS,
            ACTIVE_COUNTDOWN_AUDIO,
            ACTIVE_SHOW_TARGET,
            ACTIVE_KEEP_SCREEN_ON,
            ACTIVE_ADVANCED_TASTING,
        )
    }
}
