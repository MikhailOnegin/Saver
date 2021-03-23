package digital.fact.saver.presentation.fragments.period

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPeriodBinding
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.Source
import digital.fact.saver.models.*
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PeriodViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.*
import java.text.SimpleDateFormat
import java.util.*

class PeriodFragment : Fragment() {

    private lateinit var binding: FragmentPeriodBinding
    private lateinit var periodVM: PeriodViewModel
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
        setObservers()
    }

    private fun setObservers() {
        periodVM.incomes.observe(viewLifecycleOwner, { onIncomesChanged(it) })
        periodVM.expenses.observe(viewLifecycleOwner, { onExpensesChanged(it) })
    }

    private fun onIncomesChanged(it: Long?) {
        binding.balanceIncome.text = it?.toStringFormatter()
    }

    private fun onExpensesChanged(it: Long?) {
        binding.balanceExpenses.text = it?.toStringFormatter()
    }

//    private fun onSourcesChanged(sources: List<SourceItem>?) {
//        var saversCount = 0L
//        var walletsCount = 0L
//
//        sources?.forEach { item ->
//            when (item.itemType) {
//                Source.SourceCategory.WALLET_ACTIVE.value -> {
//                    operationsVM.getByDate(
//                        itemId = item.itemId,
//                        date = readFromPrefs()
//                    ).value?.toOperations()?.forEach { operation ->
//                        when (operation.type) {
//                            Operation.OperationType.EXPENSES.value, Operation.OperationType.PLANNED_EXPENSES.value -> walletsCount -= operation.sum
//                            Operation.OperationType.INCOME.value, Operation.OperationType.PLANNED_INCOME.value -> walletsCount += operation.sum
//                            Operation.OperationType.TRANSFER.value ->
//                                if (operation.fromSourceId == (item as Sources).id) {
//                                    walletsCount -= operation.sum
//                                } else if (operation.toSourceId == item.id) {
//                                    walletsCount += operation.sum
//                                }
//                        }
//                    }
//                }
//                Source.SourceCategory.SAVER.value -> {
//                    saversCount += (item as Sources).currentSum
//                }
//            }
//        }
//        if (walletsCount == 0L) walletsCount =
//            (sources?.firstOrNull { it is SourcesActiveCount } as SourcesActiveCount).activeWalletsSum
//        val activeSummary = walletsCount - saversCount
//        binding.summary.text = activeSummary.toStringFormatter()
//        val daysCount = calculateDaysCount()
//        binding.hint.text = when (getWordEndingType(daysCount)) {
//            WordEnding.TYPE_1 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
//                R.string.day_type1
//            )
//            WordEnding.TYPE_2 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
//                R.string.day_type2
//            )
//            WordEnding.TYPE_3 -> getString(R.string.periodDayHint) + ' ' + daysCount + ' ' + getString(
//                R.string.day_type3
//            )
//        }
//        binding.periodDayBalance.text = (activeSummary.div(daysCount).toStringFormatter())
//    }
//
//    private fun calculateDaysCount(): Int {
//        val inMillis = readFromPrefs(false) - readFromPrefs()
//        return inMillis.div(3600000L * 24).toInt()
//    }
//
//    private fun setData() {
//        setDateToButton(readFromPrefs(), readFromPrefs(false))
//    }
//
//    private fun onPlansChanged(list: List<Plan>) {
//        val incomes = list.filter { it.type == Plan.PlanType.INCOME.value }
//        val expenses = list.filter { it.type == Plan.PlanType.SPENDING.value }
//        var incomesSum = 0L
//        var expensesSum = 0L
//
//        incomes.forEach { incomesSum += it.sum }
//        expenses.forEach { expensesSum += it.sum }
//
//        binding.balanceIncome.text = incomesSum.toStringFormatter()
//        binding.balanceExpenses.text = expensesSum.toStringFormatter()
//    }
//
//    private fun setListeners() {
//        binding.period.setOnClickListener { showDatePicker() }
//    }
//
//    private fun showDatePicker() {
//        builder.setTheme(R.style.Calendar)
//        val picker = builder.build()
//        picker.addOnPositiveButtonClickListener {
//            if (it.first == it.second) {
//                createSnackBar(
//                    binding.root,
//                    getString(R.string.errorPeriodDays)
//                ).show()
//            } else {
//                writeToPrefs(it.first, it.second)
//                setDateToButton(it.first, it.second)
//                sourcesVM.updateSources()
//            }
//        }
//        picker.show(childFragmentManager, "date_picker")
//    }
//
//    private fun setDateToButton(first: Long?, second: Long?) {
//        val stringBuilder = StringBuilder()
//        stringBuilder.append(getString(R.string.period))
//        stringBuilder.append(": ")
//        stringBuilder.append(sdf.format(first))
//        stringBuilder.append(" - ")
//        stringBuilder.append(sdf.format(second))
//        binding.period.text = stringBuilder
//    }

    companion object {
        const val PREF_PLANNED_PERIOD_FROM = "pref_planned_period_from"
        const val PREF_PLANNED_PERIOD_TO = "pref_planned_period_to"
    }

}