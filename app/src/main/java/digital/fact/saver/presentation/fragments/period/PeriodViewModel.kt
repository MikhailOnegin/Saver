package digital.fact.saver.presentation.fragments.period

import androidx.lifecycle.*
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.getAverageDailyExpenses
import digital.fact.saver.utils.getDaysDifference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class PeriodViewModel(
        private val mainVM: MainViewModel
) : ViewModel() {

    private val _averageDailyExpenses = MutableLiveData(Pair(0L, 0L))
    val averageDailyExpenses: LiveData<Pair<Long, Long>> = _averageDailyExpenses

    private fun updateAverageDailyExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            _averageDailyExpenses.postValue(Pair(
                    _averageDailyExpenses.value?.second ?: 0L,
                    getAverageDailyExpenses(
                            periodStart = mainVM.periodStart.value ?: 0L,
                            periodEnd = mainVM.periodEnd.value ?: 0L
                    )))
        }
    }

    fun notifyAnimationEnd() {
        _averageDailyExpenses.value = Pair(
                first = _averageDailyExpenses.value?.second ?: 0L,
                second = _averageDailyExpenses.value?.second ?: 0L
        )
    }

    private val _periodLength = MutableLiveData<Long>()
    val periodLength: LiveData<Long> = _periodLength

    private fun updatePeriodLength() {
        viewModelScope.launch(Dispatchers.IO) {
            _periodLength.postValue(getDaysDifference(
                    end = Date(mainVM.periodEnd.value ?: 0L),
                    start = Date(mainVM.periodStart.value ?: 0L)
            ))
        }
    }

    init {
        mainVM.conditionsChanged.observeForever {
            updateAverageDailyExpenses()
            updatePeriodLength()
        }
    }

    @Suppress("UNCHECKED_CAST")
    class PeriodVMFactory(
            private val mainVM: MainViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return PeriodViewModel(mainVM) as T
        }
    }

}