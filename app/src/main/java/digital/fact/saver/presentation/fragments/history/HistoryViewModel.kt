package digital.fact.saver.presentation.fragments.history

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.domain.models.DailyFee
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.Template
import digital.fact.saver.domain.models.PlanStatus
import digital.fact.saver.domain.models.toPlans
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.*
import digital.fact.saver.utils.events.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.absoluteValue

class HistoryViewModel(
        private val mainVM: MainViewModel
) : ViewModel() {

    private var periodStart = 0L
    private var periodEnd = 0L

    private val _currentDate = MutableLiveData(Date())
    val currentDate: LiveData<Date> = _currentDate
    var shouldRecreateHistoryAdapter = false
        private set
    private val _periodDaysLeft = MutableLiveData<Long>()
    val periodDaysLeft: LiveData<Long> = _periodDaysLeft
    private val _historyBlurViewHeight = MutableLiveData(0)
    val historyBlurViewHeight: LiveData<Int> = _historyBlurViewHeight
    private val _periodProgress = MutableLiveData<Int>()
    val periodProgress: LiveData<Int> = _periodProgress

    fun setCurrentDate(newDate: Date) {
        val oldDate = _currentDate.value ?: Date()
        val daysDiff = getDaysDifference(oldDate, newDate)
        shouldRecreateHistoryAdapter = daysDiff.absoluteValue > 3
        _currentDate.value = newDate
        if (isInsideCurrentPeriod()) {
            _periodDaysLeft.value = getDaysDifference(Date(periodEnd), newDate)
        } else _periodDaysLeft.value = 0L
        updateEconomy()
        updateSavings()
        updateDailyFees()
        updatePeriodProgress()
        updateTemplates()
    }

    private val _economy = MutableLiveData(0L)
    val economy: LiveData<Long> = _economy

    private val _savings = MutableLiveData(0L)
    val savings: LiveData<Long> = _savings

    private fun updateEconomy() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = currentDate.value ?: Date()
            _economy.postValue(calculateEconomy(periodStart, periodEnd, date))
        }
    }

    private fun updateSavings() {
        viewModelScope.launch(Dispatchers.IO) {
            val date = currentDate.value ?: Date()
            _savings.postValue(calculateSavings(periodStart, periodEnd, date))
        }
    }

    fun setHistoryBlurViewWidth(newValue: Int) {
        _historyBlurViewHeight.value = newValue
    }

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
            val dbPlans = App.db.plansDao().getCurrentPlans(periodStart, periodEnd)
            val result = dbPlans.toPlans()
            result.forEach {
                it.inPeriod = true
                it.status = PlanStatus.CURRENT
            }
            _currentPlans.postValue(result)
        }
    }

    private fun updateViewModel() {
        periodStart = mainVM.periodStart.value ?: 0L
        periodEnd = mainVM.periodEnd.value ?: 0L
        updateCurrentPlans()
        updateEconomy()
        updateSavings()
        updateDailyFees()
        updatePeriodProgress()
        updateTemplates()
    }

    private val _templates = MutableLiveData<List<Template>>()
    val templates: LiveData<List<Template>> = _templates

    private fun updateTemplates() {
        viewModelScope.launch(Dispatchers.IO) {
            _templates.postValue(getTemplates())
        }
    }

    fun isInsideCurrentPeriod(): Boolean {
        return currentDate.value?.time in periodStart until periodEnd
    }

    private val _dailyFees = MutableLiveData<List<DailyFee>>()
    val dailyFees: LiveData<List<DailyFee>> = _dailyFees
    var shouldShowDailyFee = false
        private set

    private fun updateDailyFees() {
        viewModelScope.launch(Dispatchers.IO) {
            val dailyFees = getDailyFees(currentDate.value ?: Date())
            _dailyFees.postValue(dailyFees)
            shouldShowDailyFee = dailyFees.isNotEmpty()
        }
    }

    fun addDailyFeeOperation(sum: Long, sourceId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            App.db.operationsDao().insert(DbOperation(
                type = DbOperation.OperationType.SAVER_INCOME.value,
                name = "",
                operation_date = currentDate.value?.time ?: Date().time,
                adding_date = Date().time,
                sum = sum,
                from_source_id = 0L,
                to_source_id = sourceId,
                plan_id = 0L,
                category_id = 0L,
                comment = ""
            ))
            mainVM.notifyConditionsChanged()
        }
    }

    private fun updatePeriodProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            val daysGone = 1 + getDaysDifference(currentDate.value ?: Date(), Date(periodStart))
            val periodLength = getDaysDifference(Date(periodEnd), Date(periodStart))
            if (periodLength == 0L) {
                _periodProgress.postValue(0)
            } else {
                val progress = daysGone.toFloat() * 100f / periodLength.toFloat()
                if (progress < 0f) _periodProgress.postValue(0)
                else _periodProgress.postValue(progress.toInt())
            }
        }
    }

    init {
        mainVM.conditionsChanged.observeForever { updateViewModel() }
    }

    @Suppress("UNCHECKED_CAST")
    class HistoryViewModelFactory(
            private val mainVM: MainViewModel
    ): ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HistoryViewModel(mainVM) as T
        }

    }

}