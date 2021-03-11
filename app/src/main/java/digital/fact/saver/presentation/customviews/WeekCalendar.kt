package digital.fact.saver.presentation.customviews

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View

class WeekCalendar(
        context: Context,
        attrs: AttributeSet,
        defStyleAttr: Int
) : View(context, attrs, defStyleAttr) {

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
    }

}