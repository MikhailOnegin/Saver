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
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.toActiveSources
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PeriodViewModel
import digital.fact.saver.presentation.viewmodels.PeriodViewModel.Companion.PLANNED_EXPENSES_COUNT
import digital.fact.saver.presentation.viewmodels.PeriodViewModel.Companion.PLANNED_EXPENSES_FINISHED_COUNT
import digital.fact.saver.presentation.viewmodels.PeriodViewModel.Companion.PLANNED_INCOMES_COUNT
import digital.fact.saver.presentation.viewmodels.PeriodViewModel.Companion.PLANNED_INCOMES_FINISHED_COUNT
import digital.fact.saver.presentation.viewmodels.PeriodViewModel.Companion.WALLETS_COUNT
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.*
import eightbitlab.com.blurview.RenderScriptBlur
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
        setupBlurView()
        detailDialog = BottomSheetDialog(requireActivity())
        detailDialog.dismissWithAnimation = true
        return binding.root
    }

    private fun setupBlurView() {
        val radius = 10f
        binding.blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(requireActivity()))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourceVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        periodVM = ViewModelProvider(requireActivity())[PeriodViewModel::class.java]
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        periodVM.incomes.observe(viewLifecycleOwner, { onIncomesChanged(it) })
        periodVM.expenses.observe(viewLifecycleOwner, { onExpensesChanged(it) })
        periodVM.operations.observe(viewLifecycleOwner, { onOperationsChanged(it) })
        periodVM.plans.observe(viewLifecycleOwner, { onPlansChanged(it) })
        periodVM.period.observe(viewLifecycleOwner, { onPeriodChanged(it) })
        sourceVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onPlansChanged(list: List<Plan>?) {
        list?.let {
            val result = periodVM.getPlansResult(list)
            binding.balanceIncome.text =
                result.first { it.first == PLANNED_INCOMES_COUNT }.second.formatToMoney()
            binding.balanceExpenses.text =
                result.first { it.first == PLANNED_EXPENSES_COUNT }.second.formatToMoney()
            setProgress(result)
        }
    }

    private fun setProgress(list: List<Pair<String, Long>>) {
        val plannedIncomes = list.first { it.first == PLANNED_INCOMES_COUNT }.second
        val currentIncomes = list.first { it.first == PLANNED_INCOMES_FINISHED_COUNT }.second
        val plannedExpenses = list.first { it.first == PLANNED_EXPENSES_COUNT }.second
        val currentExpenses = list.first { it.first == PLANNED_EXPENSES_FINISHED_COUNT }.second
        binding.progressIncome.progress =
            ((currentIncomes.toFloat() / plannedIncomes) * 100).toInt()
        binding.progressExpenses.progress =
            ((currentExpenses.toFloat() / plannedExpenses) * 100).toInt()
    }

    private fun onOperationsChanged(list: List<Operation>?) {
        list?.let {
            val result = periodVM.getOperationsResult(
                sources = sourceVM.sources.value?.toActiveSources(
                    operations = null,
                    needOther = false
                ), operations = list
            )
            activeSummary = result.first { it.first == WALLETS_COUNT }.second
            binding.summary.text = activeSummary.formatToMoney()
        }
    }

    private fun onSourcesChanged(sources: List<Source>?) {
        sources?.let {
            periodVM.getOperationsByDate(
                sources = it.toActiveSources(
                    operations = null,
                    needOther = false
                ), period = periodVM.period.value ?: Pair(Date().time, Date().time)
            )
        }
        val daysCount = periodVM.calculateDaysCount(
            period = periodVM.period.value ?: Pair(
                Date().time,
                Date().time
            )
        )
        binding.hint.text = when (getWordEndingType(daysCount.toLong())) {
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
        binding.periodDayBalance.text =
            ((if (daysCount == 0) activeSummary else activeSummary.div(daysCount)).formatToMoney())
    }

    private fun onPeriodChanged(period: Pair<Long, Long>?) {
        period?.let {
            setDateToButton(it)
            periodVM.getPlansByDate(period)
        }
        periodVM.getPeriodPlansValues(period ?: Pair(Date().time, Date().time))
    }

    private fun onIncomesChanged(it: Long?) {
        binding.balanceIncome.text = it?.formatToMoney()
    }

    private fun onExpensesChanged(it: Long?) {
        binding.balanceExpenses.text = it?.formatToMoney()
    }

    private fun setListeners() {
        binding.period.setOnClickListener { showDatePicker() }
        binding.instructions.setOnClickListener { showCalculateInstructions() }
        binding.toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).openDrawer() }
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
                periodVM.readPrefsFromDisk()
                sourceVM.getAllSources()
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