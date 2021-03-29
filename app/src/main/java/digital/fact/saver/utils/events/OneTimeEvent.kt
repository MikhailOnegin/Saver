package digital.fact.saver.utils.events

class OneTimeEvent {

    @Suppress("MemberVisibilityCanBePrivate")
    var hasBeenHandled = false
        private set

    fun getEventIfNotHandled(): OneTimeEvent? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            this
        }
    }

    class Observer(
        private val onEventUnhandled: () -> Unit
    ) : androidx.lifecycle.Observer<OneTimeEvent>{

        override fun onChanged(event: OneTimeEvent?) {
            event?.getEventIfNotHandled()?.run {
                onEventUnhandled.invoke()
            }
        }

    }

}