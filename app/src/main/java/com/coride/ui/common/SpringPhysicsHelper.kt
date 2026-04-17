package com.coride.ui.common

import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

/**
 * M3 Expressive Spring Physics Helper
 *
 * Implements Material Design 3 Expressive spring-physics motion system:
 * - SPATIAL springs: for movement, scale, and shape (can overshoot/bounce)
 * - EFFECTS springs: for opacity/color (no overshoot — effects don't bounce)
 *
 * Spring Schemes:
 * - EXPRESSIVE: High damping ratio variation, bouncy. Use for hero moments,
 *               FABs, bottom sheets, primary CTAs.
 * - STANDARD:   Damping ratio ~1.0, critically damped. Use for list items,
 *               icons, inputs — functional, minimal bounce.
 */
object SpringPhysicsHelper {

    // ─── Spring Stiffness Presets (M3 Expressive) ──────────────────────────
    private const val STIFFNESS_EXPRESSIVE = 700f    // "Fast Expressive" — hero interactions
    private const val STIFFNESS_STANDARD   = 500f    // Standard — components
    private const val STIFFNESS_SLOW       = 280f    // Slow — large containers, sheet transitions
    private const val STIFFNESS_MICRO      = 900f    // Micro-interactions, button press feedback

    // ─── Damping Ratio Presets ─────────────────────────────────────────────
    private const val DAMPING_EXPRESSIVE   = 0.55f   // Bouncy — overshoots and settles
    private const val DAMPING_STANDARD     = 0.75f   // Medium bounce
    private const val DAMPING_EFFECTS      = 0.85f   // Near-critical — alphas don't bounce
    private const val DAMPING_MICRO        = 0.5f    // Maximum expressiveness for micro-feedback

    // ═══════════════════════════════════════════════════════════════════════
    // SPATIAL SPRINGS — movement, scale, translation (can overshoot)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Spring-animate translationY. Used for bottom sheets, cards, and panels
     * entering from off-screen.
     */
    fun springTranslationY(
        view: View,
        finalValue: Float,
        stiffness: Float = STIFFNESS_STANDARD,
        dampingRatio: Float = DAMPING_STANDARD,
        startDelay: Long = 0L,
        onEnd: (() -> Unit)? = null
    ) {
        view.postDelayed({
            SpringAnimation(view, DynamicAnimation.TRANSLATION_Y, finalValue).apply {
                spring = SpringForce(finalValue).apply {
                    this.stiffness = stiffness
                    this.dampingRatio = dampingRatio
                }
                onEnd?.let { callback ->
                    addEndListener { _, _, _, _ -> callback() }
                }
                start()
            }
        }, startDelay)
    }

    /**
     * Spring-animate translationX. Used for horizontal slide-in effects.
     */
    fun springTranslateX(
        view: View,
        finalValue: Float,
        stiffness: Float = STIFFNESS_STANDARD,
        dampingRatio: Float = DAMPING_STANDARD,
        startDelay: Long = 0L,
        onEnd: (() -> Unit)? = null
    ) {
        view.postDelayed({
            SpringAnimation(view, DynamicAnimation.TRANSLATION_X, finalValue).apply {
                spring = SpringForce(finalValue).apply {
                    this.stiffness = stiffness
                    this.dampingRatio = dampingRatio
                }
                onEnd?.let { callback ->
                    addEndListener { _, _, _, _ -> callback() }
                }
                start()
            }
        }, startDelay)
    }

    /**
     * Spring-animate both scaleX and scaleY simultaneously. The "expressive pop".
     * Great for FABs, hero circles, success checks appearing.
     */
    fun springScale(
        view: View,
        finalScale: Float,
        stiffness: Float = STIFFNESS_EXPRESSIVE,
        dampingRatio: Float = DAMPING_EXPRESSIVE,
        startDelay: Long = 0L,
        onEnd: (() -> Unit)? = null
    ) {
        view.postDelayed({
            val scaleX = SpringAnimation(view, DynamicAnimation.SCALE_X, finalScale).apply {
                spring = SpringForce(finalScale).apply {
                    this.stiffness = stiffness
                    this.dampingRatio = dampingRatio
                }
                onEnd?.let { callback ->
                    addEndListener { _, _, _, _ -> callback() }
                }
            }
            val scaleY = SpringAnimation(view, DynamicAnimation.SCALE_Y, finalScale).apply {
                spring = SpringForce(finalScale).apply {
                    this.stiffness = stiffness
                    this.dampingRatio = dampingRatio
                }
            }
            scaleX.start()
            scaleY.start()
        }, startDelay)
    }

