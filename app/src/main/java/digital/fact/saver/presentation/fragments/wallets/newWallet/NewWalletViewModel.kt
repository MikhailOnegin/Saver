package digital.fact.saver.presentation.fragments.wallets.newWallet

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.events.OneTimeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NewWalletViewModel(
    private val mainVM: MainViewModel
) : ViewModel() {

    private val _walletCreatedEvent = MutableLiveData<OneTimeEvent>()
    val walletCreatedEvent: LiveData<OneTimeEvent> = _walletCreatedEvent

    private val _creationDate = MutableLiveData(Date())
    val creationDate: LiveData<Date> = _creationDate

    fun setCreationDate(date: Date) {
        _creationDate.value = date
    }

    var type = DbSource.Type.ACTIVE.value
        private set

    fun setType(type: Int) { this.type = type }

    private var startSum = 0L

    fun setStartSum(startSum: Long) { this.startSum = startSum }

    fun createWallet(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            App.db.sourcesDao().insert(DbSource(
                name = name,
                type = type,
                start_sum = startSum,
                adding_date = _creationDate.value?.time ?: Date().time,
                aim_sum = 0L,
                aim_date = 0L,
                sort_order = 0,
                visibility = DbSource.Visibility.VISIBLE.value
            ))
            _walletCreatedEvent.postValue(OneTimeEvent())
            mainVM.notifyConditionsChanged()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class NewWalletVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NewWalletViewModel(mainVM) as T
        }
    }

}