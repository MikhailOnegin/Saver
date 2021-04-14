package digital.fact.saver.presentation.fragments.savers.newSaver

import androidx.lifecycle.*
import digital.fact.saver.App
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.getDaysDifference
import digital.fact.saver.utils.getMonthAfter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class NewSaverViewModel(
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
        _dailyFee.value = calculateDailyFee(_aimDate.value ?: Date(), aimSum)
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
        _aimDate.value = getMonthAfter(Date())
    }

    companion object {

        fun calculateDailyFee(aimDate: Date, aimSum: Long): Long {
            val daysDiff = getDaysDifference(aimDate, Date())
            return if (daysDiff <= 0) 0L else aimSum / daysDiff
        }

    }

    @Suppress("UNCHECKED_CAST")
    class SaverVMFactory(
        private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return NewSaverViewModel(mainVM) as T
        }
    }

}