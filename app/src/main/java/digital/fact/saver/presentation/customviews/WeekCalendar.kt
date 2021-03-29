package digital.fact.saver.presentation.customviews

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import java.text.SimpleDateFormat
import java.util.*

class WeekCalendar(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

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

    private val week = Week(
            this,
            numberPaint,
            textPaint,
            selectorPaint,
            helperPaint,
            resources)

    fun setCurrentDate(newDate: Date) {
        week.setCurrentDate(newDate)
    }

    fun setOnDateChangedListener(listener: ((Calendar) -> Unit)?) {
        week.setOnDateChangedListener(listener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val touchableDayWidth = (measuredWidth / 7f)
        week.construct(touchableDayWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.run {
            week.draw(this)
        }
    }

    class Week(
            private val view: View,
            private val numberPaint: Paint,
            private val textPaint: Paint,
            private val selectorPaint: Paint,
            private val helperPaint: Paint,
            resources: Resources
    ) {

        private val selectorCorners = resources.getDimension(R.dimen.mediumCorners)
        private var touchableDayWidth = 0f
        var isAnimationRunning = false
            private set

        private val daysOfWeek = arrayListOf<DayOfWeek>()
        private val selector = Path()
        private val calendar = Calendar.getInstance(Locale.getDefault())
        private var onDateChangedListener: ((Calendar) -> Unit)? = null

        fun setOnDateChangedListener(listener: ((Calendar) -> Unit)?) {
            onDateChangedListener = listener
        }

        init {
            val tempCalendar = Calendar.getInstance(Locale.getDefault())
            tempCalendar.time = calendar.time
            tempCalendar.add(Calendar.DAY_OF_MONTH, -6)
            for (i in 0 until 13) {
                daysOfWeek.add(DayOfWeek(
                        RectF(), RectF(), RectF(),
                        tempCalendar.get(Calendar.DAY_OF_MONTH),
                        getWeekDayText(tempCalendar),
                        numberPaint, textPaint))
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        fun setCurrentDate(newDate: Date) {
            if (isAnimationRunning) return
            calendar.time = newDate
            setNewDates()
            view.invalidate()
        }

        fun setNewDates() {
            val tempCalendar = Calendar.getInstance(Locale.getDefault())
            tempCalendar.time = calendar.time
            tempCalendar.add(Calendar.DAY_OF_MONTH, -6)
            for (i in 0 until 13) {
                daysOfWeek[i].number = tempCalendar.get(Calendar.DAY_OF_MONTH)
                daysOfWeek[i].text = getWeekDayText(tempCalendar)
                tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        private fun getWeekDayText(calendar: Calendar): String {
            val format = SimpleDateFormat("EE", Locale.getDefault())
            return format.format(calendar.time)
        }

        fun construct(touchableWidth: Float, measuredHeight: Int) {
            touchableDayWidth = touchableWidth
            for (i in 0 until 13) {
                val touchableLeft = (i-3) * touchableWidth
                val touchableRight = touchableLeft + touchableWidth
                daysOfWeek[i].touchableRect.set(
                        touchableLeft,
                        0f,
                        touchableRight,
                        measuredHeight.toFloat())
                val numbersTop = (touchableWidth/3f).toInt()
                val numberRectMargin = (touchableWidth/6f).toInt()
                daysOfWeek[i].numberRect.set(
                        (touchableLeft + numberRectMargin),
                        numbersTop.toFloat(),
                        (touchableRight - numberRectMargin),
                        measuredHeight.toFloat()
                )
                daysOfWeek[i].textRect.set(
                        (touchableLeft + numberRectMargin),
                        0f,
                        (touchableRight - numberRectMargin),
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

        fun draw(canvas: Canvas) {
            canvas.run {
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
                            dayOfWeek.textRect.bottom - dayOfWeek.textRect.height() * 0.3f,
                            textPaint
                    )
                }
            }
        }

        @Suppress("unused")
        fun drawHelpLines(canvas: Canvas) {
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

        private class DayOfWeek(
                val touchableRect: RectF,
                val numberRect: RectF,
                val textRect: RectF,
                var number: Int,
                var text: String,
                numberPaint: Paint,
                textPaint: Paint
        ) {

            val numberBounds = Rect()
            val textBounds = Rect()
            private lateinit var initialTouchableRect: RectF
            private lateinit var initialNumberRect: RectF
            private lateinit var initialTextRect: RectF

            init {
                numberPaint.getTextBounds(
                        number.toString(), 0, number.toString().length, numberBounds)
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

        fun handleActionUpEvent(event: MotionEvent) {
            if (isAnimationRunning) return
            for (day in daysOfWeek) {
                if (day.touchableRect.contains(event.x, event.y)) {
                    val position = daysOfWeek.indexOf(day)
                    animateSelectorMovement(position - 3)
                }
            }
        }

        private fun animateSelectorMovement(toPosition: Int) {
            calendar.add(Calendar.DAY_OF_MONTH, toPosition - 3)
            if (toPosition == 3) return
            val endMultiplier = -3 + toPosition
            val animator = ValueAnimator.ofFloat(0f, endMultiplier.toFloat())
            animator.duration = 300L
            animator.interpolator = AccelerateInterpolator()
            var prevOffset = 0f
            animator.addUpdateListener {
                val offset = touchableDayWidth * (it.animatedValue as Float)
                selector.offset(offset - prevOffset, 0f)
                prevOffset = offset
                view.invalidate()
            }
            animator.addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    onDateChangedListener?.invoke(calendar)
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
                val offset = touchableDayWidth * (it.animatedValue as Float)
                selector.offset(offset - prevOffset, 0f)
                for (day in daysOfWeek) day.offset(offset - prevOffset, 0f)
                prevOffset = offset
                view.invalidate()
            }
            animator.addListener(object: AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    for (day in daysOfWeek) day.resetRects()
                    setNewDates()
                    isAnimationRunning = false
                }
            })
            animator.start()
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_UP -> {
                week.handleActionUpEvent(event)
            }
        }
        return true
    }

}