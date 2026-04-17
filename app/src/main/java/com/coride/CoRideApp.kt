package com.coride

import android.app.Application

import com.coride.ui.common.ThemeHelper

class CoRideApp : Application() {
    
    companion object {
        lateinit var instance: CoRideApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ThemeHelper.init(this)
    }
}


