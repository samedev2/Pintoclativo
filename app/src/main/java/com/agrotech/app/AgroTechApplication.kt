package com.agrotech.app

import android.app.Application
import com.agrotech.app.di.AppContainer
import com.agrotech.app.di.DefaultAppContainer

class AgroTechApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
