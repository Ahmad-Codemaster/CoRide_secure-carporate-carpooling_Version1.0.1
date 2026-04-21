package com.coride.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.coride.R
import com.coride.data.repository.MockDataRepository
import com.coride.ui.auth.AuthActivity
import com.coride.ui.common.SpringPhysicsHelper
import com.coride.ui.common.ThemeHelper
import com.coride.ui.main.MainActivity

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.applyThemeState(this)
        super.onCreate(savedInstanceState)
        com.coride.data.local.LocalPreferences.init(this)
        setContentView(R.layout.activity_splash)

        val heroContainer = findViewById<View>(R.id.heroContainer)
        val tvAppName = findViewById<View>(R.id.tvAppName)
        val tvTagline = findViewById<View>(R.id.tvTagline)
        val progressTrack = findViewById<View>(R.id.progressTrack)
        val progressFilling = findViewById<View>(R.id.progressFilling)

        // ── Hero Circle — M3 Expressive spatial spring (bouncy scale up) ──
        SpringPhysicsHelper.springScale(
            heroContainer,
            finalScale = 1f,
            stiffness = 700f,        // Fast expressive
            dampingRatio = 0.50f,    // Bouncy overshoot
            startDelay = 80L
        )
        SpringPhysicsHelper.springAlpha(
            heroContainer, 1f,
            startDelay = 80L
        )

        // ── App Name — spring slide up with stagger ──
        SpringPhysicsHelper.springSlideUpFadeIn(
            tvAppName,
            stiffness = 500f,
            dampingRatio = 0.72f,
            startDelay = 250L
        )

        // ── Tagline — staggered slide up ──
        SpringPhysicsHelper.springSlideUpFadeIn(
            tvTagline,
            stiffness = 450f,
            dampingRatio = 0.75f,
            startDelay = 380L
        )

        // ── Progress Bar — fade in and fill ──
        SpringPhysicsHelper.springAlpha(
            progressTrack, 1f,
            startDelay = 550L
        )

        // ── Smooth Filling Animation (ValueAnimator) ──
        android.animation.ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2400
            startDelay = 600
            interpolator = android.view.animation.LinearInterpolator()
            addUpdateListener { animator ->
                progressFilling.scaleX = animator.animatedValue as Float
            }
            start()
        }

        // ── Navigate after 2.8s ──
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (MockDataRepository.isLoggedIn()) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, AuthActivity::class.java)
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2800)
    }


}

