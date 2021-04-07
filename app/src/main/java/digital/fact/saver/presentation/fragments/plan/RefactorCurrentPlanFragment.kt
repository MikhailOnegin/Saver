package digital.fact.saver.presentation.fragments.plan

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
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.prolificinteractive.materialcalendarview.CalendarDay
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.databinding.FragmentRefactorCurrentPlanBinding
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


class RefactorCurrentPlanFragment  : Fragment() {

    private lateinit var binding: FragmentRefactorCurrentPlanBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    private var id: Long? = null
    private var plan: PlanTable? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentRefactorCurrentPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(PlansViewModel::class.java)
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
            this.id?.let { id ->
                for (item in plans) {
                    if (item.id == id) {
                        this.plan = item
                        binding.textViewSumLogo.text =
                                if (item.type == PlanTable.PlanType.SPENDING.value)
                                    resources.getString(R.string.plan_spending_2)
                                else resources.getString(R.string.plan_income_2)
                        binding.toolbar.subtitle = if (item.type == PlanTable.PlanType.SPENDING.value)
                            resources.getString(R.string.spend)
                        else resources.getString(R.string.income)
                        val date = Date(item.planning_date)
                        val localDate: LocalDate =
                                Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                        binding.calendar.selectedDate = CalendarDay.from(localDate)
                        binding.editTextDescription.setText(item.name)
                        binding.editTextSum.setText(item.sum.toString())
                        return@observe
                    }
                }
            }
        })
    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.delete_plan -> {
                    plan?.let { currentPlan ->
                        plansVM.deletePlan(currentPlan).observe(viewLifecycleOwner, {
                            navC.popBackStack()
                        })
                    }
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

        binding.buttonAddPlan.setOnClickListener { _ ->
            this.plan?.let { planCurrent ->
                val date = DateTimeUtils.toSqlDate(binding.calendar.selectedDate?.date).time
                val sum: Long = (round(binding.editTextSum.text.toString().toDouble(), 2) * 100).toLong()
                if (checkFieldsValid()) {
                    val plan = PlanTable(
                            id = planCurrent.id,
                            type = planCurrent.type,
                            name = binding.editTextDescription.text.toString(),
                            operation_id = planCurrent.operation_id,
                            planning_date = date,
                            sum = sum
                    )
                    plansVM.updatePlan(plan).observe(viewLifecycleOwner, {
                        plansVM.updatePlans()
                        navC.popBackStack()
                    })

                } else {
                    createSnackBar(
                            anchorView = binding.root,
                            text = "Некорректные данные",
                            buttonText = "Ок"
                    )
                }
            }

        }
        binding.toolbar.setNavigationOnClickListener {
            navC.popBackStack()
        }
    }

    private fun checkFieldsValid(): Boolean {
        return binding.editTextDescription.text!!.isNotBlank()
                && binding.editTextSum.text.isNotBlank() && binding.editTextSum.text.toString().toDouble() != 0.0
    }

    private fun setDecorators(context: Context, start: Calendar, end: Calendar) {
        val decoratorOutsideRange = CurrentDayDecoratorOutsideRange(
                ContextCompat.getDrawable(
                        context,
                        R.drawable.selector_calendar_outside_range
                )
        )

        val decoratorStart = CurrentDecoratorStart(
                context, start, ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_start
        )
        )
        val decoratorEnd = CurrentDecoratorEnd(
                context, end, ContextCompat.getDrawable(
                context,
                R.drawable.selector_calendar_end
        )
        )
        val decoratorInRange = CurrentDayDecoratorInRange(
                context, start, end, ContextCompat.getDrawable(
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