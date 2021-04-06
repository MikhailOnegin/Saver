package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.databinding.FragmentPlanCompletedRefactorBinding
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.*
import java.util.*

class RefactorPlanFragment : Fragment() {

    private lateinit var binding: FragmentPlanCompletedRefactorBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    private lateinit var operationsVM: OperationsViewModel
    private var id: Long? = null
    private var plan: PlanTable? = null

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
        operationsVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(OperationsViewModel::class.java)
        id = arguments?.getLong("planId")
        navC = findNavController()
        setListeners()
        setObservers(this)
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                this.id?.let { id ->
                    for (plan in plans) {
                        if (plan.id == id) {
                            this.plan = plan
                            val unixFrom = period.dateFrom.time.time
                            val unixTo = period.dateTo.time.time
                            val inPeriod = plan.planning_date in unixFrom..unixTo
                            if (inPeriod && plan.operation_id == 0L) {
                                binding.imageViewMark.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_mark_pink))
                                binding.textViewInRange.text = resources.getString(R.string.plan_completed_in_period)
                                binding.toolbar.inflateMenu(R.menu.menu_plan_done_in_period)
                            } else {
                                binding.imageViewMark.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_mark_purple))
                                binding.textViewInRange.text = resources.getString(R.string.plan_completed_outside_period)
                                if( plan.operation_id == 0L)  binding.toolbar.inflateMenu(R.menu.menu_plan_outside)
                                else binding.toolbar.inflateMenu(R.menu.menu_plan_done_outside_period)
                            }
                            binding.textViewSumLogo.text =
                                    if (plan.type == PlanTable.PlanType.SPENDING.value)
                                        resources.getString(R.string.plan_spending_2)
                                    else resources.getString(R.string.plan_income_2)
                            binding.textViewFactLogo.text =
                                    if (plan.type == PlanTable.PlanType.SPENDING.value)
                                        resources.getString(R.string.fact_spent)
                                    else resources.getString(R.string.fact_income)
                            binding.toolbar.subtitle =
                                    if (plan.type == PlanTable.PlanType.SPENDING.value)
                                        resources.getString(R.string.spend)
                                    else resources.getString(R.string.income)
                            binding.editTextDescription.setText(plan.name)
                            binding.editTextSum.setText((plan.sum.toDouble() / 100 ).toString())
                            operationsVM.getAllOperations().value?.let { operations ->
                                operations.firstOrNull{it.plan_id == plan.operation_id}?.let { operation ->
                                    binding.textViewFactSum.text = (operation.sum.toDouble() / 100).toString()
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
                R.id.plan_refactor_done_in_range_delete -> {
                    plan?.let { currentPlan ->
                        plansVM.deletePlan(currentPlan).observe(viewLifecycleOwner, {
                            Toast.makeText(requireContext(), getString(R.string.deleted), Toast.LENGTH_SHORT).show()
                            navC.popBackStack()
                        })
                    }
                }
                R.id.plan_refactor_done_in_range_reset -> {
                    plan?.let { currentPlan ->
                        val updatePlan = PlanTable(
                                id = currentPlan.id,
                                type = currentPlan.type,
                                sum = currentPlan.sum,
                                name = currentPlan.name,
                                operation_id = 0,
                                planning_date = currentPlan.planning_date
                        )
                        plansVM.updatePlan(updatePlan).observe(viewLifecycleOwner, {
                            Toast.makeText(requireContext(), resources.getString(R.string.reseted), Toast.LENGTH_SHORT).show()
                            navC.popBackStack()
                        })
                    }
                }
                R.id.plan_refactor_done_in_range_show_history -> {
                    Toast.makeText(requireContext(), "Ne gotovo", Toast.LENGTH_SHORT).show()
                }
                R.id.plan_refactor_done_outside_range_show_in_history -> {
                    Toast.makeText(requireContext(), "Ne gotovo", Toast.LENGTH_SHORT).show()

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
                val sum: Long =
                        (round(binding.editTextSum.text.toString().toDouble(), 2) * 100).toLong()
                if (checkFieldsValid()) {
                    val plan = PlanTable(
                            id = planCurrent.id,
                            type = planCurrent.type,
                            name = binding.editTextDescription.text.toString(),
                            operation_id = planCurrent.operation_id,
                            planning_date = planCurrent.planning_date,
                            sum = sum
                    )
                    plansVM.updatePlan(plan).observe(viewLifecycleOwner, {
                        Toast.makeText(requireContext(), resources.getString(R.string.saved_2), Toast.LENGTH_SHORT).show()
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
                && binding.editTextSum.text.isNotBlank() && binding.editTextSum.text.toString()
                .toDouble() != 0.0
    }
}