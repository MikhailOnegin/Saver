package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.data.repositories.PlansRepositoryIml
import digital.fact.saver.domain.models.Period
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.domain.repository.PlansRepository
import digital.fact.saver.utils.resetTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PlansViewModel(application: Application) : AndroidViewModel(application) {

    val showRecyclerAnim: MutableLiveData<Boolean> = MutableLiveData(true)

    private val plansRepository: PlansRepository
    private val prefs = PreferenceManager.getDefaultSharedPreferences(application)

    private val _period: MutableLiveData<Period> = MutableLiveData()
    val period: LiveData<Period> = _period

    init {
        plansRepository = PlansRepositoryIml()
        getPeriod()
    }

    fun getPeriod() {
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = Date()
            val currentTime = calendar.time.time
            calendar.add(Calendar.DATE, 30)
            val timeNextMonth = calendar.time.time

            val timeFrom = prefs.getLong(PREF_PLANNED_PERIOD_FROM, resetTimeInMillis(currentTime))
            val timeTo = prefs.getLong(PREF_PLANNED_PERIOD_TO, resetTimeInMillis(timeNextMonth))
            val calendarFrom = Calendar.getInstance(Locale.getDefault())
            calendarFrom.timeInMillis = timeFrom
            val calendarTo = Calendar.getInstance(Locale.getDefault())
            calendarTo.timeInMillis = timeTo

            _period.postValue(
                    Period(
                            calendarFrom,
                            calendarTo
                    )
            )
        }
    }

    fun getAllPlans(): LiveData<List<DbPlan>> {
        return plansRepository.getAll()
    }

    fun insertPlan(dbPlan: DbPlan): LiveData<Long> {
        return plansRepository.insert(dbPlan)
    }

    fun deletePlan(dbPlan: DbPlan): LiveData<Int> {
        return plansRepository.delete(dbPlan)
    }

    fun updatePlan(dbPlan: DbPlan): LiveData<Int> {
        return plansRepository.update(dbPlan)
    }

    fun updatePlans() {
        plansRepository.updateAll()
    }

    private val _plansBlurViewHeight = MutableLiveData(0)
    val plansBlurViewHeight: LiveData<Int> = _plansBlurViewHeight

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }
}