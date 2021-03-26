package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
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
import kotlin.time.milliseconds

class AddPlanFragment : Fragment() {

    private lateinit var binding: FragmentAddPlanBinding
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
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
        navC = findNavController()
        datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText(resources.getString(R.string.choose_date)).setTheme(R.style.Calendar)
                .build()
        setListeners()
        setObservers()
    }

    private fun setObservers() {
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
                        planning_date = selectedDateUnix
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
            binding.editTextDescription.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_custom_edit_text_2_rounded)
        } else {
            binding.textViewIncorrectDescription.visibility = View.GONE
            binding.editTextDescription.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_custom_edit_text_rounded)
        }

        if (binding.editTextSum.text.isBlank()) {
            binding.textViewIncorrectSum.visibility = View.VISIBLE
            binding.editTextSum.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_custom_edit_text_2_rounded)
        } else {
            binding.textViewIncorrectSum.visibility = View.GONE
            binding.editTextSum.background = ContextCompat.getDrawable(requireContext(), R.drawable.background_custom_edit_text_rounded)
        }
        binding.buttonAddPlan.isEnabled = binding.editTextDescription.text.isNotBlank() && binding.editTextSum.text.isNotBlank() && binding.textViewDate.text.isNotBlank()
        return !(binding.editTextDescription.text.isEmpty() || binding.editTextSum.text.isEmpty() || binding.textViewDate.text.isEmpty())
    }
}