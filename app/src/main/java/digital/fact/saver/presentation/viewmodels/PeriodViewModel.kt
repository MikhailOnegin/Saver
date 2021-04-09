package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Operation
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.models.*
import digital.fact.saver.utils.resetTimeInMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PeriodViewModel : ViewModel() {

    private var app: App = App.getInstance()
    private val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)

    private val _period = MutableLiveData<Pair<Long, Long>>()
    val period: LiveData<Pair<Long, Long>> = _period

    private val _operations = MutableLiveData<List<digital.fact.saver.domain.models.Operation>>()
    val operations: LiveData<List<digital.fact.saver.domain.models.Operation>> = _operations

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> = _plans

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

    fun getOperationsByDate(sources: List<SourceItem>, period: Pair<Long, Long>) {
        val ids = mutableListOf<Long>()
        for (item in sources)
            if (item is Sources) ids.add(item.id)
        CoroutineScope(Dispatchers.IO).launch {
            _operations.postValue(
                App.db.operationsDao().getByDate(
                    itemId = ids,
                    date = period.second
                ).toOperations()
            )
        }
    }

    fun getPlansByDate(period: Pair<Long, Long>) {
        CoroutineScope(Dispatchers.IO).launch {
            _plans.postValue(
                App.db.plansDao().getPlansByPeriod(
                    periodStart = period.first,
                    periodEnd = period.second
                ).toPlans()
            )
        }
    }

    fun getOperationsResultByDate(
        sources: List<SourceItem>?,
        operations: List<digital.fact.saver.domain.models.Operation>
    ): List<Pair<String, Long>> {
        var saversCount = 0L
        var walletsCount = 0L
        var plannedExpensesCount = 0L
        var plannedExpensesFinishedCount = 0L
        var plannedIncomesCount = 0L
        sources?.forEach { item ->
            when (item.itemType) {
                Sources.TYPE_SOURCE_ACTIVE -> {
                    walletsCount += (item as Sources).startSum
                    operations.filter { it.fromSourceId == item.id || it.toSourceId == item.id }
                        .forEach { operation ->
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
                Sources.TYPE_SAVER -> {
                    saversCount += (item as Sources).currentSum
                }
            }
        }
        if (walletsCount == 0L) {
            val counter = sources?.firstOrNull { it is SourcesActiveCount }
            walletsCount = (counter as? SourcesActiveCount)?.activeWalletsSum ?: 0L
        }
        return listOf(
            Pair(WALLETS_COUNT, walletsCount),
            Pair(SAVERS_COUNT, saversCount),
            Pair(PLANNED_INCOMES_COUNT, plannedIncomesCount),
            Pair(PLANNED_EXPENSES_COUNT, plannedExpensesCount)
        )
    }

    fun calculateDaysCount(period: Pair<Long, Long>): Int {
        val inMillis = period.second - period.first
        return inMillis.div(3600000L * 24).toInt()
    }

    fun readPrefsFromDisk() {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_MONTH, 30)
            val nextMonth = calendar.timeInMillis
            _period.postValue(
                Pair(
                    first = prefs.getLong(PREF_PERIOD_START, resetTimeInMillis(Date().time)),
                    second = prefs.getLong(PREF_PERIOD_END, resetTimeInMillis(nextMonth))
                )
            )
        }
    }

    fun writeToPrefs(first: Long?, second: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            prefs.edit()
                .putLong(PREF_PERIOD_START, resetTimeInMillis(first ?: 0L))
                .putLong(PREF_PERIOD_END, resetTimeInMillis(second ?: 0L))
                .apply()
            readPrefsFromDisk()
        }
    }

    companion object {
        const val PREF_PERIOD_START = "pref_period_start"
        const val PREF_PERIOD_END = "pref_period_end"

        const val WALLETS_COUNT = "wallets_count"
        const val SAVERS_COUNT = "savers_count"
        const val PLANNED_INCOMES_COUNT = "planned_incomes"
        const val PLANNED_EXPENSES_COUNT = "planned_expenses"
        const val PLANNED_INCOMES_FINISHED_COUNT = "planned_incomes_finished"
        const val PLANNED_EXPENSES_FINISHED_COUNT = "planned_expenses_finished"
    }

}