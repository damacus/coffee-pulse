package com.damacus.coffeepulse

import android.content.Context
import com.damacus.coffeepulse.data.ConfigRepository
import com.damacus.coffeepulse.data.history.BrewHistoryRepository
import com.damacus.coffeepulse.data.history.CoffeePulseDatabase
import com.damacus.coffeepulse.sensory.BrewAudioPlayer
import com.damacus.coffeepulse.sensory.BrewHaptics
import com.damacus.coffeepulse.service.TimerNotificationFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = CoffeePulseDatabase.create(appContext)

    val configRepository = ConfigRepository(appContext)
    val historyRepository = BrewHistoryRepository(database.brewHistoryDao())
    val audioPlayer = BrewAudioPlayer()
    val haptics = BrewHaptics(appContext)
    val notificationFactory = TimerNotificationFactory(appContext)
}
