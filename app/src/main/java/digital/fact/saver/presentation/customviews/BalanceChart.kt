package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import digital.fact.saver.domain.models.BalanceChartItem


class BalanceChart(context: Context) : View(context) {

    private var balanceChartItem: BalanceChartItem? = null
    private var pointSize: Int = 0
    private var pointAltitude: Int = 0
    private var lineWidth: Int = 0
    private var roundRadius: Int = 0

    private var paint = Paint()

    init {
        initPaint()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(0, 0)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    private fun initPaint() {
        paint.color = ContextCompat.getColor(context, R.color.blue)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6F
        paint.flags = Paint.ANTI_ALIAS_FLAG
    }

    fun setData(data: List<Pair<Long, Long>>) {

    }

    fun setOnChartValueClicked(onChartValueClicked: (Long) -> Unit) {

    }

    private fun drawLine() {
    }

    private fun drawPointActive() {}

    private fun drawPointInactive() {

    }

    private fun drawDownView() {

    }


}