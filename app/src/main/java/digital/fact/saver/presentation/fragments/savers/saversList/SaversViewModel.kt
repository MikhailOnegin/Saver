package digital.fact.saver.presentation.fragments.savers.saversList

import androidx.lifecycle.*
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.toSaversList
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.getAllSaversForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SaversViewModel(
    mainVM: MainViewModel
) : ViewModel() {

    private lateinit var originList: List<Source>
    private val _savers = MutableLiveData<List<SourceItem>>(listOf())
    val savers: LiveData<List<SourceItem>> = _savers
    private var showInvisible = false

    fun switchVisibilityMode() {
        showInvisible = !showInvisible
        updateSourceItemsList()
    }

    private fun updateSourceItemsList() {
        viewModelScope.launch(Dispatchers.IO) {
            _savers.postValue(originList.toSaversList(showInvisible))
        }
    }

    private fun invalidateSavers() {
        viewModelScope.launch(Dispatchers.IO) {
            originList = getAllSaversForADate(Date())
            updateSourceItemsList()
        }
    }

    init {
        mainVM.conditionsChanged.observeForever { invalidateSavers() }
    }

    @Suppress("UNCHECKED_CAST")
    class SaversVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SaversViewModel(mainVM) as T
        }
    }

}