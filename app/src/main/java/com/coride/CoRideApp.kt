package com.coride

import android.app.Application

import com.coride.ui.common.ThemeHelper
import com.google.android.libraries.places.api.Places

class CoRideApp : Application() {
    
    companion object {
        lateinit var instance: CoRideApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.coride.data.local.LocalPreferences.init(this)
        ThemeHelper.init(this)
        
        // Initialize Google Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(this, "AIzaSyCvtDnZ4vdfUaqYkszjpzfLm_6LGGj3sco")
        }
    }
}


