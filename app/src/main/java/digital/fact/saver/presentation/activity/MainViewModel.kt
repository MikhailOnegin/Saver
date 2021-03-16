package digital.fact.saver.presentation.activity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class MainViewModel : ViewModel() {

    private val _currentDate = MutableLiveData<Date>()
    val currentDate: LiveData<Date> = _currentDate
    private val _periodDaysLeft = MutableLiveData<Int>()
    val periodDaysLeft: LiveData<Int> = _periodDaysLeft

    init {
        setCurrentDate(Date())
    }

    fun setCurrentDate(date: Date) {
        _currentDate.value = date
        _periodDaysLeft.value = 0 //sergeev: Устанавливать дни до конца периода.
    }

}