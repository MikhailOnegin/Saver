package digital.fact.saver.utils.calandarView

import android.content.Context
import android.graphics.drawable.Drawable
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import java.util.*

class CurrentDayDecoratorOutsideRange(
        private val selectionDrawable: Drawable?
) : DayViewDecorator {

    override fun shouldDecorate(day: CalendarDay): Boolean {
        return true
    }

    override fun decorate(view: DayViewFacade) {
        selectionDrawable?.let { view.setSelectionDrawable(it) }
        //val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.selector_calendar_outside_range)
        //backgroundDrawable?.let {view.setBackgroundDrawable(it)}

    }
}