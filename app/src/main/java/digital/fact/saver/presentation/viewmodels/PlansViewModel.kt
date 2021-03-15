package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import digital.fact.saver.data.repositories.PlansRepositoryIml
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.repository.PlansRepository

class PlansViewModel(application: Application): AndroidViewModel(application) {

    private val plansRepository: PlansRepository

    init {
        plansRepository = PlansRepositoryIml(application)
    }

    fun getAllPlans (): LiveData<List<Plan>>{
        return plansRepository.getAll()
    }

    fun insertPlan (plan: Plan): LiveData<Long> {
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

}