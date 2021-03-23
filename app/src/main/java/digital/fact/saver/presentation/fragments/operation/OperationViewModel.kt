package digital.fact.saver.presentation.fragments.operation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*

class OperationViewModel : ViewModel() {

    private val _date = MutableLiveData(Date())
    val date: LiveData<Date> = _date

    fun setOperationDate(time: Long) {
        _date.value = Date(time)
    }

}