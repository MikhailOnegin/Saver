package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import digital.fact.saver.utils.events.Event

class HistoryViewModel : ViewModel() {

    private var isSecondLayerShown = false
    private val _secondLayerEvent = MutableLiveData<Event<Boolean>>()
    val secondLayerEvent: LiveData<Event<Boolean>> = _secondLayerEvent

    fun onAddOperationButtonClicked() {
        isSecondLayerShown = !isSecondLayerShown
        _secondLayerEvent.value = Event(isSecondLayerShown)
    }

    fun resetSecondLayerState() {
        isSecondLayerShown = false
    }

    fun collapseSecondLayout() {
        isSecondLayerShown = false
        _secondLayerEvent.value = Event(isSecondLayerShown)
    }

}