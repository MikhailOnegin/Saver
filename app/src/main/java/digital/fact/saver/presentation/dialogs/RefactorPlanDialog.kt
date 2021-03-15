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
import digital.fact.saver.databinding.DialogAddPlanBinding
import digital.fact.saver.databinding.DialogRefactorPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.toDate
import digital.fact.saver.utils.toDateString
import digital.fact.saver.utils.toUnixInt
import java.text.SimpleDateFormat
import java.util.*

class RefactorPlanDialog(private val _id: Int): BottomSheetDialogFragment(){
    private lateinit var binding: DialogRefactorPlanBinding
    private val datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Выберите дату").build()
    private lateinit var plansVM: PlansViewModel
    private var plan: Plan? = null
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
            val f = 5555
            val ff = "ads"
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
            binding.textViewDate.text = date
        }

        binding.buttonUpdatePlan.setOnClickListener {
            val category = if(binding.radioButtonConsumption.isChecked) {
                Plan.PlanCategory.CONSUMPTION}
            else Plan.PlanCategory.ADMISSION
            val dateUnix = binding.textViewDate.text.toString().toUnixInt(dateFormatter)
            val plan = Plan(category, binding.editTextSum.text.toString().toInt(), binding.editTextDescription.text.toString(),  0 , 0, dateUnix )
            plansVM.updatePlan(plan)
            plansVM.updatePlans()
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