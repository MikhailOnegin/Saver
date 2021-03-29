package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.prolificinteractive.materialcalendarview.CalendarDay
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentAddPlanBinding
import digital.fact.saver.data.database.dto.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.CurrentDayDecorator
import digital.fact.saver.utils.round
import sun.bob.mcalendarview.MarkStyle
import sun.bob.mcalendarview.vo.DateData
import java.text.SimpleDateFormat

class AddPlanFragment : Fragment() {

    private lateinit var binding: FragmentAddPlanBinding
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    @SuppressLint("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy")

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
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.choose_date)).setTheme(R.style.Calendar)
            .build()
        setListeners()
        setObservers()
        val mydate= CalendarDay.from(2021,  3, 27) // year, month, date
        binding.calendar.addDecorators(CurrentDayDecorator(activity, mydate))
        val mydate2= CalendarDay.from(2021,  3, 28) // year, month, date
        val decorator = CurrentDayDecorator(activity, mydate2)
        binding.calendar.addDecorators(CurrentDayDecorator(activity, mydate2))
        //val hashMapProperty = hashMapOf<Any, Property>()
        //// Initialize default resource
        //val property = Property()
        //// Initialize default resource
        //property.layoutResource = R.layout.layout_calendar_default_property
        //// Initialize ans assign variable
        //property.dateTextViewResource = R.id.text_view
        //// Put object and property
        //hashMapProperty["default"] = property
        //// For current date
        //val currentProperty = Property()
        //currentProperty.layoutResource - R.layout.current_view
        //currentProperty.dateTextViewResource = R.id.text_view
        //hashMapProperty["current"] = currentProperty
        //// For present date
        //val presentProperty = Property()
        //presentProperty.layoutResource = R.layout.present_view
        //presentProperty.dateTextViewResource = R.id.text_view
        //hashMapProperty["present"] = presentProperty
        //// For absent
        //val absentProperty = Property()
        //absentProperty.layoutResource = R.layout.absent_view
        //absentProperty.dateTextViewResource = R.id.text_view
        //hashMapProperty["absent"] = absentProperty
        //// Set deck hash map on custom calendar
        //binding.calendarViewCustom.setMapDescToProp(hashMapProperty)
        //// Initialize date hash map
        //val dateHashMap = hashMapOf<Int, Any>()
        //// Initialize calendar
        //val calendar = Calendar.getInstance()
        //// Put values
        //dateHashMap[calendar.get((Calendar.DAY_OF_MONTH))] = "current"
        //dateHashMap[1] = "present"
        //dateHashMap[2] = "absent"
        //dateHashMap[3] = "present"
        //dateHashMap[4] = "absent"
        //dateHashMap[20] = "present"
        //dateHashMap[30] = "absent"
        //// Set date
        //binding.calendarViewCustom.setDate(calendar, dateHashMap)
        //binding.calendarViewCustom.setOnDateSelectedListener { view, selectedDate, desc ->
        //    val sDate = "${selectedDate.get(Calendar.DAY_OF_MONTH)}" +
        //            "/${selectedDate.get(Calendar.MONTH) + 1}" +
        //            "/${selectedDate.get(Calendar.YEAR)}"
        //    Toast.makeText(requireContext(), sDate, Toast.LENGTH_SHORT).show()
        //}
    }

    private fun setObservers() {
    }



    private fun setListeners() {
        binding.buttonAddPlan.setOnClickListener {
            if (checkoutValidFields()) {
                val category = if (binding.radioButtonSpending.isChecked) {
                    Plan.PlanType.SPENDING
                } else Plan.PlanType.INCOME
                val sumText = binding.editTextSum.text.toString().toDouble()
                val sumRound = round(sumText, 2)
                val sumResult = (sumRound * 100).toLong()
                val name = binding.editTextDescription.text.toString()
                val plan = Plan(
                        type = category.value,
                        sum = sumResult,
                        name = name,
                        operation_id = 0,
                        planning_date = 1
                )
                it.isEnabled = false
                plansVM.insertPlan(plan).observe(viewLifecycleOwner, {
                    binding.buttonAddPlan.isSelected = true
                    navC.navigate(R.id.action_add_fragment_to_plansFragment)
                })
            }
        }
        binding.editTextDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkoutValidFields()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        binding.editTextSum.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkoutValidFields()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    private fun checkoutValidFields(): Boolean {
        if (binding.editTextDescription.text.isBlank()) {
            binding.textViewIncorrectDescription.visibility = View.VISIBLE
        } else {
            binding.textViewIncorrectDescription.visibility = View.GONE
        }

        if (binding.editTextSum.text.isBlank()) {
            binding.textViewIncorrectSum.visibility = View.VISIBLE
        } else {
            binding.textViewIncorrectSum.visibility = View.GONE

        }
        binding.buttonAddPlan.isEnabled = binding.editTextDescription.text.isNotBlank() && binding.editTextSum.text.isNotBlank()
        return !(binding.editTextDescription.text.isEmpty() || binding.editTextSum.text.isEmpty())
    }
}