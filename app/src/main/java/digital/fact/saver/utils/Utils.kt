package digital.fact.saver.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("SimpleDateFormat")
 fun Long.toDate(): String {
    val date = Date(this)
    val format = SimpleDateFormat("dd/MM/yyyy")
    return format.format(date)
}

@RequiresApi(Build.VERSION_CODES.O)
fun String.toUnixLong(formatter: SimpleDateFormat): Long{
    var result: Long = 0
    try {
        val l = LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        //  В таблице Plan нужен Int (!)
        result = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
    }
    catch (e: Exception){

    }
return result
}
fun Long.toDateString(formatter: SimpleDateFormat): String{
    var result = ""
    try {
        val netDate = Date(this)
        result =  formatter.format(netDate)

    } catch (e: Exception) {

    }
    return result
}

fun showNotReadyToast(context: Context) {
    Toast.makeText(context, "В разработке.", Toast.LENGTH_SHORT).show()
}

fun getFormattedDateForHistory(date: Date): String {
    val locale = Locale.getDefault()
    val historyDateFormat = SimpleDateFormat("LLLL, yyyy", locale)
    return historyDateFormat.format(date).capitalize(locale)
}

enum class WordEnding { TYPE_1, TYPE_2, TYPE_3 }

fun getWordEndingType(count: Int): WordEnding {
    return when {
        count % 100 in 11..19 -> WordEnding.TYPE_3
        count % 10 == 1 -> WordEnding.TYPE_1
        count % 10 in 2..4 -> WordEnding.TYPE_2
        else -> WordEnding.TYPE_3
    }
}
