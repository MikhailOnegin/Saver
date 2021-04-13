package digital.fact.saver.utils

import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

class SumInputFilter(digitsBefore: Int = DIGITS_BEFORE_ZERO, digitsAfter: Int = DIGITS_AFTER_ZERO) :
    InputFilter {

    var mPattern: Pattern = Pattern.compile("[0-9]{0,$digitsBefore}(\\.[0-9]{0,$digitsAfter})?")

    override fun filter(
        source: CharSequence,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        val builder = StringBuilder(dest.toString())
        builder.replace(dstart, dend, source.substring(start, end))
        val matcher = mPattern.matcher(builder.toString())
        return if (!matcher.matches()) "" else null
    }

    companion object {
        const val DIGITS_BEFORE_ZERO = 9
        const val DIGITS_AFTER_ZERO = 2
    }
}