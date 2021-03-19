package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import digital.fact.saver.R

class DotFillerView(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {

    private var dotSize = 0f
    private var dashSize = 0f
    private var offset = 0f
    private var count = 0
    private val dotRect = RectF()
    private val dotFirstRect = RectF()
    private val dotLastRect = RectF()
    private val dotPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorAccent)
        style = Paint.Style.FILL
    }

    init {
        setWillNotDraw(false)
        context.theme.obtainStyledAttributes(
                attributeSet,
                R.styleable.DotFillerView,
                0, 0
        ).apply {
            try {
                dotSize = getDimension(R.styleable.DotFillerView_dotSize, 1f)
                dashSize = getDimension(R.styleable.DotFillerView_dashSize, 1f)
                dotPaint.color = getColor(R.styleable.DotFillerView_dotColor, 0)
            } finally {
                recycle()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        count = ((measuredHeight.toFloat() - dotSize)/(dotSize + dashSize)).toInt()
        offset = (measuredHeight.toFloat() - (count * (dotSize + dashSize) + dotSize))/2f
        dotRect.set(
                0f,
                measuredHeight.toFloat() - dotSize,
                dotSize,
                measuredHeight.toFloat()
        )
        dotFirstRect.set(
                dotSize/2f,
                measuredHeight.toFloat() - dotSize,
                dotSize,
                measuredHeight.toFloat()
        )
        dotLastRect.set(
                0f,
                measuredHeight.toFloat() - dotSize,
                dotSize/2f,
                measuredHeight.toFloat()
        )
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
       // canvas?.run {
       //     translate(offset, -height * 0.2f)
       //     for(i in 0 until count) {
       //         drawRect(if(i == 0) dotFirstRect else dotRect, dotPaint)// квадратик
       //         translate(0f, dotSize + dashSize) // отступ
       //     }
       //     drawRect(dotLastRect, dotPaint)
       // }
    }

}
