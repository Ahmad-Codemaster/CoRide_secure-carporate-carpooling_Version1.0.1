package com.coride.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coride.R
import com.coride.ui.common.ThemeHelper

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeState(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
    }
}

