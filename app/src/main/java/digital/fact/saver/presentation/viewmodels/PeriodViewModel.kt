package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.Source
import digital.fact.saver.models.SourceItem
import digital.fact.saver.models.Sources
import digital.fact.saver.models.SourcesActiveCount
import digital.fact.saver.models.toOperations
import digital.fact.saver.utils.resetDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PeriodViewModel : ViewModel() {

    private var app: App = App.getInstance()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    init {
        readPrefsFromDisk()
    }

    private val _incomes = MutableLiveData<Long>()
    val incomes: LiveData<Long> = _incomes
    private val _expenses = MutableLiveData<Long>()
    val expenses: LiveData<Long> = _expenses

    fun getPeriodPlansValues(period: Pair<Long, Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = App.db.plansDao()
                .getPlansByPeriod(periodStart = period.first, periodEnd = period.second)
            var incomes = 0L
            var expenses = 0L
            result.forEach {
                when (it.type) {
                    Plan.PlanType.INCOME.value -> incomes += it.sum
                    Plan.PlanType.SPENDING.value -> expenses += it.sum
                }
            }
            _incomes.postValue(incomes)
            _expenses.postValue(expenses)
        }
    }

    fun getOperationsResultByDate(sources: List<SourceItem>, period: Pair<Long, Long>): Long {
        var saversCount = 0L
        var walletsCount = 0L
        sources.forEach { item ->
            when (item.itemType) {
                Source.SourceCategory.WALLET_ACTIVE.value -> {
                    App.db.operationsDao().getByDate(
                        itemId = item.itemId,
                        date = period.second
                    ).toOperations().forEach { operation ->
                        when (operation.type) {
                            Operation.OperationType.EXPENSES.value, Operation.OperationType.PLANNED_EXPENSES.value -> walletsCount -= operation.sum
                            Operation.OperationType.INCOME.value, Operation.OperationType.PLANNED_INCOME.value -> walletsCount += operation.sum
                            Operation.OperationType.TRANSFER.value ->
                                if (operation.fromSourceId == (item as Sources).id) {
                                    walletsCount -= operation.sum
                                } else if (operation.toSourceId == item.id) {
                                    walletsCount += operation.sum
                                }
                        }
                    }
                }
                Source.SourceCategory.SAVER.value -> {
                    saversCount += (item as Sources).currentSum
                }
            }
        }
        if (walletsCount == 0L) walletsCount =
            (sources.firstOrNull { it is SourcesActiveCount } as SourcesActiveCount).activeWalletsSum
        return walletsCount - saversCount
    }

    fun calculateDaysCount(period: Pair<Long, Long>): Int {
        val inMillis = period.second - period.first
        return inMillis.div(3600000L * 24).toInt()
    }

    private val _period = MutableLiveData<Pair<Long, Long>>()
    val period: LiveData<Pair<Long, Long>> = _period

    private fun readPrefsFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, 30)
            val nextMonth = calendar.timeInMillis
            _period.postValue(
                Pair(
                    first = prefs.getLong(PREF_PLANNED_PERIOD_FROM, resetDate(Date().time)),
                    second = prefs.getLong(PREF_PLANNED_PERIOD_TO, resetDate(nextMonth))
                )
            )
        }
    }

    fun writeToPrefs(first: Long?, second: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit()
                .putLong(PREF_PLANNED_PERIOD_FROM, resetDate(first ?: 0L))
                .putLong(PREF_PLANNED_PERIOD_TO, resetDate(second ?: 0L))
                .apply()
            readPrefsFromDisk()
        }
    }

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }

}