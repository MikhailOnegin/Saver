package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.toPlans
import digital.fact.saver.presentation.viewmodels.PeriodViewModel
import digital.fact.saver.utils.events.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

    private val _currentPlans = MutableLiveData<List<Plan>>()
    val currentPlans: LiveData<List<Plan>> = _currentPlans

    private fun updateCurrentPlans() {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance())
            val periodStart = prefs.getLong(PeriodViewModel.PREF_PLANNED_PERIOD_FROM, 0L)
            val periodEnd = prefs.getLong(PeriodViewModel.PREF_PLANNED_PERIOD_TO, 0L)
            val dbPlans = App.db.plansDao().getCurrentPlans(periodStart, periodEnd)
            _currentPlans.postValue(dbPlans.toPlans())
        }
    }

    init {
        updateCurrentPlans()
    }

}