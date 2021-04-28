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

abstract class OverlayBuilder(context: Context): View(context) {

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

    fun drawCurvedArrow(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        curveRadius: Int,
        color: Int,
        lineWidth: Float
    ): OverlayBuilder {
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = lineWidth
        paint.color = ContextCompat.getColor(context, color)
        val path = Path()
        val midX = x1 + (x2 - x1) / 2
        val midY = y1 + (y2 - y1) / 2
        val xDiff = (midX - x1)
        val yDiff = (midY - y1)
        val angle = atan2(yDiff.toDouble(), xDiff.toDouble()) * (180 / Math.PI) - 90
        val angleRadians = Math.toRadians(angle)
        val pointX = (midX + curveRadius * cos(angleRadians)).toFloat()
        val pointY = (midY + curveRadius * sin(angleRadians)).toFloat()
        path.moveTo(x1, y1)
        path.cubicTo(x1, y1, pointX, pointY, x2, y2)
        val canvas = Canvas()
        canvas.drawPath(path, paint)
        draw(canvas)
        return this
    }
}