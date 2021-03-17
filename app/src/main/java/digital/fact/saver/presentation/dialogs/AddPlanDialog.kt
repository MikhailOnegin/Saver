package digital.fact.saver.presentation.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.DialogAddPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import digital.fact.saver.utils.toDate
import java.text.SimpleDateFormat
import java.util.*


class AddPlanDialog: BottomSheetDialogFragment(){

    private lateinit var binding: DialogAddPlanBinding
    private val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Выберите дату").setTheme(R.style.Calendar).build()
    private lateinit var plansVM: PlansViewModel
    private  var selectedDateUnix: Long = 0
    @SuppressLint("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("dd.MM.yyyy")

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = DialogAddPlanBinding.inflate(inflater, container, false)
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
        plansVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(PlansViewModel::class.java)
        setListeners()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onStart() {
        super.onStart()
        val currentDate =  Calendar.getInstance().time
        val currentDateText = dateFormatter.format(currentDate)
        selectedDateUnix = currentDate.time
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

        binding.buttonAddPlan.setOnClickListener {
            val category = if(binding.radioButtonConsumption.isChecked) {Plan.PlanType.SPENDING}
            else Plan.PlanType.INCOME
            val sum = round(binding.editTextSum.text.toString().toFloat(), 2).toFloat()
            val plan = Plan(category, sum, binding.editTextDescription.text.toString(), 0, selectedDateUnix )
            plansVM.insertPlan(plan)
            plansVM.updatePlans()
            this.dismiss()
        }
    }
}