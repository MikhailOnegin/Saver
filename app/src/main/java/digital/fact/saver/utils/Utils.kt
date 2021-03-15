package digital.fact.saver.utils

import android.annotation.SuppressLint
import android.os.Build
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
fun String.toUnixInt(formatter: SimpleDateFormat): Int{
    var result = 0
    try {
        val l = LocalDate.parse(this, DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        //  В таблице Plan нужен Int (!)
        result = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond.toInt()
    }
    catch (e: Exception){

    }
return result
}
fun Int.toDateString(formatter: SimpleDateFormat): String{
    var result = ""
    try {
        val netDate = Date(this.toLong())
        result =  formatter.format(netDate)

    } catch (e: Exception) {

    }
    return result
}
