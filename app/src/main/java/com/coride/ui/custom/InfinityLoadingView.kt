package com.coride.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.google.android.material.color.MaterialColors
import kotlin.math.*

/**
 * A premium Material 3 inspired Infinity (Lemniscate) Loading View.
 * It features a wavy glowing segment traveling along an infinity loop path.
 */
class InfinityLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 8f
    }

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeWidth = 12f
    }

    private val infinityPath = Path()
    private val indicatorPath = Path()
    private val pathMeasure = PathMeasure()
    
    private var progress = 0f // 0 to 1 for the segment position
    
    private var animator: ValueAnimator? = null

    var indicatorColor = Color.WHITE
    var trackColor = Color.parseColor("#1AFFFFFF")
    var segmentLength = 0.3f // 30% of the path length

    init {
        indicatorColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnPrimary, Color.WHITE)
        trackColor = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant, Color.GRAY)
        trackColor = Color.argb(40, Color.red(trackColor), Color.green(trackColor), Color.blue(trackColor))
        
        startAnimations()
    }

    private fun startAnimations() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2000 // Slightly faster for a "normal" feel
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createInfinityPath(w.toFloat(), h.toFloat())
    }

    private fun createInfinityPath(w: Float, h: Float) {
        infinityPath.reset()
        val centerX = w / 2f
        val centerY = h / 2f
        val a = min(w, h) * 0.4f // Size parameter

        // Lemniscate of Bernoulli formula:
        // x = a * cos(t) / (1 + sin^2(t))
        // y = a * sin(t) * cos(t) / (1 + sin^2(t))
        
        val steps = 100
        for (i in 0..steps) {
            val t = (i.toFloat() / steps) * 2 * PI.toFloat()
            val sinT = sin(t)
            val cosT = cos(t)
            val denominator = 1 + sinT * sinT
            
            val x = centerX + (a * cosT) / denominator
            val y = centerY + (a * sinT * cosT) / denominator
            
            if (i == 0) infinityPath.moveTo(x, y)
            else infinityPath.lineTo(x, y)
        }
        infinityPath.close()
        pathMeasure.setPath(infinityPath, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (infinityPath.isEmpty) return

        trackPaint.color = trackColor
        indicatorPaint.color = indicatorColor
        
        // Draw the background infinity track
        canvas.drawPath(infinityPath, trackPaint)

        // Calculate segment start and end
        val totalLength = pathMeasure.length
        val startDist = progress * totalLength
        val segmentDist = segmentLength * totalLength
        
        indicatorPath.reset()
        
        // Handle wrap-around for the segment
        if (startDist + segmentDist <= totalLength) {
            pathMeasure.getSegment(startDist, startDist + segmentDist, indicatorPath, true)
        } else {
            // Segment crosses the end of the path
            pathMeasure.getSegment(startDist, totalLength, indicatorPath, true)
            pathMeasure.getSegment(0f, (startDist + segmentDist) % totalLength, indicatorPath, true)
        }

        canvas.drawPath(indicatorPath, indicatorPaint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }
}