    /**
     * Effects spring for alpha — smooth fade, NO overshoot.
     * Use for text, background fades — things that should never flash.
     */
    fun springAlpha(
        view: View,
        finalAlpha: Float,
        stiffness: Float = STIFFNESS_STANDARD,
        dampingRatio: Float = DAMPING_EFFECTS,
        startDelay: Long = 0L,
        onEnd: (() -> Unit)? = null
    ) {
        view.postDelayed({
            SpringAnimation(view, DynamicAnimation.ALPHA, finalAlpha).apply {
                spring = SpringForce(finalAlpha).apply {
                    this.stiffness = stiffness
                    this.dampingRatio = dampingRatio
                }
                onEnd?.let { callback ->
                    addEndListener { _, _, _, _ -> callback() }
                }
                start()
            }
        }, startDelay)
    }

    /**
     * Combined expressive entrance: slide up from bottom + fade in.
     * Used for text content that appears after a hero element.
     */
    fun springSlideUpFadeIn(
        view: View,
        stiffness: Float = STIFFNESS_STANDARD,
        dampingRatio: Float = DAMPING_STANDARD,
        startDelay: Long = 0L
    ) {
        springTranslationY(view, 0f, stiffness, dampingRatio, startDelay)
        springAlpha(view, 1f, stiffness, DAMPING_EFFECTS, startDelay)
    }

    /**
     * Micro-interaction feedback: press scale — shrink then spring back.
     * Gives tactile, physics-based feel to button presses.
     */
    fun springPressFeedback(view: View) {
        // Compress down
        SpringAnimation(view, DynamicAnimation.SCALE_X, 0.88f).apply {
            spring = SpringForce(0.88f).apply {
                stiffness = STIFFNESS_MICRO
                dampingRatio = DAMPING_MICRO
            }
            addEndListener { _, _, _, _ ->
                // Spring back to 1.0
                SpringAnimation(view, DynamicAnimation.SCALE_X, 1f).apply {
                    spring = SpringForce(1f).apply {
                        stiffness = STIFFNESS_EXPRESSIVE
                        dampingRatio = DAMPING_MICRO
                    }
                    start()
                }
                SpringAnimation(view, DynamicAnimation.SCALE_Y, 1f).apply {
                    spring = SpringForce(1f).apply {
                        stiffness = STIFFNESS_EXPRESSIVE
                        dampingRatio = DAMPING_MICRO
                    }
                    start()
                }
            }
            start()
        }
        SpringAnimation(view, DynamicAnimation.SCALE_Y, 0.88f).apply {
            spring = SpringForce(0.88f).apply {
                stiffness = STIFFNESS_MICRO
                dampingRatio = DAMPING_MICRO
            }
            start()
        }
    }

    /**
     * Staggered spring entrance for a list of views.
     * Each view springs in offset by [staggerDelayMs] from the previous.
     * Initial state: views should be at alpha=0 and translationY > 0.
     */
    fun staggerSpringEntrance(
        views: List<View>,
        staggerDelayMs: Long = 60L,
        stiffness: Float = STIFFNESS_STANDARD,
        dampingRatio: Float = DAMPING_STANDARD
    ) {
        views.forEachIndexed { index, view ->
            val delay = index * staggerDelayMs
            springSlideUpFadeIn(view, stiffness, dampingRatio, delay)
        }
    }

    /**
     * Spring entrance for a bottom sheet / panel sliding in from bottom.
     * EXPRESSIVE scheme — hero-level interaction.
     */
    fun springBottomSheetEntrance(
        view: View,
        startDelay: Long = 200L
    ) {
        springTranslationY(
            view, 0f,
            stiffness = STIFFNESS_SLOW,
            dampingRatio = DAMPING_STANDARD,
            startDelay = startDelay
        )
        springAlpha(
            view, 1f,
            stiffness = STIFFNESS_SLOW,
            dampingRatio = DAMPING_EFFECTS,
            startDelay = startDelay
        )
    }

    /**
     * Spring pop for FABs — expressive bounce from scale 0.
     */
    fun springFabEntrance(view: View, delay: Long = 0L) {
        springScale(
            view, 1f,
            stiffness = STIFFNESS_EXPRESSIVE,
            dampingRatio = DAMPING_EXPRESSIVE,
            startDelay = delay
        )
        springAlpha(
            view, 1f,
            stiffness = STIFFNESS_STANDARD,
            dampingRatio = DAMPING_EFFECTS,
            startDelay = delay
        )
    }

    /**
     * Spring entrance for a chip/status element from above.
     */
    fun springDropEntrance(view: View, delay: Long = 0L) {
        springTranslationY(
            view, 0f,
            stiffness = STIFFNESS_EXPRESSIVE,
            dampingRatio = DAMPING_STANDARD,
            startDelay = delay
        )
        springAlpha(
            view, 1f,
            stiffness = STIFFNESS_STANDARD,
            dampingRatio = DAMPING_EFFECTS,
            startDelay = delay
        )
    }
}

