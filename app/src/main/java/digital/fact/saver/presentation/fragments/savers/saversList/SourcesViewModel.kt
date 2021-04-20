package digital.fact.saver.presentation.fragments.savers.saversList

import androidx.lifecycle.*
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.getSaversForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SourcesViewModel(
    mainVM: MainViewModel
) : ViewModel() {

    private val _savers = MutableLiveData<List<SourceItem>>(listOf())
    val savers: LiveData<List<SourceItem>> = _savers

    private fun updateSavers() {
        viewModelScope.launch(Dispatchers.IO) {
            //sergeev: Получить корректный List<SourceItem>
            _savers.postValue(getSaversForADate(Date()))
        }
    }

    init {
        mainVM.conditionsChanged.observeForever { updateSavers() }
    }

    @Suppress("UNCHECKED_CAST")
    class SourcesVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SourcesViewModel(mainVM) as T
        }
    }

}