package com.indrive.clone

import android.app.Application

import com.indrive.clone.ui.common.ThemeHelper

class InDriveApp : Application() {
    
    companion object {
        lateinit var instance: InDriveApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        ThemeHelper.init(this)
    }
}
