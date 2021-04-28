package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.prolificinteractive.materialcalendarview.CalendarDay
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.databinding.FragmentPlanCompletedRefactorBinding
import digital.fact.saver.domain.models.PlanStatus
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.*
import digital.fact.saver.utils.calandarView.CurrentDayDecoratorInRange
import digital.fact.saver.utils.calandarView.CurrentDayDecoratorOutsideRange
import digital.fact.saver.utils.calandarView.CurrentDecoratorEnd
import digital.fact.saver.utils.calandarView.CurrentDecoratorStart
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import java.util.*

class RefactorPlanFragment : Fragment() {

    private lateinit var binding: FragmentPlanCompletedRefactorBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    private lateinit var operationsVM: OperationsViewModel
    private var id: Long? = null
    private var dbPlan: DbPlan? = null
    private var planStatus: PlanStatus? = null

    @SuppressLint("SimpleDateFormat")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlanCompletedRefactorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(PlansViewModel::class.java)
        operationsVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
            .get(OperationsViewModel::class.java)
        id = arguments?.getLong("planId")
        navC = findNavController()
        setListeners()
        setObservers(this)
        binding.editTextSum.filters = arrayOf(SumInputFilter())
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.period.observe(owner, { period ->
            setDecorators(requireContext(), period.dateFrom, period.dateTo)
        })

        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                this.id?.let { id ->
                    for (plan in plans) {
                        if (plan.id == id) {
                            this.dbPlan = plan
                            val unixFrom = period.dateFrom.time.time
                            val unixTo = period.dateTo.time.time
                            val planStatus = checkPlanStatus(plan, unixFrom, unixTo)
                            this.planStatus = planStatus
                            binding.toolbar.menu.clear()
                            planStatus?.let { status ->
                                showCalendar(status)
                                setMenu(status)
                                if (status == PlanStatus.CURRENT || status == PlanStatus.OUTSIDE) {
                                    val date = Date(plan.planning_date)
                                    val localDate: LocalDate =
                                        Instant.ofEpochMilli(date.time)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                    binding.calendar.selectedDate = CalendarDay.from(localDate)
                                }
                            }
                            binding.editTextDescription.setText(plan.name)
                            binding.editTextSum.setText(sumToString(plan.sum))

                            if (plan.operation_id != 0L &&
                                plan.planning_date in unixFrom..unixTo
                            ) {
                                binding.imageViewMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.ic_check_mark_pink
                                    )
                                )
                                binding.textViewInRange.text =
                                    resources.getString(R.string.plan_completed_in_period)
                            } else {
                                binding.imageViewMark.setImageDrawable(
                                    ContextCompat.getDrawable(
                                        requireContext(),
                                        R.drawable.ic_check_mark_purple
                                    )
                                )
                                binding.textViewInRange.text =
                                    resources.getString(R.string.plan_completed_outside_period)
                            }
                            binding.textViewSumLogo.text =
                                if (plan.type == DbPlan.PlanType.EXPENSES.value)
                                    resources.getString(R.string.plan_spend)
                                else resources.getString(R.string.plan_income)
                            binding.textViewFactLogo.text =
                                if (plan.type == DbPlan.PlanType.EXPENSES.value)
                                    resources.getString(R.string.fact_spent)
                                else resources.getString(R.string.fact_income)
                            binding.toolbar.subtitle =
                                if (plan.type == DbPlan.PlanType.EXPENSES.value)
                                    resources.getString(R.string.spend)
                                else resources.getString(R.string.income)
                            operationsVM.getAllOperations().value?.let { operations ->
                                operations.firstOrNull { it.plan_id == plan.operation_id }
                                    ?.let { operation ->
                                        binding.textViewFactSum.text = sumToString(operation.sum)
                                    }
                            }
                            return@observe
                        }
                    }
                }
            }
        })
    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {

                R.id.delete_plan -> {
                    dbPlan?.let { currentPlan ->
                        SlideToPerformDialog(title = getString(R.string.will_do_delete),
                            message = getString(R.string.you_delete_plan_from_list),
                            onSliderFinishedListener = {
                                plansVM.deletePlan(currentPlan).observe(viewLifecycleOwner, {
                                    navC.popBackStack()
                                })
                            }
                        ).show(childFragmentManager, "confirm-delete-dialog")
                    }
                }

                R.id.plan_refactor_done_in_range_delete -> {
                    dbPlan?.let { currentPlan ->
                        SlideToPerformDialog(title = getString(R.string.will_do_delete),
                            message = getString(R.string.you_delete_plan_from_list),
                            onSliderFinishedListener = {
                                plansVM.deletePlan(currentPlan).observe(viewLifecycleOwner, {
                                    navC.popBackStack()
                                })
                            }
                        ).show(childFragmentManager, "confirm-delete-dialog")
                    }
                }
                R.id.plan_refactor_done_in_range_reset -> {
                    dbPlan?.let { currentPlan ->
                        SlideToPerformDialog(title = getString(R.string.will_do_reset),
                            message = getString(R.string.you_will_reset_plan),
                            onSliderFinishedListener = {
                                val updatePlan = DbPlan(
                                    id = currentPlan.id,
                                    type = currentPlan.type,
                                    sum = currentPlan.sum,
                                    name = currentPlan.name,
                                    operation_id = 0,
                                    planning_date = currentPlan.planning_date
                                )
                                plansVM.updatePlan(updatePlan).observe(viewLifecycleOwner, {
                                    navC.popBackStack()
                                })
                            }
                        ).show(childFragmentManager, "confirm-delete-dialog")

                    }
                }
                R.id.plan_refactor_done_in_range_show_history -> {
                }
                R.id.plan_refactor_done_outside_range_show_in_history -> {

                }
            }
            false
        }
        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.buttonAddPlan.isEnabled = checkFieldsValid()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.editTextSum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.buttonAddPlan.isEnabled = checkFieldsValid()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        binding.buttonAddPlan.setOnClickListener {
            this.dbPlan?.let { dbPlan ->
                var date = 0L
                planStatus?.let { status ->
                    date = if (status == PlanStatus.CURRENT || status == PlanStatus.OUTSIDE) {
                        if (binding.checkBoxWithoutDate.isChecked)
                            0L
                        else
                            DateTimeUtils.toSqlDate(binding.calendar.selectedDate?.date).time
                    } else dbPlan.planning_date
                }
                val sum: Long =
                    (round(binding.editTextSum.text.toString().toDouble(), 2) * 100).toLong()
                val plan = DbPlan(
                    id = dbPlan.id,
                    type = dbPlan.type,
                    name = binding.editTextDescription.text.toString(),
                    operation_id = dbPlan.operation_id,
                    planning_date = date,
                    sum = sum
                )
                plansVM.updatePlan(plan).observe(viewLifecycleOwner, {
                    plansVM.updatePlans()
                    navC.popBackStack()
                })
            }

        }
        binding.toolbar.setNavigationOnClickListener {
            navC.popBackStack()
        }

        binding.checkBoxWithoutDate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) binding.constraintCalendar.visibility = View.GONE
            else binding.constraintCalendar.visibility = View.VISIBLE
        }
    }


    private fun checkFieldsValid(): Boolean {
        return binding.editTextDescription.text!!.isNotBlank()
                && binding.editTextSum.text.isNotBlank() && binding.editTextSum.text.toString()
            .toDouble() != 0.0
    }

    private fun showCalendar(planStatus: PlanStatus) {
        when (planStatus) {
            PlanStatus.CURRENT -> {
                binding.constraintDate.visibility = View.VISIBLE
                binding.constraintFactSum.visibility = View.GONE
            }
            PlanStatus.OUTSIDE -> {
                binding.constraintDate.visibility = View.VISIBLE
                binding.constraintFactSum.visibility = View.GONE
            }
            else -> {
                binding.constraintDate.visibility = View.GONE
                binding.constraintFactSum.visibility = View.VISIBLE
            }
        }
    }

    private fun setMenu(planStatus: PlanStatus) {
        when (planStatus) {
            PlanStatus.CURRENT ->
                binding.toolbar.inflateMenu(R.menu.menu_plan_delete)

            PlanStatus.DONE ->
                binding.toolbar.inflateMenu(R.menu.menu_plan_done_in_period)

            PlanStatus.DONE_OUTSIDE ->
                binding.toolbar.inflateMenu(R.menu.menu_plan_done_outside)
            PlanStatus.OUTSIDE ->
                binding.toolbar.inflateMenu(R.menu.menu_plan_outside)

        }
    }

    private fun setDecorators(context: Context, start: Calendar, end: Calendar) {
        val decoratorOutsideRange = CurrentDayDecoratorOutsideRange(
            ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_outside_range
            )
        )

        val decoratorStart = CurrentDecoratorStart(
            start, ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_start
            )
        )
        val decoratorEnd = CurrentDecoratorEnd(
            end, ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_end
            )
        )
        val decoratorInRange = CurrentDayDecoratorInRange(
            start, end, ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_in_range
            )
        )
        binding.calendar.addDecorator(decoratorOutsideRange)
        binding.calendar.addDecorator(decoratorInRange)
        binding.calendar.addDecorator(decoratorStart)
        binding.calendar.addDecorator(decoratorEnd)
    }
}