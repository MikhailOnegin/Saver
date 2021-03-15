package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import digital.fact.saver.data.repositories.PlansRepositoryIml
import digital.fact.saver.data.repositories.SourcesRepositoryImpl
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.repository.PlansRepository
import digital.fact.saver.domain.repository.SourcesRepository

class PlansViewModel(application: Application): AndroidViewModel(application) {

    private val plansRepository: PlansRepository
    private val sourceRepository: SourcesRepository

    init {
        plansRepository = PlansRepositoryIml(application)
        sourceRepository = SourcesRepositoryImpl(application)
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