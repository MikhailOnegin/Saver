package digital.fact.saver.utils

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
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.domain.models.PlanStatus
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

const val decimalSeparator = '.'

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

fun getToday(date: Date): Date {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.time = date
    calendar.set(Calendar.MILLISECOND, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    return calendar.time
}

fun getDaysDifference(end: Date, start: Date): Long {
    val diffInMillis = resetTimeInMillis(end.time) - resetTimeInMillis(start.time)
    return diffInMillis / (24L * 60L * 60L * 1000L)
}

fun Long.formatToMoney(needSpaces: Boolean = true): String {
    val builder = StringBuilder(this.toString())
    val minimumLength = if (this >= 0) 3 else 4
    val insertIndex = if (this >= 0) 0 else 1
    while (builder.length < minimumLength) builder.insert(insertIndex, '0')
    builder.insert(builder.length - 2, decimalSeparator)
    if (!needSpaces) {
        if (builder[0] == '-') builder.insert(1, " ")
        return builder.toString()
    }
    val builderWithSpaces = StringBuilder()
    var groupCounter = 0
    for (i in 0..builder.lastIndex) {
        if (i < 3) {
            builderWithSpaces.insert(0, builder[builder.lastIndex - i])
            continue
        }
        else {
            if (groupCounter == 3) {
                if (builder[builder.lastIndex - i] != '-') {
                    builderWithSpaces.insert(0, " ")
                    groupCounter = 0
                }
            }
            builderWithSpaces.insert(0, builder[builder.lastIndex - i])
            groupCounter++
        }
    }
    if (builderWithSpaces[0] == '-') builderWithSpaces.insert(1, " ")
    return builderWithSpaces.toString()
}

fun String?.toLongFormatter(): Long {
    return if (this.isNullOrEmpty()) 0L
    else this.toFloat().times(100F).toLong()
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


fun round(value: Double, places: Int): Double {
    require(places >= 0)
    var bd = BigDecimal(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)
    return bd.toDouble()
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
        val mainPart = if (contains(decimalSeparator)) {
            val indexOfComma = indexOf(decimalSeparator)
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

fun getLongSumFromString(text: String, signed: Boolean = false): Long {
    if (text.isEmpty()) return 0L
    val builder = StringBuilder(text.removeWhiteSpaces())
    if (text.contains(decimalSeparator)) {
        val dotIndex = builder.indexOf(decimalSeparator)
        while (builder.length - dotIndex < 3) builder.append('0')
        builder.deleteCharAt(dotIndex)
    } else builder.append("00")
    val parsedValue = try {
        builder.toString().toLong()
    } catch (exc: NumberFormatException) {
        0L
    }
    return if (signed) parsedValue else abs(parsedValue)
}

fun String.removeWhiteSpaces(): String {
    val builder = StringBuilder()
    for (char in this) if (!char.isWhitespace()) builder.append(char)
    return builder.toString()
}

fun getSumStringFromLong(sum: Long): String {
    val builder = StringBuilder(sum.toString())
    while (builder.length < 3) builder.insert(0, '0')
    builder.insert(builder.length - 2, decimalSeparator)
    return builder.toString()
}

fun getMonthBefore(date: Date): Date {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.time = date
    calendar.add(Calendar.MONTH, -1)
    return calendar.time
}

fun getMonthAfter(date: Date): Date {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.time = date
    calendar.add(Calendar.MONTH, 1)
    return calendar.time
}

fun getYearBefore(date: Date): Date {
    val calendar = Calendar.getInstance(Locale.getDefault())
    calendar.time = date
    calendar.add(Calendar.YEAR, -1)
    return calendar.time
}

object UniqueIdGenerator {

    private var uniqueId = 0L

    fun nextId() = ++uniqueId

}

fun checkPlanStatus(plan: DbPlan, unixFrom: Long, unixTo: Long): PlanStatus? {
    return if (plan.operation_id == 0L && plan.planning_date in unixFrom .. unixTo || plan.planning_date == 0L)
        PlanStatus.CURRENT
    else if (plan.operation_id != 0L && plan.planning_date in unixFrom .. unixTo && plan.planning_date != 0L)
        PlanStatus.DONE
    else if (plan.operation_id != 0L && plan.planning_date !in unixFrom .. unixTo && plan.planning_date != 0L)
        PlanStatus.DONE_OUTSIDE
    else if( plan.planning_date !in unixFrom .. unixTo && plan.planning_date != 0L  || plan.planning_date != 0L)
        PlanStatus.OUTSIDE
    else null
}

fun sumToString(sum: Long): String {
    val bd = BigDecimal(sum.toDouble() / 100)
    return bd.setScale(2, RoundingMode.HALF_UP).toString()
}