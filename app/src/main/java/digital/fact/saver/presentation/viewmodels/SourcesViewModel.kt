package digital.fact.saver.presentation.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import digital.fact.saver.data.repositories.*
import digital.fact.saver.domain.models.*
import digital.fact.saver.domain.repository.*

class SourcesViewModel(application: Application) : AndroidViewModel(application) {

    private var sourcesRepository: SourcesRepository
    var sources: LiveData<List<Source>> = MutableLiveData()

    init {
        sourcesRepository = SourcesRepositoryImpl(application)
        sources = sourcesRepository.getAll()
    }

    fun insertSource(item: Source) {
        sourcesRepository.insert(item)
    }

    fun getAllSources(): LiveData<List<Source>> {
        return sources
    }

    fun deleteSource(item: Source) {
        sourcesRepository.delete(item)
    }

    fun updateSource(item: Source) {
        sourcesRepository.update(item)
    }

    fun updateSources() {
        sourcesRepository.updateAll()
    }

}