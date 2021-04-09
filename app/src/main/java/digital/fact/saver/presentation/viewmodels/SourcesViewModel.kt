package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.data.repositories.*
import digital.fact.saver.domain.repository.*
import digital.fact.saver.utils.events.Event
import digital.fact.saver.utils.events.OneTimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourcesViewModel() : ViewModel() {

    private var _sources: MutableLiveData<List<Source>> = MutableLiveData()
    val sources: LiveData<List<Source>> = _sources
    val deleteSourceEvent = MutableLiveData<OneTimeEvent>()

    init {
        getAllSources()
    }

    fun insertSource(item: Source) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().insert(item) }
    }

    fun getAllSources() {
        viewModelScope.launch(Dispatchers.IO) {
            _sources.postValue(App.db.sourcesDao().getAll())
        }
    }

    fun deleteSource(item: Source) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().delete(item) }
        deleteLinkedOperations(item)
    }

    fun updateSource(item: Source) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().update(item) }
    }

    private fun deleteLinkedOperations(item: Source) {
        viewModelScope.launch(Dispatchers.IO) {
            val linkedOperations = App.db.operationsDao().getAll().filter {
                it.from_source_id == item._id || it.to_source_id == item._id
            }
            linkedOperations.forEach {
                App.db.operationsDao().delete(it)
                if (it.plan_id != 0L) App.db.plansDao().deleteById(it.plan_id)
            }
        }
    }

}