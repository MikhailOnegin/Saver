package digital.fact.saver.presentation.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.DialogRefactorPlanBinding
import digital.fact.saver.data.database.dto.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import digital.fact.saver.utils.toDate
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat
import java.util.*

class RefactorPlanDialog(private val _id: Long): BottomSheetDialogFragment(){

    private lateinit var binding: DialogRefactorPlanBinding
    private val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату").setTheme(R.style.Calendar).build()
    private lateinit var plansVM: PlansViewModel
    private var plan: Plan? = null
    private var selectedDateUnix: Long = 0
    @SuppressLint("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRefactorPlanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val behavior = (dialog as BottomSheetDialog).behavior
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(
            PlansViewModel::class.java)
        setListeners()
        setObservers(this)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, {plans ->
            for (i in plans.indices){
                val plan = plans[i]
                if(_id == plan.id){
                    this.plan = plan
                    if(plan.type == Plan.PlanType.INCOME.value){
                        binding.radioButtonSpending.isChecked = false
                        binding.radioButtonIncome.isChecked = true
                    }
                    else {
                        binding.radioButtonSpending.isChecked = true
                        binding.radioButtonIncome.isChecked = false
                    }
                    binding.editTextDescription.setText(plan.name)
                    binding.editTextSum.setText((plan.sum.toDouble()/100).toString())
                    binding.textViewDate.text = plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
                    return@observe
                }
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        val currentDate =  Calendar.getInstance().time
        selectedDateUnix = currentDate.time
        val currentDateText = dateFormatter.format(currentDate)
        binding.textViewDate.text = currentDateText
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        binding.textViewDate.setOnClickListener {
            datePicker.show( childFragmentManager,
                "addPlan"
            )
        }

        datePicker.addOnPositiveButtonClickListener {
            val date  = it.toDate()
            selectedDateUnix = it
            binding.textViewDate.text = date
        }

        binding.buttonUpdatePlan.setOnClickListener {
            val type = when(binding.radioButtonSpending.isChecked){
                true -> Plan.PlanType.SPENDING.value
                else -> Plan.PlanType.INCOME.value
            }
            var newPlan: Plan? = null
            val sumText = binding.editTextSum.text.toString().toDouble()
            val sumRound = round(sumText, 2)
            val sumResult = (sumRound * 100).toLong()
            this.plan?.let {
                newPlan = Plan(it.id, type, sumResult, binding.editTextDescription.text.toString(), it.operation_id, selectedDateUnix)
            }
            newPlan?.let {
                plansVM.updatePlan(it).observe(viewLifecycleOwner, {
                    plansVM.updatePlans()
                    this.dismiss()
                })
            }

        }
    }
}