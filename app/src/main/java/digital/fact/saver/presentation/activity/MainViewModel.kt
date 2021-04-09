package digital.fact.saver.presentation.activity

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.presentation.viewmodels.PeriodViewModel
import digital.fact.saver.utils.getDaysDifference
import digital.fact.saver.utils.resetTimeInMillis
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

    private val _conditionsChanged = MutableLiveData<Boolean>()
    val conditionsChanged: LiveData<Boolean> = _conditionsChanged

    /**
     * Используется для отправки уведомления о том, что исходные данные для расчетов
     * изменились (источники, операции, планы, или настройки планируемого периода).
     */
    fun sendConditionsChangedNotification() {
        try {
            _conditionsChanged.value = true
        } catch (exc: IllegalStateException) {
            _conditionsChanged.postValue(true)
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun initializePeriod() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance())
        if (!prefs.contains(PeriodViewModel.PREF_PERIOD_START)) {
            prefs.edit().putLong(PeriodViewModel.PREF_PERIOD_START, getDefaultPeriodStart())
                    .commit()
        }
        if (!prefs.contains(PeriodViewModel.PREF_PERIOD_END)) {
            prefs.edit().putLong(PeriodViewModel.PREF_PERIOD_END, getDefaultPeriodEnd())
                    .commit()
        }
    }

    private fun getDefaultPeriodStart(): Long {
        return resetTimeInMillis(Date().time)
    }

    private fun getDefaultPeriodEnd(): Long {
        val start = resetTimeInMillis(Date().time)
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = start
        calendar.add(Calendar.MONTH, 1)
        return calendar.timeInMillis
    }

    init {
        initializePeriod()
    }

}