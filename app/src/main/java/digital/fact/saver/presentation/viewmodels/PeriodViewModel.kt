package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.fragments.period.PeriodFragment
import digital.fact.saver.utils.resetDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PeriodViewModel : ViewModel() {

    private var app: App = App.getInstance()

    init {
        getPeriodPlansValues()
    }

    private val _incomes = MutableLiveData<Long>()
    val incomes: LiveData<Long> = _incomes
    private val _expenses = MutableLiveData<Long>()
    val expenses: LiveData<Long> = _expenses

    private fun getPeriodPlansValues() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = App.db.plansDao()
                .getPlansByPeriod(periodStart = readFromPrefs(), periodEnd = readFromPrefs(false))
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

    private fun readFromPrefs(first: Boolean = true): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = Date()
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val nextMonth = calendar.timeInMillis
        val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
        return if (first)
            prefs.getLong(PeriodFragment.PREF_PLANNED_PERIOD_FROM, Date().time)
        else {
            prefs.getLong(PeriodFragment.PREF_PLANNED_PERIOD_TO, nextMonth)
        }
    }

    fun writeToPrefs(first: Long?, second: Long?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(app.applicationContext)
        prefs.edit()
            .putLong(PeriodFragment.PREF_PLANNED_PERIOD_FROM, resetDate(first ?: 0L))
            .putLong(PeriodFragment.PREF_PLANNED_PERIOD_TO, resetDate(second ?: 0L))
            .apply()
    }

}