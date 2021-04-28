package digital.fact.saver.utils.calandarView

import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade

class CurrentDayDecoratorOutsideRange(
        private val selectionDrawable: Drawable?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true
    }

    override fun decorate(view: DayViewFacade) {
        selectionDrawable?.let { view.setSelectionDrawable(it) }

    }
}