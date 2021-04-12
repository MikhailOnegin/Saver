package digital.fact.saver.presentation.fragments.savers

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.getDaysDifference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SaverViewModel(
    private val mainVM: MainViewModel
) : ViewModel() {

    private val _saverCreatedEvent = MutableLiveData<OneTimeEvent>()
    val saverCreatedEvent: LiveData<OneTimeEvent> = _saverCreatedEvent

    private val _creationDate = MutableLiveData(Date())
    val creationDate: LiveData<Date> = _creationDate

    fun setCreationDate(date: Date) { _creationDate.value = date }

    private val _aimDate = MutableLiveData<Date>()
    val aimDate: LiveData<Date> = _aimDate

    fun setAimDate(date: Date) {
        _aimDate.value = date
        updateDailyFee()
    }

    private var aimSum = 0L

    fun setAimSum(sum: Long) {
        aimSum = sum
        updateDailyFee()
    }

    private val _dailyFee = MutableLiveData(0L)
    val dailyFee: LiveData<Long> = _dailyFee

    private fun updateDailyFee() {
        val daysDiff = getDaysDifference(_aimDate.value ?: Date(), Date())
        if (daysDiff <= 0) _dailyFee.value = 0L
        else _dailyFee.value = aimSum / daysDiff
    }

    fun createSaver(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            App.db.sourcesDao().insert(Source(
                name = name,
                type = Source.Type.SAVER.value,
                start_sum = 0L,
                adding_date = _creationDate.value?.time ?: Date().time,
                aim_sum = aimSum,
                aim_date = _aimDate.value?.time ?: Date().time,
                sort_order = 0,
                visibility = Source.Visibility.VISIBLE.value
            ))
            _saverCreatedEvent.postValue(OneTimeEvent())
            mainVM.sendConditionsChangedNotification()
        }
    }

    init {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.add(Calendar.MONTH, 1)
        _aimDate.value = calendar.time
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