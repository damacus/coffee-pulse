package com.damacus.coffeepulse

import android.app.Application

class CoffeePulseApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
