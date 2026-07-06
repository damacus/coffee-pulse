package com.damacus.coffeepulse.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.damacus.coffeepulse.CoffeePulseApplication
import com.damacus.coffeepulse.domain.BrewMath
import com.damacus.coffeepulse.domain.TimerEngine
import com.damacus.coffeepulse.domain.model.BrewConfig
import com.damacus.coffeepulse.domain.model.BrewHistoryEntry
import com.damacus.coffeepulse.domain.model.TimerPhase
import com.damacus.coffeepulse.domain.model.TimerSession
import com.damacus.coffeepulse.service.BrewTimerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class BrewViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as CoffeePulseApplication).container
    private val appContext = application.applicationContext
    private val _uiState = MutableStateFlow(BrewUiState())
    val uiState: StateFlow<BrewUiState> = _uiState.asStateFlow()

    private var tickerJob: Job? = null

    init {
        viewModelScope.launch {
            val config = container.configRepository.config.first()
            val restored = container.configRepository.activeSession.first()
            _uiState.update {
                val session = restored?.let { restoredSession ->
                    TimerEngine.snapshot(restoredSession, System.currentTimeMillis()).session
                } ?: TimerEngine.idle(config)
                it.copy(config = session.config, session = session)
            }
            if (restored?.isRunning == true) startTicker()
        }

        viewModelScope.launch {
            container.historyRepository.entries.collect { entries ->
                _uiState.update { it.copy(history = entries) }
            }
        }
    }

    fun startBrew() {
        val config = _uiState.value.config
        val wasIdle = _uiState.value.session.phase == TimerPhase.IDLE
        val nowMillis = System.currentTimeMillis()
        val next = if (wasIdle) {
            TimerEngine.start(config, nowMillis)
        } else {
            TimerEngine.resume(_uiState.value.session, nowMillis)
        }
        _uiState.update { it.copy(session = next, config = next.config) }
        if (wasIdle) {
            BrewTimerService.start(appContext, config)
        } else {
            sendServiceAction(BrewTimerService.ACTION_RESUME)
        }
        persistSession(next)
        startTicker()
    }

    fun pauseBrew() {
        val next = TimerEngine.pause(_uiState.value.session, System.currentTimeMillis())
        _uiState.update { it.copy(session = next) }
        sendServiceAction(BrewTimerService.ACTION_PAUSE)
        persistSession(next)
        tickerJob?.cancel()
    }

    fun resetBrew() {
        tickerJob?.cancel()
        val next = TimerEngine.reset(_uiState.value.session)
        _uiState.update { it.copy(session = next, pendingFinish = null) }
        container.haptics.cancel()
        BrewTimerService.stop(appContext)
        persistSession(null)
    }

    fun requestFinish() {
        val session = _uiState.value.session
        if (session.phase != TimerPhase.IDLE) {
            pauseBrew()
            _uiState.update { it.copy(pendingFinish = session.copy(isRunning = false)) }
        }
    }

    fun dismissFinish() {
        _uiState.update { it.copy(pendingFinish = null) }
    }

    fun saveFinishedBrew(rating: Int?, notes: String) {
        val session = _uiState.value.pendingFinish ?: return
        val finishedAt = System.currentTimeMillis()
        val startedAt = session.startedAtMillis ?: (finishedAt - session.elapsedSeconds * 1_000L)
        val entry = BrewHistoryEntry(
            id = UUID.randomUUID().toString(),
            startedAtMillis = startedAt,
            finishedAtMillis = finishedAt,
            totalSeconds = session.elapsedSeconds,
            bloomSeconds = session.config.bloomSeconds,
            pulseIntervalSeconds = session.config.pulseIntervalSeconds,
            coffeeGrams = session.config.coffeeGrams,
            waterRatio = session.config.waterRatio,
            totalWaterGrams = BrewMath.totalWaterGrams(session.config.coffeeGrams, session.config.waterRatio),
            themeId = session.config.themeId,
            rating = rating,
            notes = notes.trim(),
        )
        viewModelScope.launch {
            container.historyRepository.save(entry)
            resetBrew()
            container.haptics.finish(session.config.hapticsEnabled)
        }
    }

    fun saveConfig(config: BrewConfig) {
        viewModelScope.launch {
            container.configRepository.saveConfig(config)
            _uiState.update { state ->
                val nextSession = if (state.session.phase == TimerPhase.IDLE) {
                    TimerEngine.idle(config)
                } else {
                    state.session.copy(config = config)
                }
                state.copy(config = config, session = nextSession)
            }
        }
    }

    fun toggleSound() {
        val next = _uiState.value.config.copy(soundEnabled = !_uiState.value.config.soundEnabled)
        saveConfig(next)
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val current = _uiState.value.session
                if (!current.isRunning) break
                val nowMillis = System.currentTimeMillis()
                val transition = TimerEngine.snapshot(current, nowMillis)
                _uiState.update { it.copy(session = transition.session) }
                persistSession(transition.session)
                delay(TimerEngine.millisUntilNextUpdate(transition.session, System.currentTimeMillis()))
            }
        }
    }

    private fun persistSession(session: TimerSession?) {
        viewModelScope.launch {
            container.configRepository.saveActiveSession(session)
        }
    }

    private fun sendServiceAction(action: String) {
        appContext.startService(Intent(appContext, BrewTimerService::class.java).setAction(action))
    }
}
