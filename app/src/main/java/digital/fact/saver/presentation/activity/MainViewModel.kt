package digital.fact.saver.presentation.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import digital.fact.saver.utils.getDaysDifference
import java.util.*
import kotlin.math.absoluteValue

class MainViewModel : ViewModel() {

    private val _currentDate = MutableLiveData(Date())
    val currentDate: LiveData<Date> = _currentDate
    var shouldRecreateHistoryAdapter = false
        private set
    private val _periodDaysLeft = MutableLiveData<Int>()
    val periodDaysLeft: LiveData<Int> = _periodDaysLeft
    private val _historyBlurViewHeight = MutableLiveData(0)
    val historyBlurViewHeight: LiveData<Int> = _historyBlurViewHeight

    fun setCurrentDate(newDate: Date) {
        val oldDate = _currentDate.value ?: Date()
        val daysDiff = getDaysDifference(oldDate, newDate)
        shouldRecreateHistoryAdapter = daysDiff.absoluteValue > 3
        _currentDate.value = newDate
        _periodDaysLeft.value = 0 //sergeev: Устанавливать дни до конца периода.
    }

    fun setHistoryBlurViewWidth(newValue: Int) {
        _historyBlurViewHeight.value = newValue
    }

}