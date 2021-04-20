package digital.fact.saver.presentation.fragments.wallets.walletsList

import androidx.lifecycle.*
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.toSourceItemsList
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.viewmodels.AbstractSourcesViewModel
import digital.fact.saver.utils.getAllWalletsForADate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class WalletsViewModel(
    mainVM: MainViewModel
) : AbstractSourcesViewModel() {

    private lateinit var originList: List<Source>
    private val _activeWallets = MutableLiveData<List<SourceItem>>(listOf())
    val activeWallets: LiveData<List<SourceItem>> = _activeWallets
    private val _inactiveWallets = MutableLiveData<List<SourceItem>>(listOf())
    val inactiveWallets: LiveData<List<SourceItem>> = _inactiveWallets
    private var showInvisible = false

    override fun switchVisibilityMode() {
        showInvisible = !showInvisible
        applyVisibilityAndUpdateLists()
    }

    private fun applyVisibilityAndUpdateLists() {
        viewModelScope.launch(Dispatchers.IO) {
            _activeWallets.postValue(
                originList.toSourceItemsList(showInvisible, DbSource.Type.ACTIVE.value)
            )
            _inactiveWallets.postValue(
                originList.toSourceItemsList(showInvisible, DbSource.Type.INACTIVE.value)
            )
        }
    }

    private fun invalidateWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            originList = getAllWalletsForADate(Date())
            applyVisibilityAndUpdateLists()
        }
    }

    init {
        mainVM.conditionsChanged.observeForever { invalidateWallets() }
    }

    @Suppress("UNCHECKED_CAST")
    class WalletsVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return WalletsViewModel(mainVM) as T
        }
    }

}