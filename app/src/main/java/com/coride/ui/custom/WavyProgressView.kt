package com.coride.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.color.MaterialColors
import kotlin.math.PI
import kotlin.math.sin

/**
 * A Material 3 Expressive inspired Wavy Progress Indicator.
 * Supports determinate progress (filling) with a wavy active part.
 */
class WavyProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val path = Path()
    private var phase = 0f
    private var phaseAnimator: ValueAnimator? = null

    // Progress (0.0 to 1.0)
    var progress = 0.3f // Default for preview
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    // Configurable properties
    var waveAmplitude = 12f
    var waveFrequency = 1.5f 
    var waveSpeed = 1.2f
    var strokeThickness = 12f
    var indicatorColor = Color.WHITE
    var trackColor = Color.parseColor("#33FFFFFF")

    init {
        indicatorColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE)
        trackColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
        trackColor = Color.argb(40, Color.red(trackColor), Color.green(trackColor), Color.blue(trackColor))
        
        startPhaseAnimation()
    }

    private fun startPhaseAnimation() {
        phaseAnimator?.cancel()
        phaseAnimator = ValueAnimator.ofFloat(0f, (2 * PI).toFloat()).apply {
            duration = (2500 / waveSpeed).toLong()
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                phase = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()
        val centerY = h / 2f
        val startX = strokeThickness / 2f
        val endX = w - strokeThickness / 2f
        val usableWidth = endX - startX
        
        // The point where progress ends
        val progressX = startX + (usableWidth * progress)

        paint.strokeWidth = strokeThickness
        paint.color = indicatorColor
        
        trackPaint.strokeWidth = strokeThickness
        trackPaint.color = trackColor

        // 1. Draw full background track (straight line)
        canvas.drawLine(startX, centerY, endX, centerY, trackPaint)

        // 2. Draw wavy progress indicator (from start to progressX)
        if (progress > 0) {
            path.reset()
            path.moveTo(startX, centerY)

            val step = 4f
            var x = startX
            while (x <= progressX) {
                // Use a fixed wavelength so the wave doesn't squash when filling
                // angle = (x / wavelength) + phase
                val wavelength = usableWidth / (waveFrequency * 2) 
                val angle = (x / wavelength) + phase
                val y = centerY + sin(angle).toFloat() * waveAmplitude
                path.lineTo(x, y)
                x += step
                
                // Ensure we hit exactly progressX at the end
                if (x > progressX && x < progressX + step) x = progressX
            }

            canvas.drawPath(path, paint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        phaseAnimator?.cancel()
    }

    fun setWaveProps(amplitude: Float, frequency: Float, speed: Float) {
        this.waveAmplitude = amplitude
        this.waveFrequency = frequency
        this.waveSpeed = speed
        startPhaseAnimation()
    }
}
