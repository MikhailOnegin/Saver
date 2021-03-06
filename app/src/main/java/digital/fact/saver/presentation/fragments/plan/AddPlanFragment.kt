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
import digital.fact.saver.databinding.FragmentAddPlanBinding
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

class AddPlanFragment : Fragment() {

    private lateinit var binding: FragmentAddPlanBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController

    @SuppressLint("SimpleDateFormat")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(PlansViewModel::class.java)
        navC = findNavController()
        setListeners()
        setObservers(this)
        setDateOfCalendar()
        binding.editTextSum.filters = arrayOf(SumInputFilter())
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.period.observe(owner, { period ->
            setDecorators(requireContext(), period.dateFrom, period.dateTo)
        })
    }

    private fun setListeners() {

        binding.buttonTest.setOnClickListener {
            navC.navigate(R.id.action_add_fragment_to_balanceFragment)
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
        binding.radioButtonIncome.setOnClickListener {
            binding.radioButtonSpending.isChecked = !binding.radioButtonIncome.isChecked
        }
        binding.radioButtonSpending.setOnClickListener {
            binding.radioButtonIncome.isChecked = !binding.radioButtonSpending.isChecked
        }

        binding.buttonAddPlan.setOnClickListener {
            val type = if (binding.radioButtonSpending.isChecked) DbPlan.PlanType.EXPENSES.value
            else DbPlan.PlanType.INCOME.value
            val date = if(binding.constraintCalendar.visibility == View.VISIBLE)
                DateTimeUtils.toSqlDate(binding.calendar.selectedDate?.date).time
            else 0
            val sum: Long = (round(binding.editTextSum.text.toString().toDouble(), 2) * 100).toLong()
                val plan = DbPlan(
                    type = type,
                    name = binding.editTextDescription.text.toString(),
                    operation_id = 0,
                    planning_date = date,
                    sum = sum
                )
                plansVM.insertPlan(plan).observe(viewLifecycleOwner, {
                    plansVM.updatePlans()
                    navC.popBackStack()
                })


        }
        binding.checkBoxWithoutDate.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) binding.constraintCalendar.visibility = View.GONE
            else binding.constraintCalendar.visibility = View.VISIBLE
        }

        binding.toolbar.setNavigationOnClickListener {
            navC.popBackStack()
        }
    }


    private fun checkFieldsValid(): Boolean {
        return binding.editTextDescription.text!!.isNotBlank()
                && binding.editTextSum.text.isNotBlank() && binding.editTextSum.text.toString().toDouble() != 0.0
    }

    private fun setDecorators(context: Context, start: Calendar, end: Calendar){
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

    private fun setDateOfCalendar(){
        val date = Date()
        val localDate: LocalDate =
            Instant.ofEpochMilli(date.time).atZone(ZoneId.systemDefault()).toLocalDate()
        binding.calendar.selectedDate = CalendarDay.from(localDate)
    }
}