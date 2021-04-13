package digital.fact.saver.presentation.fragments.savers.saver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import digital.fact.saver.App
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.toSource
import digital.fact.saver.presentation.fragments.savers.newSaver.NewSaverViewModel
import digital.fact.saver.utils.fillSourceWithCurrentSumForToday
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SaverViewModel : ViewModel() {

    private val _saver = MutableLiveData<Sources>()
    val saver: LiveData<Sources> = _saver

    fun initialize(saverId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val saver = App.db.sourcesDao().getSource(saverId).toSource()
            fillSourceWithCurrentSumForToday(saver)
            aimSum = saver.aimSum
            _aimDate.postValue(Date(saver.aimDate))
            _saver.postValue(saver)
        }
    }

    private val _aimDate = MutableLiveData<Date>()
    val aimDate: LiveData<Date> = _aimDate

    fun setAimDate(date: Date) {
        _aimDate.value = date
        updateDailyFee()
    }

    var visibility = 0
        private set

    fun setVisibility(visibility: Int) {
        this.visibility = visibility
    }

    private var aimSum = 0L

    fun setAimSum(sum: Long) {
        aimSum = sum
        updateDailyFee()
    }

    private val _dailyFee = MutableLiveData(0L)
    val dailyFee: LiveData<Long> = _dailyFee

    private fun updateDailyFee() {
        val remains = aimSum - (saver.value?.currentSum ?: 0L)
        _dailyFee.value = NewSaverViewModel
            .calculateDailyFee(_aimDate.value ?: Date(), remains)
    }

    init {
        saver.observeForever { updateDailyFee() }
    }

}