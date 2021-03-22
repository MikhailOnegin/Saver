package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentAddPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import digital.fact.saver.utils.toDate
import java.text.SimpleDateFormat
import java.util.*

class AddPlanFragment : Fragment() {

    private lateinit var binding: FragmentAddPlanBinding
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var plansVM: PlansViewModel
    private var selectedDateUnix: Long = 0

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
        datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(resources.getString(R.string.choose_date)).setTheme(R.style.Calendar)
            .build()
        setListeners()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        val currentDate = Calendar.getInstance().time
        val currentDateText = dateFormatter.format(currentDate)
        selectedDateUnix = currentDate.time
        binding.textViewDate.text = currentDateText
    }

    private fun setListeners() {
        binding.textViewDate.setOnClickListener {
            datePicker.show(
                childFragmentManager,
                "addPlan"
            )
        }

        datePicker.addOnPositiveButtonClickListener {
            val date = it.toDate()
            selectedDateUnix = it
            binding.textViewDate.text = date
        }

        binding.buttonAddPlan.setOnClickListener {
            val category = if (binding.radioButtonConsumption.isChecked) {
                Plan.PlanType.SPENDING
            } else Plan.PlanType.INCOME
            val sumText = binding.editTextSum.text.toString().toDouble()
            val sum = round(sumText, 2).toLong()
            val name = binding.editTextDescription.text.toString()
            val plan = Plan(
                type = category.value,
                sum = sum,
                name = name,
                operation_id = 0,
                planning_date = selectedDateUnix
            )
            plansVM.insertPlan(plan)
            plansVM.updatePlans()
            findNavController().popBackStack()
        }
    }
}