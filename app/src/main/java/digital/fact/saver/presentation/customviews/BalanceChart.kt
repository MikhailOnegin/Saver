package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin


class BalanceChart(context: Context): View(context) {
    fun setData(data: List<Pair<Long, Long>>){

    }

    fun setOnChartValueClicked(onChartValueClicked: (Long) -> Unit){

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


}