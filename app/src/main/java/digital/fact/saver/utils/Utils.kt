package digital.fact.saver.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import digital.fact.saver.App
import digital.fact.saver.R
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

fun createSnackBar(
        anchorView: View,
        text: String?,
        buttonText: String? = null,
        onButtonClicked: (() -> Unit)? = null
): Snackbar {
    val snackBar = Snackbar.make(
            anchorView,
            text.toString(),
            Snackbar.LENGTH_SHORT
    )
    snackBar.setBackgroundTint(ContextCompat.getColor(App.getInstance(), R.color.colorAccent))
    snackBar.setTextColor(ContextCompat.getColor(App.getInstance(), android.R.color.white))
    snackBar.setActionTextColor(ContextCompat.getColor(App.getInstance(), android.R.color.white))
    val textView =
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.maxLines = 10
    if (buttonText != null) {
        snackBar.setAction(buttonText) {
            onButtonClicked?.invoke() ?: snackBar.dismiss()
        }
        snackBar.duration = Snackbar.LENGTH_INDEFINITE
    }
    return snackBar
}

fun resetTimeInMillis(timeInMillis: Long): Long {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.timeInMillis = timeInMillis
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    return calendar.timeInMillis
}

fun getTomorrow(date: Date): Date {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.time = date
    calendar.add(Calendar.DAY_OF_YEAR, 1)
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    return calendar.time
}

fun getDaysDifference(date_1: Date, date_2: Date): Long {
    val diffInMillis = resetTimeInMillis(date_1.time) - resetTimeInMillis(date_2.time)
    return diffInMillis / (24L * 60L * 60L * 1000L)
}

fun Long.formatToMoney(needSpaces: Boolean = true): String {
    val builder = StringBuilder(this.toString())
    if (builder.length == 1) {
        builder.insert(0, "00")
        builder.insert(builder.lastIndex - 1, '.')
    } else if (builder.length == 2) {
        builder.insert(0, '0')
        builder.insert(builder.lastIndex - 1, '.')
    } else if (builder.length == 3) {
        builder.insert(builder.lastIndex - 1, '.')
    } else {
        if (needSpaces) {
            val length = builder.length - 2
            val spaces = length / 3
            val offset = length % 3
            for (i in 1..spaces) {
                if (spaces == i && offset == 0) {
                    break
                }
                builder.insert(length - (3 * i), ' ')
            }
        }
        builder.insert(builder.lastIndex - 1, '.')
    }
    return builder.toString()
}

fun String?.toLongFormatter(): Long {
    return if (this.isNullOrEmpty()) 0L
    else this.toFloat().times(100F).toLong()
}

// Устанавливает margin top для первого элемента в ресайклере
fun RecyclerView.addCustomItemDecorator(margin: Int) {
    this.apply {
        addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
            ) {
                if (getChildAdapterPosition(view) == 0) {
                    outRect.top = margin
                } else outRect.top = 0
            }
        })
    }
}

fun RecyclerView.removeItemsDecorations(){
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}

fun Long.toDateString(formatter: SimpleDateFormat): String {
    var result = ""
    try {
        val netDate = Date(this)
        result = formatter.format(netDate)

    } catch (e: Exception) {

    }
    return result
}

@Suppress("unused")
fun showNotReadyToast(context: Context) {
    Toast.makeText(context, "В разработке.", Toast.LENGTH_SHORT).show()
}

fun getFormattedDateForHistory(date: Date): String {
    val locale = Locale.getDefault()
    val historyDateFormat = SimpleDateFormat("LLLL, yyyy", locale)
    return historyDateFormat.format(date).capitalize(locale)
}

enum class WordEnding { TYPE_1, TYPE_2, TYPE_3 }

fun getWordEndingType(count: Long): WordEnding {
    return when {
        count % 100L in 11L..19L -> WordEnding.TYPE_3
        count % 10L == 1L -> WordEnding.TYPE_1
        count % 10L in 2L..4L -> WordEnding.TYPE_2
        else -> WordEnding.TYPE_3
    }
}

// Округляет Float до заданных колчичеств знаков после точки
fun round(value: Double, places: Int): Double {
    require(places >= 0)
    var bd = BigDecimal(value.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}

// Анимированное изменение числа в TextView
@SuppressLint("SetTextI18n")
fun startCountAnimation(
        view: TextView,
        fromNumber: Float,
        toNumber: Float,
        duration: Long,
        places: Int
) {
    val animator = ValueAnimator.ofFloat(fromNumber, toNumber)
    animator.duration = duration
    animator.addUpdateListener { animation ->
        val number: Float = animation.animatedValue as Float
        require(places >= 0)
        var bd = BigDecimal(number.toString())
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        view.text = bd.toString()
    }
    animator.start()
}

class LinearRvItemDecorations(
        sideMarginsDimension: Int? = null,
        marginBetweenElementsDimension: Int? = null,
        private val drawTopMarginForFirstElement: Boolean = true
) : RecyclerView.ItemDecoration() {

    private val res = App.getInstance().resources
    private val sideMargins =
        if (sideMarginsDimension != null)
            res.getDimension(sideMarginsDimension).toInt()
        else 0
    private val verticalMargin =
        if (marginBetweenElementsDimension != null)
            res.getDimension(marginBetweenElementsDimension).toInt()
        else 0

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        outRect.set(
                sideMargins,
                if (drawTopMarginForFirstElement && position == 0) sideMargins else 0,
                sideMargins,
                if (position + 1 == parent.adapter?.itemCount) sideMargins else verticalMargin
        )
    }

}

fun getFullFormattedDate(date: Date): String {
    val fullDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    return fullDateFormat.format(date)
}

fun String.insertGroupSeparators(): String {
    return try {
        var decimalPart: String? = null
        val mainPart = if (contains(",")) {
            val indexOfComma = indexOf(",")
            decimalPart = substring(indexOfComma, this.length)
            substring(0, indexOfComma)
        } else this
        val builder = StringBuilder()
        var spacesCount = 0
        for (i in mainPart.length - 1 downTo 0) {
            if (builder.isNotEmpty() && (builder.length - spacesCount) % 3 == 0) {
                builder.insert(0, " ")
                spacesCount++
            }
            builder.insert(0, mainPart[i])
        }
        if (!decimalPart.isNullOrBlank()) builder.append(decimalPart)
        builder.toString()
    } catch (exc: Exception) {
        this
    }
}

fun getLongSumFromString(text: String): Long {
    if (text.isEmpty()) return 0L
    val builder = StringBuilder(text)
    if (text.contains(",")) {
        val dotIndex = builder.indexOf(",")
        while (builder.length - dotIndex != 3) builder.append('0')
        builder.deleteCharAt(dotIndex)
    } else builder.append("00")
    val parsedValue = try {
        builder.toString().toLong()
    } catch (exc: NumberFormatException) {
        0L
    }
    return abs(parsedValue)
}

fun getSumStringFromLong(sum: Long): String {
    val builder = StringBuilder(sum.toString())
    while (builder.length < 3) builder.insert(0, '0')
    builder.insert(builder.length - 2, ',')
    return builder.toString()
}