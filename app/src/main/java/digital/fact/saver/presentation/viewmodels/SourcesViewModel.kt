package digital.fact.saver.presentation.viewmodels

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.utils.events.OneTimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SourcesViewModel() : ViewModel() {

    private var _sources: MutableLiveData<List<DbSource>> = MutableLiveData()
    val sources: LiveData<List<DbSource>> = _sources
    val deleteSourceEvent = MutableLiveData<OneTimeEvent>()

    init {
        getAllSources()
    }

    fun insertSource(item: DbSource) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().insert(item) }
    }

    fun getAllSources() {
        viewModelScope.launch(Dispatchers.IO) {
            _sources.postValue(App.db.sourcesDao().getAll())
        }
    }

    fun deleteSource(item: DbSource) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().delete(item) }
        deleteLinkedOperations(item)
    }

    fun updateSource(item: DbSource) {
        viewModelScope.launch(Dispatchers.IO) { App.db.sourcesDao().update(item) }
    }

    private fun deleteLinkedOperations(item: DbSource) {
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