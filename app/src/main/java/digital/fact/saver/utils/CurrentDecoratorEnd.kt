package digital.fact.saver.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import digital.fact.saver.R
import java.util.*

class CurrentDecoratorEnd(
        private val context: Context,
        private val end: Calendar,
        private val drawable: Drawable?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val periodEnd = CalendarDay.from(
                end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1,
                end.get(Calendar.DAY_OF_MONTH)
        )
        return day == periodEnd
    }

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setSelectionDrawable(it) }
        //val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_calendar_outside_range)
       // backgroundDrawable?.let {view.setBackgroundDrawable(it)}
    }
}