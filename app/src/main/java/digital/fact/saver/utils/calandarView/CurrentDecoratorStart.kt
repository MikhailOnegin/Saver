package digital.fact.saver.utils.calandarView

import android.content.Context
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class CurrentDecoratorStart(
        private val context: Context,
        private val start: Calendar,
        private val drawable: Drawable?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val periodStart = CalendarDay.from(
                start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1,
                start.get(Calendar.DAY_OF_MONTH)
        )
        return day == periodStart
    }

    override fun decorate(view: DayViewFacade) {
        drawable?.let { view.setSelectionDrawable(it) }
        //val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_calendar_outside_range)
        //backgroundDrawable?.let {view.setBackgroundDrawable(it)}
    }
}
