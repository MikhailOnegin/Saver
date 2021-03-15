package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import digital.fact.saver.data.repositories.*
import digital.fact.saver.domain.models.*
import digital.fact.saver.domain.repository.*

class TestDatabaseViewModel(application: Application): AndroidViewModel(application) {

    private var classesRepository: ClassesRepository
    private var operationsRepository:OperationsRepository
    private var plansRepository: PlansRepository
    private var sourcesRepository: SourcesRepository
    private var  templatesRepository: TemplatesRepository

    var classes: LiveData<List<Class>> = MutableLiveData()
    var operations: LiveData<List<Operation>> = MutableLiveData()
    var plans: LiveData<List<Plan>> = MutableLiveData()
    var sources:LiveData<List<Source>> = MutableLiveData()
    var templates: LiveData<List<Template>> = MutableLiveData()

    init {
        classesRepository = ClassesRepositoryIml(application)
        operationsRepository = OperationsRepositoryIml(application)
        plansRepository = PlansRepositoryIml(application)
        sourcesRepository = SourcesRepositoryImpl(application)
        templatesRepository = TemplatesRepositoryImpl(application)

        classes = classesRepository.getAll()
        operations = operationsRepository.getAll()
        plans = plansRepository.getAll()
        sources = sourcesRepository.getAll()
        templates = templatesRepository.getAll()
    }

    fun insertClass(item: Class){
        classesRepository.insert(item)
    }

    fun getAllClass(): LiveData<List<Class>>{
        return classes
    }

    fun deleteClass(item: Class){
        classesRepository.delete(item)
    }
    fun updateClass(item: Class){
        classesRepository.update(item)
    }

    fun updateClasses(){
        classesRepository.updateAll()
    }

    fun insertOperation(item: Operation){
        operationsRepository.insert(item)
    }

    fun getAllOperations(): LiveData<List<Operation>>{
        return operations
    }

    fun deleteOperation(item: Operation){
        operationsRepository.delete(item)
    }
    fun updateOperation(item: Operation){
        operationsRepository.update(item)
    }

    fun updateOperations(){
        operationsRepository.updateAll()
    }

    fun insertPlan(item: Plan){
        plansRepository.insert(item)
    }

    fun getAllPlans(): LiveData<List<Plan>>{
        return plans
    }

    fun deletePlan(item: Plan){
        plansRepository.delete(item)
    }

    fun updatePlan(item: Plan){
        plansRepository.update(item)
    }

    fun updatePlans(){
        plansRepository.updateAll()
    }

    fun insertSource(item: Source){
        sourcesRepository.insert(item)
    }

    fun getAllSources(): LiveData<List<Source>>{
        return sources
    }

    fun deleteSource(item: Source){
        sourcesRepository.delete(item)
    }
    fun updateSource(item: Source){
        sourcesRepository.update(item)
    }

    fun updateSources(){
        sourcesRepository.updateAll()
    }

    fun insertTemplate(item: Template){
        templatesRepository.insert(item)
    }

    fun getAllTemplates(): LiveData<List<Template>>{
        return templates
    }

    fun deleteTemplate(item: Template){
        templatesRepository.delete(item)
    }
    fun updateTemplate(item: Template){
        templatesRepository.update(item)
    }

    fun updateTemplates(){
        templatesRepository.updateAll()
    }
}