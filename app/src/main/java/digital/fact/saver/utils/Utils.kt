package digital.fact.saver.utils

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.App
import digital.fact.saver.R
import java.lang.StringBuilder
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

fun Long.toStringFormatter(needSpaces: Boolean = true): String {
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

fun String.toLongFormatter(): Long {
    return (this.toFloat() * 100F).toLong()
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

fun Long.toDateString(formatter: SimpleDateFormat): String {
    var result = ""
    try {
        val netDate = Date(this)
        result = formatter.format(netDate)

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
fun round(value: Double, places: Int): Double {
    require(places >= 0)
    var bd = BigDecimal(value.toString())
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
}

// Анимированное изменение числа в TextView
fun startCountAnimation(view: TextView, fromNumber: Float, toNumber: Float, duration: Long, places: Int) {
    val animator = ValueAnimator.ofFloat(fromNumber, toNumber)
    animator.duration = duration
    animator.addUpdateListener { animation ->
        val number: Float = animation.animatedValue as Float
        require(places >= 0)
        var bd = BigDecimal(number.toString())
        bd = bd.setScale(places, RoundingMode.HALF_UP)
        view.text = bd.toDouble().toString() }
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
                if (drawTopMarginForFirstElement && position == 0) verticalMargin else 0,
                sideMargins,
                verticalMargin
        )
    }

}