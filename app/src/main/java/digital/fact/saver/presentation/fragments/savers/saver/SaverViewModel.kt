package digital.fact.saver.presentation.fragments.savers.saver

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.toSource
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.fragments.savers.newSaver.NewSaverViewModel
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.fillSourceWithCurrentSumForToday
import digital.fact.saver.utils.resetTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SaverViewModel(
        private val mainVM: MainViewModel
) : ViewModel() {

    private val _saver = MutableLiveData<Source>()
    val saver: LiveData<Source> = _saver
    private val _aimDate = MutableLiveData<Date>()
    val aimDate: LiveData<Date> = _aimDate
    private val _dailyFee = MutableLiveData(0L)
    val dailyFee: LiveData<Long> = _dailyFee
    private val _hasChanges = MutableLiveData<Boolean>()
    val hasChanges: LiveData<Boolean> = _hasChanges
    private val _exitEvent = MutableLiveData<OneTimeEvent>()
    val exitEvent: LiveData<OneTimeEvent> = _exitEvent
    private val _noNameEvent = MutableLiveData<OneTimeEvent>()
    val noNameEvent: LiveData<OneTimeEvent> = _noNameEvent

    var visibility = 0
        private set

    private var aimSum = 0L
    private var name: String = ""

    fun initialize(saverId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val saver = App.db.sourcesDao().getSource(saverId).toSource()
            fillSourceWithCurrentSumForToday(saver)
            aimSum = saver.aimSum
            _aimDate.postValue(Date(saver.aimDate))
            _saver.postValue(saver)
        }
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setAimDate(date: Date) {
        _aimDate.value = date
        updateDailyFee()
        updateHasChanges()
    }

    fun setVisibility(visibility: Int) {
        this.visibility = visibility
        updateHasChanges()
    }

    fun setAimSum(sum: Long) {
        aimSum = sum
        updateDailyFee()
    }

    private fun updateDailyFee() {
        val remains = aimSum - (saver.value?.currentSum ?: 0L)
        _dailyFee.value = NewSaverViewModel
                .calculateDailyFee(_aimDate.value ?: Date(), remains)
    }

    fun updateHasChanges() {
        if (name != saver.value?.name) {
            _hasChanges.value = true
            return
        }
        if (aimSum != saver.value?.aimSum) {
            _hasChanges.value = true
            return
        }
        if (resetTimeInMillis(aimDate.value?.time ?: 0L)
                != resetTimeInMillis(saver.value?.aimDate ?: 0L)) {
            _hasChanges.value = true
            return
        }
        if (visibility != saver.value?.visibility) {
            _hasChanges.value = true
            return
        }
        _hasChanges.value = false
    }

    fun saveChanges() {
        if (name.isBlank()) {
            _noNameEvent.value = OneTimeEvent()
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            saver.value?.let {
                App.db.sourcesDao().update(DbSource(
                        _id = it.id,
                        name = name,
                        type = it.type,
                        start_sum = it.startSum,
                        adding_date = it.addingDate,
                        aim_sum = aimSum,
                        aim_date = aimDate.value?.time ?: Date().time,
                        sort_order = it.sortOrder,
                        visibility = visibility
                ))
                mainVM.notifyConditionsChanged()
                _exitEvent.postValue(OneTimeEvent())
            }
        }
    }

    fun deleteSaver() {
        viewModelScope.launch(Dispatchers.IO) {
            saver.value?.let {
                App.db.sourcesDao().deleteSource(it.id)
                mainVM.notifyConditionsChanged()
                _exitEvent.postValue(OneTimeEvent())
            }
        }
    }

    init {
        saver.observeForever { updateDailyFee() }
    }

    @Suppress("UNCHECKED_CAST")
    class SaverVMFactory(
            private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SaverViewModel(mainVM) as T
        }
    }

}