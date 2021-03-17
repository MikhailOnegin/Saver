package digital.fact.saver.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import java.math.BigDecimal
import java.math.RoundingMode
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
        result = l.atStartOfDay(ZoneId.systemDefault()).toInstant().epochSecond
    }
    catch (e: Exception){

    }
return result
}

// Устанавливает margin top для первого элемента в ресайклере
fun RecyclerView.addCustomItemDecorator( margin: Int){
    this.apply {
        addItemDecoration(object: RecyclerView.ItemDecoration(){
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val margin2 = resources.getDimension(R.dimen._32dp).toInt()
                if(getChildAdapterPosition(view) == 0){
                    outRect.top = margin2
                }else outRect.top = 0
            }
        })
    }
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

// Округляет Float до заданных колчичеств знаков после точки
fun round(value: Float, places: Int): Double {
    require(places >= 0)
    var bd = BigDecimal(value.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}

// Анимированное изменение числа в TextView
fun startCountAnimation(view: TextView, fromNumber: Float, toNumber :Float, duration: Long) {
    val animator = ValueAnimator.ofFloat(fromNumber, toNumber)
    animator.addUpdateListener { animation -> view.text = animation.animatedValue.toString() }
    animator.start()
}
