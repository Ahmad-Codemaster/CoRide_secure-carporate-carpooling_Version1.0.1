package com.coride.ui.custom

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.coride.R

class CurvedBottomNavigationView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val mPath = Path()
    private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    private val CIRCLE_RADIUS = dpToPx(28f)
    private val GAP = dpToPx(4f)
    private val CRADLE_RADIUS = CIRCLE_RADIUS + GAP
    private val TOP_OFFSET = dpToPx(38f)

    private var selectedIndex = 0
    private var currentCutoutX = -1f

    private val itemsLayout: LinearLayout
    private var navController: NavController? = null
    
    data class NavItem(val id: Int, @DrawableRes val iconRes: Int, @StringRes val textRes: Int)
    private val menuItems = mutableListOf<NavItem>()
    private val itemViews = mutableListOf<FrameLayout>()

    private val activeColor = ContextCompat.getColor(context, R.color.primary)
    private val inactiveColor = ContextCompat.getColor(context, R.color.outline)
    private val bgColor = Color.WHITE

    private var circleY = dpToPx(34f)
    private var isAnimating = false

    init {
        mPaint.style = Paint.Style.FILL
        mPaint.color = bgColor
        
        mShadowPaint.style = Paint.Style.FILL
        mShadowPaint.color = bgColor
        mShadowPaint.setShadowLayer(dpToPx(8f), 0f, dpToPx(2f), Color.parseColor("#22000000"))
        setLayerType(View.LAYER_TYPE_SOFTWARE, mShadowPaint)

        setWillNotDraw(false)
        clipChildren = false
        clipToPadding = false

        itemsLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            setPadding(0, TOP_OFFSET.toInt(), 0, 0)
            clipChildren = false
            clipToPadding = false
        }
        addView(itemsLayout)
    }

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

    fun setupWithNavController(navController: NavController, items: List<NavItem>) {
        this.navController = navController
        this.menuItems.clear()
        this.menuItems.addAll(items)
        
        val currentDestId = navController.currentDestination?.id
        val initialIndex = menuItems.indexOfFirst { it.id == currentDestId }
        if (initialIndex != -1) {
            this.selectedIndex = initialIndex
        }

        buildItems()
        
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val index = menuItems.indexOfFirst { it.id == destination.id }
            if (index != -1 && index != selectedIndex && !isAnimating) {
                setSelectedIndex(index)
            }
        }
    }

    private fun buildItems() {
        itemsLayout.removeAllViews()
        itemViews.clear()

        menuItems.forEachIndexed { index, item ->
            val itemView = FrameLayout(context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
                clipChildren = false
                clipToPadding = false
            }

            val icon = ImageView(context).apply {
                setImageResource(item.iconRes)
                setColorFilter(if (index == selectedIndex) activeColor else inactiveColor)
                layoutParams = LayoutParams(dpToPx(24f).toInt(), dpToPx(24f).toInt()).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = dpToPx(13f).toInt()
                }
            }
            
            val text = TextView(context).apply {
                setText(item.textRes)
                setTextColor(if (index == selectedIndex) activeColor else inactiveColor)
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                    bottomMargin = dpToPx(12f).toInt()
                }
            }

            itemView.addView(icon)
            itemView.addView(text)
            
            itemView.setOnClickListener {
                if (index != selectedIndex && !isAnimating) {
                    navController?.navigate(item.id)
                }
            }

            if (index == selectedIndex) {
                icon.translationY = -dpToPx(31f)
            }

            itemsLayout.addView(itemView)
            itemViews.add(itemView)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (currentCutoutX == -1f && itemViews.isNotEmpty() && itemViews[0].width > 0) {
            val v = itemViews[selectedIndex]
            currentCutoutX = v.x + v.width / 2f
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (currentCutoutX == -1f) return

        val w = width.toFloat()
        val h = height.toFloat()

        val cornerRadius = dpToPx(16f)

        mPath.reset()
        mPath.moveTo(cornerRadius, TOP_OFFSET)
        
        val curveStart = currentCutoutX - CRADLE_RADIUS * 1.6f
        val curveEnd = currentCutoutX + CRADLE_RADIUS * 1.6f

        mPath.lineTo(curveStart, TOP_OFFSET)

        mPath.cubicTo(
            currentCutoutX - CRADLE_RADIUS * 0.8f, TOP_OFFSET,
            currentCutoutX - CRADLE_RADIUS * 1.0f, TOP_OFFSET + CRADLE_RADIUS,
            currentCutoutX, TOP_OFFSET + CRADLE_RADIUS
        )
        
        mPath.cubicTo(
            currentCutoutX + CRADLE_RADIUS * 1.0f, TOP_OFFSET + CRADLE_RADIUS,
            currentCutoutX + CRADLE_RADIUS * 0.8f, TOP_OFFSET,
            curveEnd, TOP_OFFSET
        )

        mPath.lineTo(w - cornerRadius, TOP_OFFSET)
        mPath.quadTo(w, TOP_OFFSET, w, TOP_OFFSET + cornerRadius)
        mPath.lineTo(w, h)
        mPath.lineTo(0f, h)
        mPath.lineTo(0f, TOP_OFFSET + cornerRadius)
        mPath.quadTo(0f, TOP_OFFSET, cornerRadius, TOP_OFFSET)
        mPath.close()

        canvas.drawPath(mPath, mShadowPaint)

        canvas.drawCircle(currentCutoutX, circleY, CIRCLE_RADIUS, mShadowPaint)
    }

    private fun setSelectedIndex(newIndex: Int) {
        if (newIndex == selectedIndex) return
        
        val oldIndex = selectedIndex
        selectedIndex = newIndex
        isAnimating = true

        val oldView = itemViews[oldIndex]
        val newView = itemViews[newIndex]

        val oldIcon = oldView.getChildAt(0) as ImageView
        val oldText = oldView.getChildAt(1) as TextView
        val newIcon = newView.getChildAt(0) as ImageView
        val newText = newView.getChildAt(1) as TextView

        val targetX = newView.x + newView.width / 2f
        val startX = currentCutoutX

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 400
        animator.interpolator = OvershootInterpolator(1.2f)
        
        oldIcon.setColorFilter(inactiveColor)
        oldText.setTextColor(inactiveColor)
        newIcon.setColorFilter(activeColor)
        newText.setTextColor(activeColor)

        animator.addUpdateListener { anim ->
            val fraction = anim.animatedFraction
            currentCutoutX = startX + (targetX - startX) * fraction
            
            val oldY = -dpToPx(31f) * (1f - fraction)
            val newY = -dpToPx(31f) * fraction
            
            oldIcon.translationY = oldY
            newIcon.translationY = newY
            
            val arc = Math.sin((fraction * Math.PI)).toFloat()
            circleY = dpToPx(34f) + (dpToPx(10f) * arc)

            invalidate()
        }
        
        val endListener = object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                isAnimating = false
            }
        }
        animator.addListener(endListener)
        animator.start()
    }
}

