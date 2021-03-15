package digital.fact.saver.presentation.customviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import digital.fact.saver.R

class WeekCalendar(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val selectorCorners = resources.getDimension(R.dimen.mediumCorners)
    private var touchableWidth = 0

    private val daysOfWeek = arrayListOf<DayOfWeek>()
    private val selector = Path()

    private val numberPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        style = Paint.Style.FILL_AND_STROKE
        typeface = Typeface.DEFAULT
        textSize = resources.getDimension(R.dimen.weekCalendarNumbersTextSize)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.parseColor("#AAACC8")
        style = Paint.Style.FILL_AND_STROKE
        typeface = Typeface.DEFAULT
        textSize = resources.getDimension(R.dimen.weekCalendarTextTextSize)
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val helperPaint = Paint().apply {
        color = ContextCompat.getColor(context, android.R.color.white)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val selectorPaint = Paint().apply {
        color = Color.parseColor("#6A54F5")
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    init {
        for (i in 0 until 13) {
            daysOfWeek.add(DayOfWeek(
                    RectF(), RectF(), RectF(),
                    i + 10,
                    "пн",
                    numberPaint, textPaint))
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        touchableWidth = (measuredWidth / 7f).toInt()
        for (i in 0 until 13) {
            val touchableLeft = (i-3) * touchableWidth
            val touchableRight = touchableLeft + touchableWidth
            daysOfWeek[i].touchableRect.set(
                    touchableLeft.toFloat(),
                    0f,
                    touchableRight.toFloat(),
                    measuredHeight.toFloat())
            val numbersTop = (touchableWidth/3f).toInt()
            val numberRectMargin = (touchableWidth/6f).toInt()
            daysOfWeek[i].numberRect.set(
                    (touchableLeft + numberRectMargin).toFloat(),
                    numbersTop.toFloat(),
                    (touchableRight - numberRectMargin).toFloat(),
                    measuredHeight.toFloat()
            )
            daysOfWeek[i].textRect.set(
                    (touchableLeft + numberRectMargin).toFloat(),
                    0f,
                    (touchableRight - numberRectMargin).toFloat(),
                    numbersTop.toFloat()
            )
            if (i == 6) {
                selector.reset()
                selector.addRoundRect(
                        daysOfWeek[i].numberRect,
                        selectorCorners,
                        selectorCorners,
                        Path.Direction.CCW
                )
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            //drawHelpLines(this)
            drawPath(selector, selectorPaint)
            for (dayOfWeek in daysOfWeek) {
                drawText(
                        dayOfWeek.number.toString(),
                        dayOfWeek.numberRect.centerX(),
                        dayOfWeek.numberRect.centerY() + dayOfWeek.numberBounds.height()/2,
                        numberPaint
                )
                drawText(
                        dayOfWeek.text,
                        dayOfWeek.textRect.centerX(),
                        dayOfWeek.textRect.top + dayOfWeek.textBounds.height().toFloat(),
                        textPaint
                )
            }
        }
    }

    @Suppress("unused")
    private fun drawHelpLines(canvas: Canvas) {
        canvas.run {
            for (day in daysOfWeek) {
                drawRect(day.touchableRect, helperPaint)
            }
            for (dayOfWeek in daysOfWeek) {
                drawRect(dayOfWeek.numberRect, helperPaint)
                drawRect(dayOfWeek.textRect, helperPaint)
            }
        }
    }

    class DayOfWeek(
            val touchableRect: RectF,
            val numberRect: RectF,
            val textRect: RectF,
            val number: Int,
            val text: String,
            numberPaint: Paint,
            textPaint: Paint
    ) {

        val numberBounds = Rect()
        val textBounds = Rect()
        private lateinit var initialTouchableRect: RectF
        private lateinit var initialNumberRect: RectF
        private lateinit var initialTextRect: RectF

        init {
            numberPaint.getTextBounds(number.toString(), 0, 2, numberBounds)
            textPaint.getTextBounds(text, 0, 2, textBounds)
        }

        fun offset(dx: Float, dy:Float) {
            if (!::initialTouchableRect.isInitialized) {
                initialTouchableRect = RectF()
                initialTouchableRect.set(touchableRect)
            }
            if (!::initialNumberRect.isInitialized) {
                initialNumberRect = RectF()
                initialNumberRect.set(numberRect)
            }
            if (!::initialTextRect.isInitialized) {
                initialTextRect = RectF()
                initialTextRect.set(textRect)
            }
            touchableRect.offset(dx, dy)
            numberRect.offset(dx, dy)
            textRect.offset(dx, dy)
        }

        fun resetRects() {
            touchableRect.set(initialTouchableRect)
            numberRect.set(initialNumberRect)
            textRect.set(initialTextRect)
        }

    }

    var isAnimationRunning = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (isAnimationRunning) return true
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                for (day in daysOfWeek) {
                    if (day.touchableRect.contains(event.x, event.y)) {
                        val position = daysOfWeek.indexOf(day)
                        animateSelectorMovement(position - 3)
                    }
                }
            }
        }
        return true
    }

    private fun animateSelectorMovement(toPosition: Int) {
        if (toPosition == 3) return
        val endMultiplier = -3 + toPosition
        val animator = ValueAnimator.ofFloat(0f, endMultiplier.toFloat())
        animator.duration = 300L
        animator.interpolator = AccelerateInterpolator()
        var prevOffset = 0f
        animator.addUpdateListener {
            val offset = touchableWidth * (it.animatedValue as Float)
            selector.offset(offset - prevOffset, 0f)
            prevOffset = offset
            invalidate()
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                animateAdjustingWeekDays(toPosition)
            }
        })
        isAnimationRunning = true
        animator.start()
    }

    private fun animateAdjustingWeekDays(fromPosition: Int) {
        val endMultiplier = 3 - fromPosition
        val animator = ValueAnimator.ofFloat(0f, endMultiplier.toFloat())
        animator.duration = 600L
        animator.interpolator = DecelerateInterpolator()
        var prevOffset = 0f
        animator.addUpdateListener {
            val offset = touchableWidth * (it.animatedValue as Float)
            selector.offset(offset - prevOffset, 0f)
            for (day in daysOfWeek) day.offset(offset - prevOffset, 0f)
            prevOffset = offset
            invalidate()
        }
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                for (day in daysOfWeek) day.resetRects()
                isAnimationRunning = false
            }
        })
        animator.start()
    }

}