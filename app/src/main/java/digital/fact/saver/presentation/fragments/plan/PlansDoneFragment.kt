package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansDoneBinding
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.adapters.recycler.PlansDoneAdapter
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansDoneFragment : Fragment(), ActionMode.Callback {

    var selectionTracker: SelectionTracker<Long>? = null
    private var actionMode: ActionMode? = null
    private lateinit var binding: FragmentPlansDoneBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var adapterPlansDone: PlansDoneAdapter
    private lateinit var navC: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(
            PlansViewModel::class.java
        )
        operationsVM = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(
                OperationsViewModel::class.java
        )
        navC = findNavController()
        initializedAdapters()
        binding.recyclerPlansDone.adapter = adapterPlansDone
        selectionTracker = getSelectionTracker(adapterPlansDone, binding.recyclerPlansDone)
        adapterPlansDone.selectionTracker = selectionTracker
        binding.recyclerPlansDone.addCustomItemDecorator(
            (resources.getDimension(R.dimen._32dp).toInt())
        )
        setObservers(this)
        binding.includeEmptyData.title.text =
            resources.getString(R.string.not_found_plans_done)
        binding.includeEmptyData.hint.text =
            resources.getString(R.string.description_not_found_plans_done)
    }

    override fun onResume() {
        super.onResume()
        plansVM.getPeriod()
        plansVM.updatePlans()
    }

    private fun initializedAdapters() {
        adapterPlansDone = PlansDoneAdapter(
                click = { id ->
                    val bundle = Bundle()
                    bundle.putLong("planId", id)
                    navC.navigate(R.id.action_plansFragment_toRefactorCompletedPlanFragment, bundle)
                }
        )
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansDone = plans.filter {
                    it.operation_id != 0L
                }.filter { it.planning_date in unixFrom..unixTo }
                val plansDoneOutside = plans.filter { it.operation_id != 0L }
                        .filter { it.planning_date !in unixFrom..unixTo }
                val planItems =
                        toPlansItems(plansDone.toPlans(), plansDoneOutside.toPlansDoneOutside())
                operationsVM.getAllOperations().value?.let { operations ->
                    planItems.forEach { plan ->
                        when (plan) {
                            is Plan -> {
                                operations.firstOrNull { it.plan_id == plan.operation_id }?.let { plan.sum_fact = it.sum }
                            }
                            is PlanDoneOutside -> {
                                operations.firstOrNull { it.plan_id == plan.operation_id }?.let { plan.sum_fact = it.sum }
                            }
                            else -> {
                            }
                        }
                    }
                }
                visibilityViewEmptyData(plansDone.isEmpty() && plansDoneOutside.isEmpty())
                adapterPlansDone.submitList(planItems)
            }
        })

        plansVM.period.observe(owner, { period ->
            plansVM.getAllPlans().value?.let { plans ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansDone = plans.filter {
                    it.operation_id != 0L
                }.filter { it.planning_date in unixFrom..unixTo }
                val plansDoneOutside = plans.filter { it.operation_id != 0L }
                    .filter { it.planning_date !in unixFrom..unixTo }
                val planItems =
                    toPlansItems(plansDone.toPlans(), plansDoneOutside.toPlansDoneOutside())
                operationsVM.getAllOperations().value?.let { operations ->
                    planItems.forEach { plan ->
                        when (plan) {
                            is Plan -> {
                                operations.firstOrNull { it.plan_id == plan.operation_id }?.let { plan.sum_fact = it.sum }
                            }
                            is PlanDoneOutside -> {
                                operations.firstOrNull { it.plan_id == plan.operation_id }?.let { plan.sum_fact = it.sum }
                            }
                            else -> {
                            }
                        }
                    }
                }
                visibilityViewEmptyData(plansDone.isEmpty() && plansDoneOutside.isEmpty())
                adapterPlansDone.submitList(planItems)
            }
        })

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                selectionTracker?.let {
                    if (it.hasSelection() && this@PlansDoneFragment.actionMode == null) {
                        actionMode = requireActivity().startActionMode(this@PlansDoneFragment)
                    } else if (!it.hasSelection()) {
                        actionMode?.finish()
                        actionMode == null
                    } else {
                        actionMode?.invalidate()
                        setSelectedTitle(it.selection.size())
                    }
                }
            }
        })

        plansVM.plansBlurViewHeight.observe(owner) { onBlurViewHeightChanged(it) }
    }

    private fun visibilityViewEmptyData(visibility: Boolean) {
        if (visibility) {
            binding.constraintRecycler.visibility = View.GONE
            binding.includeEmptyData.root.visibility = View.VISIBLE
        } else {
            binding.constraintRecycler.visibility = View.VISIBLE
            binding.includeEmptyData.root.visibility = View.GONE
        }
    }

    private fun setSelectedTitle(selected: Int) {
        actionMode?.title =
            "${resources.getString(R.string.selected)} ${resources.getString(R.string.items)} $selected"
    }

    private fun getSelectionTracker(
        currentAdapter: PlansDoneAdapter,
        recycler: RecyclerView
    ): SelectionTracker<Long> {
        return SelectionTracker.Builder(
            "mySelection",
            recycler,
            PlansDoneAdapter.MyItemKeyProvider(currentAdapter),
            PlansDoneAdapter.MyItemDetailsLookup(recycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
    }


    private fun onBlurViewHeightChanged(newHeight: Int) {
        binding.recyclerPlansDone.updatePadding(bottom = newHeight)
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.menu_plans_done_outside_period, menu) ?: return false
        this.actionMode = mode
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectionTracker?.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.plans_done_outside_period_reset-> {
                selectionTracker?.let { tracker ->
                    val plansForReset = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansDone.getPlanById(id)
                        plan?.let {
                            plansForReset.add(it)
                        }
                    }
                    plansForReset.forEach {
                        if (it is PlanDoneOutside) {
                            val planUpdate =
                                PlanTable(it.id, it.type, it.sum, it.name, 0, it.planning_date)
                            plansVM.updatePlan(planUpdate).observe(viewLifecycleOwner, {
                                Toast.makeText(requireContext(), "Планы сброшены", Toast.LENGTH_SHORT).show()
                                plansVM.updatePlans()
                            })
                        }
                    }
                }
                true
            }

            R.id.plans_done_outside_period_delete-> {
                selectionTracker?.let { tracker ->
                    val plansForDelete = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansDone.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }
                    plansForDelete.forEach {
                        if (it is PlanDoneOutside) {
                            val planDelete =
                                PlanTable(it.id, it.type, it.sum, it.name, 0, it.planning_date)
                            plansVM.deletePlan(planDelete).observe(viewLifecycleOwner, {
                                Toast.makeText(requireContext(), "Планы удалены", Toast.LENGTH_SHORT).show()
                                plansVM.updatePlans()
                            })
                        }
                    }
                }
                true
            }

            else -> false
        }
    }
}