package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import kotlin.math.max

class CounterDrawable(val context: Context) : Drawable(){

    private var mCount = 0
    private var mWillDraw = false
    private val mCirclePaint = Paint()
    private val mTextPaint = Paint()

    init {
        mCirclePaint.apply {
            color = ContextCompat.getColor(context, R.color.colorAccent)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        mTextPaint.apply {
            color = ContextCompat.getColor(context, R.color.textColorWhite)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = context.resources.getDimension(R.dimen.counterDrawableTextSize)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
    }

    fun setCount(count:Int){
        mCount = count
        mWillDraw = count > 0
        invalidateSelf()
    }

    override fun draw(canvas: Canvas) {
        if(!mWillDraw) return

        val width = bounds.right - bounds.left
        val height = bounds.bottom - bounds.top
        val maxDimen = max(width, height)
        val radius = maxDimen/3.3f
        val centerX = bounds.right.toFloat() - bounds.width()/4
        val centerY = bounds.bottom.toFloat() - bounds.height()/4

        var textWidth = mTextPaint.measureText(mCount.toString())
        val maxWidth = if(mCount > 9) radius*2*0.8 else radius*2*0.5
        do{
            if(textWidth <= maxWidth) break
            mTextPaint.textSize = mTextPaint.textSize * 0.9f
            textWidth = mTextPaint.measureText(mCount.toString())
        }while(textWidth > maxWidth)

        canvas.drawCircle(centerX, centerY, radius, mCirclePaint)
        canvas.drawText(mCount.toString(), centerX, centerY+radius/2, mTextPaint)
    }

    override fun setAlpha(alpha: Int) {}

    override fun getOpacity(): Int = PixelFormat.UNKNOWN

    override fun setColorFilter(colorFilter: ColorFilter?) {}

}