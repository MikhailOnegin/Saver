package digital.fact.saver.presentation.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import digital.fact.saver.domain.models.BalanceChartItem
import digital.fact.saver.utils.round
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat


@SuppressLint("Recycle")
class BalanceChart(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)


    private val chartItems = mutableListOf(
        BalanceChartItem(1619532755735, 90000),
        BalanceChartItem(1622865617000, 100000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 120000),
        BalanceChartItem(1623211217000, 110000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 120000),
        BalanceChartItem(1622865617000, 100000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 120000),
    )
    val path = Path() // Путь линии


    private var pxBetweenPints = 200f // расстояние между точками

    private var lineWidth = 8f // толщина линии
    private var textSumSize = 24f
    private var textDateSize = 30f
    private var backgroundUnderLine: Drawable? = null
    private var pointActiveSize = 22f // размер неактивной точки
    private var pointInactiveSize = 22f // размер активной точки
    private var pointActiveColor: Int? = null // цвет активной кнопки
    private var pointInactiveColor: Int? = null // цвет неактивной кнопки

    private var paint = Paint() // кисточка для линии

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.BalanceChart)
            lineWidth = ta.getInt(R.styleable.BalanceChart_BC_lineWidth, DEF_LINE_WIDTH).toFloat()
            textSumSize =
                ta.getInt(R.styleable.BalanceChart_BC_sumTextSize, DEF_SUM_TEXT_SIZE).toFloat()
            textDateSize =
                ta.getInt(R.styleable.BalanceChart_BC_sumTextSize, DEF_DATE_TEXT_SIZE).toFloat()
            backgroundUnderLine = ta.getDrawable(R.styleable.BalanceChart_BC_backgroundUnderLine)
            pointActiveSize =
                ta.getInt(R.styleable.BalanceChart_BC_pointActiveSize, DEF_POINT_ACTIVE_SIZE)
                    .toFloat()
            pointInactiveSize = ta.getInt(
                R.styleable.BalanceChart_BC_pointInactiveSize,
                DEF_POINT_INACTIVE_SIZE
            ).toFloat()
            pointActiveColor =
                ta.getColor(R.styleable.BalanceChart_BC_pointActiveColor, Color.GREEN)
            pointInactiveColor = ta.getColor(
                R.styleable.BalanceChart_BC_pointInactiveColor,
                ContextCompat.getColor(context, R.color.blue)
            )
        }

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = 0f
        if (chartItems.isNotEmpty()) width += pxBetweenPints / 2
        chartItems.forEach { _ ->
            width += pxBetweenPints
        }
        setMeasuredDimension(width.toInt(), heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }


    @SuppressLint("SimpleDateFormat", "DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initPaint()
        canvas?.let { canva ->
            val coordinates = mutableListOf<Coordinate>()
            canva.drawColor(Color.WHITE)
            val height = canva.height              // высота
            val availableHeight = height * 0.5  // доступный диапазон

            var maxSum = 0L
            var minSum = 0L
            chartItems.forEach { item ->
                if (item.sum > maxSum) {
                    maxSum = item.sum
                }
            }
            minSum = maxSum
            chartItems.forEach { item ->
                if (item.sum < minSum) {
                    minSum = item.sum
                }
            }
            val rangeSum = maxSum - minSum // диапазон сумм
            val pxSum =
                ((rangeSum / availableHeight)) * 2 // сколько значений суммы для одного пикселя
            path.moveTo(0f, height / 2F)
            var x = pxBetweenPints / 2
            var y = (height / 2).toFloat()
            for (i in chartItems.indices) {
                val item = chartItems[i]
                if (i != 0) y = (height / 2 - ((item.sum - chartItems[0].sum) / pxSum)).toFloat()

                coordinates.add(
                    Coordinate(
                        x, y, item.sum, item.time
                    )
                )
                x += pxBetweenPints
            }
            coordinates.forEach {
                path.lineTo(it.x, it.y)
                val timeText = it.time.toDateString(SimpleDateFormat("dd.MM.yy"))
                val sumText = round(it.sum.toDouble()/100, 2)
                canvas.drawText(sumText.toString(), it.x, it.y - 100, getPaintTextSum())
                canvas.drawText(timeText, it.x, (height * 0.8).toFloat(), getPaintTextDate())
            }
            canva.drawPath(path, paint)
            coordinates.forEach { coordinate ->
                val paintWhite = Paint()
                paintWhite.color = Color.WHITE
                paintWhite.style = Paint.Style.FILL

                val paintGray = Paint()
                paintGray.color = ContextCompat.getColor(context, R.color.lightGray)
                paintGray.style = Paint.Style.STROKE
                paintGray.strokeWidth = 2f

                val paintBlue = Paint()
                paintBlue.color = ContextCompat.getColor(context, R.color.blue)
                paintBlue.style = Paint.Style.FILL

                canva.drawCircle(coordinate.x, coordinate.y, pointInactiveSize, paintWhite)
                canva.drawCircle(coordinate.x, coordinate.y, pointInactiveSize, paintGray)
                canva.drawCircle(coordinate.x, coordinate.y, pointInactiveSize / 2, paintBlue)
            }

        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)

    }

    private fun initPaint() {
        paint.color = ContextCompat.getColor(context, R.color.blue)
        paint.strokeWidth = lineWidth
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.pathEffect = CornerPathEffect(30f)
    }

    fun setData(data: List<BalanceChartItem>) {
        chartItems.clear()
        chartItems.addAll(data)
        requestLayout()
    }


    private fun getPaintTextSum(): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.textSize = textSumSize
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        paint.textAlign = Paint.Align.CENTER
        return paint
    }

    private fun getPaintTextDate(): Paint {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.BLACK
        paint.textSize = textDateSize
        paint.style = Paint.Style.FILL_AND_STROKE
        paint.textAlign = Paint.Align.CENTER
        return paint
    }


    companion object {
        val DEF_LINE_WIDTH = 8
        val DEF_SUM_TEXT_SIZE = 24
        val DEF_DATE_TEXT_SIZE = 30
        val DEF_POINT_ACTIVE_SIZE = 22
        val DEF_POINT_INACTIVE_SIZE = 18
    }


    data class Coordinate(val x: Float, val y: Float, val sum: Long, val time: Long)
}