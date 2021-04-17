package digital.fact.saver.presentation.activity

import android.annotation.SuppressLint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.preference.PreferenceManager
import digital.fact.saver.App
import digital.fact.saver.utils.resetTimeInMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {

    //sergeev: Перевести все запросы на эти переменные вместо чтения SharedPreferences.
    private val _periodStart = MutableLiveData<Long>()
    val periodStart: LiveData<Long> = _periodStart

    private val _periodEnd = MutableLiveData<Long>()
    val periodEnd: LiveData<Long> = _periodEnd

    private val _conditionsChanged = MutableLiveData(true)
    val conditionsChanged: LiveData<Boolean> = _conditionsChanged

    /**
     * Используется для отправки уведомления о том, что исходные данные для расчетов
     * изменились (источники, операции, планы, или настройки планируемого периода).
     */
    fun notifyConditionsChanged() {
        try {
            _conditionsChanged.value = true
        } catch (exc: IllegalStateException) {
            _conditionsChanged.postValue(true)
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun initializePeriod() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance())
        if (!prefs.contains(PREF_PERIOD_START)) {
            prefs.edit().putLong(PREF_PERIOD_START, getDefaultPeriodStart())
                    .commit()
        }
        if (!prefs.contains(PREF_PERIOD_END)) {
            prefs.edit().putLong(PREF_PERIOD_END, getDefaultPeriodEnd())
                    .commit()
        }
        _periodStart.value = prefs.getLong(PREF_PERIOD_START, getDefaultPeriodStart())
        _periodEnd.value = prefs.getLong(PREF_PERIOD_END, getDefaultPeriodEnd())
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

    fun updatePeriod(start: Long, end: Long) {
        _periodStart.value = resetTimeInMillis(start)
        _periodEnd.value = resetTimeInMillis(end)
        notifyConditionsChanged()
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance())
            prefs.edit()
                .putLong(PREF_PERIOD_START, resetTimeInMillis(start))
                .putLong(PREF_PERIOD_END, resetTimeInMillis(end))
                .apply()
        }
    }

    init {
        initializePeriod()
    }

    companion object {

        const val PREF_PERIOD_START = "pref_period_start"
        const val PREF_PERIOD_END = "pref_period_end"

    }

}