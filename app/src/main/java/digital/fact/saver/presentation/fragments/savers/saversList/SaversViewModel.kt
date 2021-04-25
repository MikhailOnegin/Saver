package digital.fact.saver.presentation.fragments.savers.saversList

import androidx.lifecycle.*
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.toSourceItemsList
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.viewmodels.AbstractSourcesViewModel
import digital.fact.saver.utils.getAllSaversForADateEvening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SaversViewModel(
    mainVM: MainViewModel
) : AbstractSourcesViewModel() {

    private lateinit var originList: List<Source>
    private val _savers = MutableLiveData<List<SourceItem>>(listOf())
    val savers: LiveData<List<SourceItem>> = _savers
    private var showInvisible = false

    override fun switchVisibilityMode() {
        showInvisible = !showInvisible
        applyVisibilityAndUpdateList()
    }

    private fun applyVisibilityAndUpdateList() {
        viewModelScope.launch(Dispatchers.IO) {
            _savers.postValue(
                originList.toSourceItemsList(showInvisible, DbSource.Type.SAVER.value)
            )
        }
    }

    private fun invalidateSavers() {
        viewModelScope.launch(Dispatchers.IO) {
            originList = getAllSaversForADateEvening(Date())
            applyVisibilityAndUpdateList()
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