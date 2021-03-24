package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import digital.fact.saver.data.repositories.PlansRepositoryIml
import digital.fact.saver.domain.models.Period
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.repository.PlansRepository
import java.util.*

class PlansViewModel(application: Application): AndroidViewModel(application) {

    private val plansRepository: PlansRepository

    init {
        plansRepository = PlansRepositoryIml(application)
    }

    private val _period: MutableLiveData<Period> = MutableLiveData()
    val period: LiveData<Period> = _period

    fun getPeriod(){
         val calendarFrom: Calendar = GregorianCalendar()
         calendarFrom[Calendar.YEAR] = 2021
         calendarFrom[Calendar.MONTH] = 2
         calendarFrom[Calendar.DAY_OF_MONTH] = 3
         calendarFrom[Calendar.HOUR_OF_DAY] = 19
         calendarFrom[Calendar.MINUTE] = 42
         calendarFrom[Calendar.SECOND] = 12

         val calendarTo: Calendar = GregorianCalendar()
         calendarTo[Calendar.YEAR] = 2021
         calendarTo[Calendar.MONTH] = 3
         calendarTo[Calendar.DAY_OF_MONTH] = 3
         calendarTo[Calendar.HOUR_OF_DAY] = 19
         calendarTo[Calendar.MINUTE] = 42
         calendarTo[Calendar.SECOND] = 12
         _period.value = Period(calendarFrom, calendarTo)
     }

    fun getAllPlans (): LiveData<List<Plan>>{
        return plansRepository.getAll()
    }

    private val _insertPlan: MutableLiveData<Long> = MutableLiveData()
    val insertPlan: LiveData<Long> = _insertPlan

    fun insertPlan(plan: Plan): LiveData<Long> {
        return plansRepository.insert(plan)
    }

    fun deletePlan(plan: Plan): LiveData<Int>{
        return plansRepository.delete(plan)
    }

    fun updatePlan(plan: Plan): LiveData<Int> {
        return plansRepository.update(plan)
    }

    fun updatePlans(){
        plansRepository.updateAll()
    }


    private val _plansBlurViewHeight = MutableLiveData(0)
    val plansBlurViewHeight: LiveData<Int> = _plansBlurViewHeight

    fun setPlansBlurViewWidth(newValue: Int) {
        _plansBlurViewHeight.value = newValue
    }
}