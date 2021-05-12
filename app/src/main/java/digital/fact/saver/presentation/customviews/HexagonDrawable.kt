package digital.fact.saver.presentation.customviews

import android.graphics.*

import android.graphics.drawable.Drawable


class HexagonDrawable(val list: List<BalanceChart.Coordinate>) : Drawable() {
    private val mPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mPath: Path = Path()
    override fun draw(canvas: Canvas) {
        canvas.drawPath(mPath, mPaint)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        mPaint.colorFilter = colorFilter
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()
        mPath.reset()
        mPath.moveTo(0f, height / 2)
        mPath.lineTo(width / 4f, 0f)
        mPath.lineTo(width * 3 / 4f, 0f)
        mPath.lineTo(width, height / 2f)
        mPath.lineTo(width * 3 / 4f, height)
        mPath.lineTo(width / 4, height)
        mPath.close()
    }
}