package digital.fact.saver.presentation.fragments.operation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.StringBuilder
import java.util.*

class OperationViewModel : ViewModel() {

    private val _date = MutableLiveData(Date())
    val date: LiveData<Date> = _date

    fun setOperationDate(time: Long) {
        _date.value = Date(time)
    }

    private val _sum = MutableLiveData<String>()
    val sum: LiveData<String> = _sum
    private val builder = StringBuilder()

    fun onKeyboardButtonClicked(input: String) {
        val chars = charsCountAfterComma()
        if (chars >= 2 || builder.length >= 12) return
        if (builder.length == 1 && builder.first() == '0' && input != ",") return
        builder.append(input)
        _sum.value = builder.toString()
    }

    fun onBackspaceClicked() {
        if (builder.isNotEmpty()) builder.deleteCharAt(builder.length - 1)
        _sum.value = builder.toString()
    }

    private fun builderHasComma(): Boolean {
        return builder.contains(',')
    }

    private fun charsCountAfterComma(): Int {
        if (builderHasComma()) {
            val commaIndex = builder.indexOf(",")
            return builder.length - commaIndex - 1
        }
        return 0
    }

    init {
        _sum.value = builder.toString()
    }

}