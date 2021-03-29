package digital.fact.saver.presentation.fragments.period

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPeriodBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.toActiveSources
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PeriodViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.*
import java.text.SimpleDateFormat
import java.util.*

class PeriodFragment : Fragment() {

    private lateinit var binding: FragmentPeriodBinding
    private lateinit var periodVM: PeriodViewModel
    private lateinit var sourceVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var detailDialog: BottomSheetDialog
    private var activeSummary: Long = 0L
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val builder = MaterialDatePicker.Builder.dateRangePicker()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeriodBinding.inflate(inflater, container, false)
        detailDialog = BottomSheetDialog(requireActivity())
        detailDialog.dismissWithAnimation = true
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        periodVM = ViewModelProvider(requireActivity())[PeriodViewModel::class.java]
        periodVM = ViewModelProvider(requireActivity())[PeriodViewModel::class.java]
        sourceVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        periodVM.incomes.observe(viewLifecycleOwner, { onIncomesChanged(it) })
        periodVM.expenses.observe(viewLifecycleOwner, { onExpensesChanged(it) })
        periodVM.period.observe(viewLifecycleOwner, { onPeriodChanged(it) })
        sourceVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(sources: List<Source>?) {
        sources?.let {
            activeSummary = periodVM.getOperationsResultByDate(
                sources = it.toActiveSources(
                    operations = null
                ), period = periodVM.period.value ?: Pair(Date().time, Date().time)
            )
            binding.summary.text = activeSummary.toStringFormatter()
        }
        val daysCount = periodVM.calculateDaysCount(
            period = periodVM.period.value ?: Pair(
                Date().time,
                Date().time
            )
        )
        binding.hint.text = when (getWordEndingType(daysCount)) {
            WordEnding.TYPE_1 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
                R.string.day_type1
            )
            WordEnding.TYPE_2 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
                R.string.day_type2
            )
            WordEnding.TYPE_3 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
                R.string.day_type3
            )
        }
        binding.periodDayBalance.text = (activeSummary.div(daysCount).toStringFormatter())
    }

    private fun onPeriodChanged(period: Pair<Long, Long>?) {
        period?.let { setDateToButton(it) }
        periodVM.getPeriodPlansValues(period ?: Pair(Date().time, Date().time))
    }

    private fun onIncomesChanged(it: Long?) {
        binding.balanceIncome.text = it?.toStringFormatter()
    }

    private fun onExpensesChanged(it: Long?) {
        binding.balanceExpenses.text = it?.toStringFormatter()
    }

    private fun setListeners() {
        binding.period.setOnClickListener { showDatePicker() }
        binding.instructions.setOnClickListener { showCalculateInstructions() }
    }

    private fun showCalculateInstructions() {

    }

    private fun showDatePicker() {
        builder.setTheme(R.style.Calendar)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            if (it.first == it.second) {
                createSnackBar(
                    binding.root,
                    getString(R.string.errorPeriodDays)
                ).show()
            } else {
                periodVM.writeToPrefs(it.first, it.second)
                val period =
                    Pair(first = it.first ?: Date().time, second = it.second ?: Date().time)
                setDateToButton(period)
                sourceVM.updateSources()
            }
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun setDateToButton(period: Pair<Long, Long>) {
        val stringBuilder = StringBuilder()
        stringBuilder.append(getString(R.string.period))
        stringBuilder.append(": ")
        stringBuilder.append(sdf.format(period.first))
        stringBuilder.append(" - ")
        stringBuilder.append(sdf.format(period.second))
        binding.period.text = stringBuilder
    }

}