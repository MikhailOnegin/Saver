package digital.fact.saver.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

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