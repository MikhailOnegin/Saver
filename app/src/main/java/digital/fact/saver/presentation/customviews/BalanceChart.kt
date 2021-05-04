package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import digital.fact.saver.domain.models.BalanceChartItem


class BalanceChart(
    context: Context,
    attrs: AttributeSet,
    defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    private val list = mutableListOf<BalanceChartItem>(
        BalanceChartItem(1617543398, 90000),
        BalanceChartItem(1620135398, 100000),
        BalanceChartItem(1620135398, 110000),
        BalanceChartItem(1620308198, 120000)
    )
    private var width = 0f

    val path = Path() // Путь линии
    private var minValue = 0f // минимальное значение view
    private var maxValue = 0f // максимальное значкение view
    private var currentValue = 0f // текущее значение view

    private var dpBetweenPints = 10 // расстояние между точками
    private var pointActiveSize = 12 // размер неактивной точки точки
    private var pointInactiveSize = 12 // размер активной точки
    private var lineWidth = 6f // толщина линии


    private var centerPointStart = Point()  // точка старта линии элемента
    private var centerPointFinish = Point() // точка финиша линии элемента
    private var paint = Paint() // кисточка
    private var rectF = RectF()

    private val point1 = Point(200,300)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //setMeasuredDimension(0, 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initPaint()
        path.moveTo(100f, 100f)
        list.forEach {
            path.rQuadTo(200f, 300f, 600f, 100f)
        }
        path.reset()
        path.moveTo(100f, 100f)

        canvas?.drawPath(path, paint)
    }

    private fun drawPoint(canvas: Canvas) {
        TODO("Not yet implemented")
    }

    private fun initPaint() {
        paint.color = ContextCompat.getColor(context, R.color.blue)
        paint.strokeWidth = lineWidth
        paint.style = Paint.Style.STROKE
    }

    fun setData(data: List<BalanceChartItem>) {
        list.clear()
        list.addAll(data)
        invalidate()
    }

    fun setOnChartValueClicked(onChartValueClicked: (Long) -> Unit) {

    }

    private fun drawLine(canvas: Canvas) {
    }

    private fun calculateLineRadius(item: BalanceChartItem): Int {
        TODO("Not yet implemented")
    }
}