package digital.fact.saver.utils.calandarView

import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class CurrentDayDecoratorInRange(
    private val start: Calendar,
    private val end: Calendar,
    private val selectionDrawable: Drawable?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        val periodStart = CalendarDay.from(
            start.get(Calendar.YEAR), start.get(Calendar.MONTH) + 1,
            start.get(Calendar.DAY_OF_MONTH)
        )
        val periodEnd = CalendarDay.from(
            end.get(Calendar.YEAR), end.get(Calendar.MONTH) + 1,
            end.get(Calendar.DAY_OF_MONTH)
        )
        return day.isInRange(periodStart, periodEnd)
    }

    override fun decorate(view: DayViewFacade) {
        selectionDrawable?.let { view.setSelectionDrawable(it) }

    }
}