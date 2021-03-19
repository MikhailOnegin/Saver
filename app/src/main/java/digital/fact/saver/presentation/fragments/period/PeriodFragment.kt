package digital.fact.saver.presentation.fragments.period

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPeriodBinding
import digital.fact.saver.presentation.activity.MainViewModel
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class PeriodFragment : Fragment() {

    private lateinit var binding: FragmentPeriodBinding
    private lateinit var mainVM: MainViewModel
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val builder = MaterialDatePicker.Builder.dateRangePicker()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeriodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setData()
        setListeners()
    }

    private fun setData() {
        setDateToButton(readFromPrefs(), readFromPrefs(false))
        setSelectionForPeriod(builder)
    }

    private fun setListeners() {
        binding.period.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        builder.setTheme(R.style.Calendar)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            writeToPrefs(it.first, it.second)
            setDateToButton(it.first, it.second)
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun setDateToButton(first: Long?, second: Long?) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(getString(R.string.period))
        stringBuilder.append(": ")
        stringBuilder.append(sdf.format(first))
        stringBuilder.append(" - ")
        stringBuilder.append(sdf.format(second))
        binding.period.text = stringBuilder
    }

    private fun setSelectionForPeriod(builder: MaterialDatePicker.Builder<Pair<Long, Long>>) {
        val first = readFromPrefs()
        val second = readFromPrefs(false)
        val period = Pair(first, second)
        builder.setSelection(period)
    }

    private fun writeToPrefs(first: Long?, second: Long?) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        prefs.edit()
            .putLong(PREF_PLANNED_PERIOD_FROM, first ?: 0L)
            .putLong(PREF_PLANNED_PERIOD_TO, second ?: 0L)
            .apply()
    }

    private fun readFromPrefs(first: Boolean = true): Long {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = mainVM.currentDate.value
        calendar.add(Calendar.DAY_OF_MONTH, 30)
        val nextMonth = calendar.timeInMillis
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return if (first)
            prefs.getLong(PREF_PLANNED_PERIOD_FROM, mainVM.currentDate.value?.time ?: Date().time)
        else {
            prefs.getLong(PREF_PLANNED_PERIOD_TO, nextMonth)
        }
    }

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }

}