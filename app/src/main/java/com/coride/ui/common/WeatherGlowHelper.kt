package com.coride.ui.common

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat

object WeatherGlowHelper {

    fun startAuroraGlow(view: View) {
        // 1. Infinite Rotation
        val rotateAnimator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            duration = 3000
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
        }

        // 2. Pulse Scale (Pulsing Glow)
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.15f).apply {
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.15f).apply {
            duration = 1500
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }

        rotateAnimator.start()
        scaleX.start()
        scaleY.start()

        // 3. Dynamic Color Shifting
        if (view is ImageView) {
            val colors = intArrayOf(
                Color.parseColor("#1976D2"), // Primary Blue
                Color.parseColor("#00E5FF"), // Cyan Glow
                Color.parseColor("#D500F9"), // Pulsing Purple
                Color.parseColor("#1976D2")  // Back to Blue
            )

            val colorAnimator = ValueAnimator.ofArgb(*colors).apply {
                duration = 6000
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener { animator ->
                    val color = animator.animatedValue as Int
                    ImageViewCompat.setImageTintList(view, ColorStateList.valueOf(color))
                }
            }
            colorAnimator.start()
        }
    }
}

