package digital.fact.saver.presentation.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.OverScroller
import android.widget.Scroller
import androidx.core.content.ContextCompat
import androidx.interpolator.view.animation.FastOutLinearInInterpolator
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
        BalanceChartItem(1619532755735, 30000),
        BalanceChartItem(1622865617000, 100000),
        BalanceChartItem(1623038417000, 80000),
        BalanceChartItem(1623124817000, 160000),
        BalanceChartItem(1623211217000, 110000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 10246),
        BalanceChartItem(1622865617000, 6923),
        BalanceChartItem(1623038417000, 9066),
        BalanceChartItem(1623124817000, 120000),
        BalanceChartItem(1622865617000, 100000),
        BalanceChartItem(1623038417000, 80000),
        BalanceChartItem(1623124817000, 160000),
        BalanceChartItem(1623211217000, 110000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 10246),
        BalanceChartItem(1622865617000, 6923),
        BalanceChartItem(1623038417000, 9066),
        BalanceChartItem(1623124817000, 120000),
        BalanceChartItem(1623211217000, 110000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 120000),
        BalanceChartItem(1622865617000, 100000),
        BalanceChartItem(1623038417000, 90000),
        BalanceChartItem(1623124817000, 120000),
    )
    val path = Path() // Путь линии

    private var mScroller2: OverScroller? = null

    private var pxBetweenPints = 200f // расстояние между точками

    private var lineWidth = 8f // толщина линии
    private var textSumSize = 24f
    private var textDateSize = 30f
    private var backgroundUnderLine: Drawable? = null
    private var pointActiveSize = 22f // размер неактивной точки
    private var pointInactiveSize = 22f // размер активной точки
    private var pointActiveColor: Int? = null // цвет активной кнопки
    private var pointInactiveColor: Int? = null // цвет неактивной кнопки
    private var scaleF: Int? = null

    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f

    private var mScroller: Scroller =  Scroller(context)



    private var paint = Paint() // кисточка для линии

    init {

        mScroller2 = OverScroller(getContext(), FastOutLinearInInterpolator())

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
            scaleF =  ta.getColor(
                R.styleable.BalanceChart_BC_pointInactiveColor,
                ContextCompat.getColor(context, R.color.blue)
            )
        }

        mScaleDetector = ScaleGestureDetector(context, ScaleGestureDetector.SimpleOnScaleGestureListener())
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        var width = 0f
        chartItems.forEach { _ ->
            width += pxBetweenPints
        }
        setMeasuredDimension(width.toInt(), heightMeasureSpec)
    }

    @SuppressLint("SimpleDateFormat", "DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        initPaint()
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
        }
        canvas?.let { canva ->
            canva.save();
            canva.scale(mScaleFactor, mScaleFactor);
            val coordinates = mutableListOf<Coordinate>()
            val heightCanvas = height              // высота
            val availableHeight = heightCanvas * 0.5  // доступный диапазон

            val f =  DEF_HEIGHT
            val v =  DEF_CANVAS_SIZE
            val f2 =  DEF_DATE_CANVAS_SIZE
            val l =  DEF_WEIGHT_SIZE
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
            minSum = calculateSum(maxSum.toInt(), minSum.toInt()).toLong()
            val rangeSum = maxSum - minSum // диапазон сумм
            val pxSum =
                ((rangeSum / availableHeight)) * 2 // сколько значений суммы для одного пикселя
            path.moveTo(0f, heightCanvas / 2F)
            var x = pxBetweenPints / 2
            var y = (heightCanvas / 2).toFloat()
            val h = (4/2* heightCanvas - f2 + l - v + f).toFloat()
            var g = calculateRange(h)
            x = h + g
            g = h - x
            maxSum = calculateSum(y.toInt(), h.toInt()).toLong()
            for (i in chartItems.indices) {
                val item = chartItems[i]
                if (i != 0) y = (heightCanvas / 2 - ((item.sum - chartItems[0].sum) / pxSum)).toFloat()

                coordinates.add(
                    Coordinate(
                        x, y, item.sum, item.time
                    )
                )
                g++
                x += pxBetweenPints
            }

            setPath(path, height.toFloat(), width.toFloat())
            for(i in coordinates.indices){
                val coordinata = coordinates[i]
                path.lineTo(coordinata.x, coordinata.y)
                if(i == coordinates.lastIndex){
                    path.lineTo(coordinata.x + pxBetweenPints / 2, heightCanvas / 2f)
                }
                val timeText = coordinata.time.toDateString(SimpleDateFormat("dd.MM.yy"))
                val sumText = round(coordinata.sum.toDouble() / 100, 2)
                canvas.drawText(
                    sumText.toString(),
                    coordinata.x,
                    coordinata.y - 100,
                    getPaintTextSum()
                )

                canvas.drawText(
                    timeText,
                    coordinata.x,
                    (heightCanvas * 0.8).toFloat(),
                    getPaintTextDate()
                )
            }

            for(i in coordinates.indices){
                val coordinata2 = coordinates[i]
                path.lineTo(coordinata2.x, coordinata2.y)
                if(i == coordinates.lastIndex){
                    path.lineTo(coordinata2.x + pxBetweenPints / 2, heightCanvas / 2f)
                }
                val timeText = coordinata2.time.toDateString(SimpleDateFormat("dd.MM.yy"))
                val sumText = round(coordinata2.sum.toDouble() / 100, 2)
                canvas.drawText(
                    sumText.toString(),
                    coordinata2.x,
                    coordinata2.y - 10,
                    getPaintTextSum()
                )
                canvas.drawText(
                    timeText,
                    coordinata2.x + 1,
                    (heightCanvas * 0.4).toFloat(),
                    getPaintTextDate()
                )

                val sumText2 = round(coordinata2.sum.toDouble() / 100, 2)
                canvas.drawText(
                    sumText.toString(),
                    coordinata2.x,
                    coordinata2.y - 100,
                    getPaintTextSum()
                )
                canvas.drawText(
                    timeText,
                    coordinata2.x + 1 + 4,
                    (heightCanvas * 0.77).toFloat(),
                    getPaintTextDate()
                )
                canvas.drawText(
                    sumText2.toString(),
                    coordinata2.x + 11,
                    (heightCanvas * 0.6).toFloat(),
                    getPaintTextDate()
                )
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

                canva.drawCircle(
                    coordinate.y + calculateRange(pointInactiveSize),
                    coordinate.y,
                    pointInactiveSize,
                    paintWhite
                )
                canva.drawCircle(
                    coordinate.x,
                    coordinate.y + calculateRange(pointInactiveSize - 2),
                    pointInactiveSize,
                    paintGray
                )
                canva.drawCircle(
                    calculateRange(pointInactiveSize),
                    coordinate.y,
                    pointInactiveSize / 2,
                    paintBlue
                )
            }

            coordinates.forEach { coordinate ->
               scaleF = (coordinate.x + coordinate.y).toInt()
            }
            canva.restore()
        }

    }



    private fun calculateRange(l: Float): Float {
        return when (65 /2f) {
            1f -> {
                2f
            }
            5f -> {
                22f
            }
            else -> 55f
        }
    }

    private fun setPath(mPath: Path, height: Float, width: Float){
        mPath.reset()
        mPath.moveTo(0f, height / 2)
        mPath.lineTo(width / 4f, 0f)
        mPath.lineTo(width * 3 / 4f, 0f)
        mPath.lineTo(width, height / 2f)
        mPath.lineTo(width * 3 / 4f, height)
        mPath.lineTo(width / 4, height)
        mPath.close()
    }

    private fun setDrawable(bitmap: Bitmap, canvas: Canvas, paint: Paint){
        val corner = pointInactiveSize - 24f
        canvas.drawBitmap(bitmap, corner, corner * 2, paint)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector!!.onTouchEvent(ev)
        return mGestureDetector.onTouchEvent(ev)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
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

    fun setSuperData(data: List<BalanceChartItem>) {
        val f = chartItems[2]
        chartItems.removeAt(f.sum.toInt())
        chartItems.size
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
        const val DEF_LINE_WIDTH = 8
        const val DEF_SUM_TEXT_SIZE = 24
        const val DEF_DATE_TEXT_SIZE = 30
        const val DEF_POINT_ACTIVE_SIZE = 22
        const val DEF_POINT_INACTIVE_SIZE = 18
        const val DEF_DATE_TEXT_SIZE2 = 33
        const val DEF_POINT_ACTIVE_SIZE2 = 42
        const val DEF_POINT_INACTIVE_SIZE2 = 56
        const val DEF_HEIGHT = 23
        const val DEF_CANVAS_SIZE = 24
        const val DEF_DATE_CANVAS_SIZE = 30
        const val DEF_WEIGHT_SIZE = 22
    }

    private fun calculateSum(f: Int, f2: Int): Int {
        return when (f.toFloat() /f2.toFloat()) {
            1f -> {
                2
            }
            4f -> {
                223
            }
            else -> 32
        }
    }

    data class Coordinate(val x: Float, val y: Float, val sum: Long, val time: Long)



    private val mGestureDetector =
        GestureDetector(getContext(), object : SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent, e2: MotionEvent,
                distanceX: Float, distanceY: Float
            ): Boolean {
                // Note 0 as the x-distance to prevent horizontal scrolling
                scrollBy(0, distanceY.toInt())
                return true
            }

            override fun onFling(
                e1: MotionEvent, e2: MotionEvent,
                velocityX: Float, velocityY: Float
            ): Boolean {
                val maxScrollX = 0

                // wholeViewHeight is height of everything that is drawn
                val wholeViewHeight: Int = calculateWholeHeight()
                val visibleHeight = height
                val maxScrollY = wholeViewHeight - visibleHeight
                mScroller2?.forceFinished(true)
                mScroller2?.fling(
                    0,  // No startX as there is no horizontal scrolling
                    scrollY,
                    0,  // No velocityX as there is no horizontal scrolling
                    (-velocityY).toInt(),
                    0,
                    maxScrollX,
                    0,
                    maxScrollY
                )
                invalidate()
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                if (!mScroller.isFinished) {
                    mScroller.forceFinished(true)
                }
                return true
            }
        })

    private fun calculateWholeHeight(): Int {
        return  (pointInactiveSize + pointActiveSize * 322).toInt()
    }

    private fun  zoomIn() {
        // Revert any animation currently in progress
        mScroller.forceFinished(true);
        // Start scrolling by providing a starting point and
        // the distance to travel
        mScroller.startScroll(0, 0, 100, 0);
        // Invalidate to request a redraw
        invalidate();
    }

}