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
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.toDate
import digital.fact.saver.utils.toDateString
import digital.fact.saver.utils.toUnixLong
import java.text.SimpleDateFormat
import java.util.*

class RefactorPlanDialog(private val _id: Int): BottomSheetDialogFragment(){

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
                if(_id == plan._id){
                    this.plan = plan
                    binding.editTextDescription.setText(plan.name)
                    binding.editTextSum.setText(plan.sum.toString())
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
            val category = if(binding.radioButtonConsumption.isChecked) {
                Plan.PlanCategory.SPENDING}
            else Plan.PlanCategory.INCOME
            val dateUnix = binding.textViewDate.text.toString().toUnixLong(dateFormatter)
            val operationId = when(binding.radioButtonDone.isChecked){
                true -> 1
                else -> 0
            }
            var newPlan: Plan? = null
            this.plan?.let {
                newPlan = Plan(it._id, category.value, binding.editTextSum.text.toString().toFloat(), binding.editTextDescription.text.toString(), operationId, 0, selectedDateUnix)
            }
            newPlan?.let {
                plansVM.updatePlan(it)
                plansVM.updatePlans()
            }
            this.dismiss()
        }
        binding.buttonDeletePlan.setOnClickListener {
            plan?.let {
                plansVM.deletePlan(it)
                plansVM.updatePlans()
                this.dismiss()
            }
        }
    }
}