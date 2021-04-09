package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.SourcesActiveCount
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.utils.resetTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PeriodViewModel : ViewModel() {

    private var app: App = App.getInstance()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val _period = MutableLiveData<Pair<Long, Long>>()
    val period: LiveData<Pair<Long, Long>> = _period

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
                    PlanTable.PlanType.INCOME.value -> incomes += it.sum
                    PlanTable.PlanType.EXPENSES.value -> expenses += it.sum
                }
            }
            _incomes.postValue(incomes)
            _expenses.postValue(expenses)
        }
    }

    fun getOperationsResultByDate(
        sources: List<SourceItem>,
        period: Pair<Long, Long>
    ): Pair<Long, Pair<Long, Long>> {
        var saversCount = 0L
        var walletsCount = 0L
        var plannedExpensesCount = 0L
        var plannedIncomesCount = 0L
        sources.forEach { item ->
            when (item.itemType) {
                Sources.TYPE_SOURCE_ACTIVE -> {
                    App.db.operationsDao().getByDate(
                        itemId = item.itemId,
                        date = period.second
                    ).toOperations().forEach { operation ->
                        when (operation.type) {
                            Operation.OperationType.EXPENSES.value -> walletsCount -= operation.sum
                            Operation.OperationType.PLANNED_EXPENSES.value -> {
                                walletsCount -= operation.sum
                                plannedExpensesCount += operation.sum
                            }
                            Operation.OperationType.INCOME.value -> walletsCount += operation.sum
                            Operation.OperationType.PLANNED_INCOME.value -> {
                                walletsCount += operation.sum
                                plannedIncomesCount += operation.sum
                            }
                            Operation.OperationType.TRANSFER.value ->
                                if (operation.fromSourceId == (item as Sources).id) {
                                    walletsCount -= operation.sum
                                } else if (operation.toSourceId == item.id) {
                                    walletsCount += operation.sum
                                }
                        }
                    }
                }
                Source.Type.SAVER.value -> {
                    saversCount += (item as Sources).currentSum
                }
            }
        }
        if (walletsCount == 0L) {
            val counter = sources.firstOrNull { it is SourcesActiveCount }
            walletsCount = (counter as? SourcesActiveCount)?.activeWalletsSum ?: 0L
        }
        return Pair(walletsCount - saversCount, Pair(plannedIncomesCount, plannedExpensesCount))
    }

    fun calculateDaysCount(period: Pair<Long, Long>): Int {
        val inMillis = period.second - period.first
        return inMillis.div(3600000L * 24).toInt()
    }

    private fun readPrefsFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, 30)
            val nextMonth = calendar.timeInMillis
            _period.postValue(
                Pair(
                    first = prefs.getLong(PREF_PLANNED_PERIOD_FROM, resetTimeInMillis(Date().time)),
                    second = prefs.getLong(PREF_PLANNED_PERIOD_TO, resetTimeInMillis(nextMonth))
                )
            )
        }
    }

    fun writeToPrefs(first: Long?, second: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit()
                .putLong(PREF_PLANNED_PERIOD_FROM, resetTimeInMillis(first ?: 0L))
                .putLong(PREF_PLANNED_PERIOD_TO, resetTimeInMillis(second ?: 0L))
                .apply()
            readPrefsFromDisk()
        }
    }

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }

}