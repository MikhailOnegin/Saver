package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.data.repositories.PlansRepositoryIml
import digital.fact.saver.data.database.dto.Period
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.repository.PlansRepository
import digital.fact.saver.utils.resetDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PlansViewModel(application: Application) : AndroidViewModel(application) {

    val showRecyclerAnim: MutableLiveData<Boolean> = MutableLiveData(true)

    private val plansRepository: PlansRepository
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    init {
        plansRepository = PlansRepositoryIml(application)
        getPeriod()
    }

    private val _period: MutableLiveData<Period> = MutableLiveData()
    val period: LiveData<Period> = _period

    fun getPeriod() {

        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date()
            val currentTime = calendar.time.time
            calendar.add(Calendar.DATE, 30)
            val timeNextMonth = calendar.time.time


            val timeFrom = prefs.getLong(PREF_PLANNED_PERIOD_FROM, resetDate(currentTime))
            val timeTo = prefs.getLong(PREF_PLANNED_PERIOD_TO, resetDate(timeNextMonth))
            val calendarFrom = Calendar.getInstance(Locale.getDefault())
            calendarFrom.timeInMillis = timeFrom
            val calendarTo = Calendar.getInstance(Locale.getDefault())
            calendarTo.timeInMillis = timeTo

            calendarFrom.add(Calendar.DAY_OF_MONTH, 3)

            _period.postValue(
                    Period(
                            calendarFrom,
                            calendarTo
                    )
            )
        }
    }

    fun getAllPlans(): LiveData<List<PlanTable>> {
        return plansRepository.getAll()
    }

    private val _insertPlan: MutableLiveData<Long> = MutableLiveData()
    val insertPlan: LiveData<Long> = _insertPlan

    fun insertPlan(planTable: PlanTable): LiveData<Long> {
        return plansRepository.insert(planTable)
    }

    fun deletePlan(planTable: PlanTable): LiveData<Int> {
        return plansRepository.delete(planTable)
    }

    fun updatePlan(planTable: PlanTable): LiveData<Int> {
        return plansRepository.update(planTable)
    }

    fun getPlansByPeriod(periodStart: Long, periodEnd: Long): LiveData<List<PlanTable>> {
        return plansRepository.getPlansByPeriod(periodStart = periodStart, periodEnd = periodEnd)
    }

    fun updatePlans() {
        plansRepository.updateAll()
    }


    private val _plansBlurViewHeight = MutableLiveData(0)
    val plansBlurViewHeight: LiveData<Int> = _plansBlurViewHeight

    fun setPlansBlurViewWidth(newValue: Int) {
        _plansBlurViewHeight.value = newValue
    }

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }
}