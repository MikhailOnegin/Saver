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
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.LinearRvItemDecorations
import digital.fact.saver.utils.addCustomItemDecorator
import digital.fact.saver.utils.removeItemsDecorations

class PlansDoneFragment : Fragment(), ActionMode.Callback {

    private var actionMode: ActionMode? = null
    var selectionTracker: SelectionTracker<Long>? = null
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

        binding.recyclerPlansDone.addItemDecoration(
            LinearRvItemDecorations(
                sideMarginsDimension = R.dimen.screenContentPadding,
                marginBetweenElementsDimension = R.dimen.verticalMarginBetweenListElements
            )
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

    override fun onPause() {
        super.onPause()
        onDestroyActionMode(actionMode)
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
                //binding.recyclerPlansDone.removeItemsDecorations()
                //if(plansDone.isEmpty()) {
                //    binding.recyclerPlansDone.addCustomItemDecorator(
                //            0
                //    )
                //}
                //else {
                //    binding.recyclerPlansDone.addCustomItemDecorator(
                //            (resources.getDimension(R.dimen.xLargeMargin).toInt())
                //    )
                //}

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
                            else -> {}
                        }
                    }

                }
                visibilityViewEmptyData(plansDone.isEmpty() && plansDoneOutside.isEmpty())
                //binding.recyclerPlansDone.removeItemsDecorations()
                //if(plansDone.isEmpty()) {
                //    binding.recyclerPlansDone.addCustomItemDecorator(
                //            0
                //    )
                //}
                //else {
                //    binding.recyclerPlansDone.addCustomItemDecorator(
                //            (resources.getDimension(R.dimen.xLargeMargin).toInt())
                //    )
                //}
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
                        actionMode = null
                    } else
                        actionMode?.invalidate()

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
        this.actionMode = mode
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectionTracker?.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        val plans = selectionTracker?.selection?.firstOrNull{ (adapterPlansDone.getPlanById(it) is Plan)}
        mode?.menu?.clear()
        if (plans == null) {
            mode?.menuInflater?.inflate(R.menu.menu_plans_done_outside_period, menu)
            mode?.title = ""
        }
        else {
            mode?.menuInflater?.inflate(R.menu.menu_no_action, menu)
            mode?.title = "Нет доступных дейстивий"
        }
        onCreateActionMode(mode, menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.plans_done_outside_period_reset -> {
                selectionTracker?.let { tracker ->
                    val plansForReset = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansDone.getPlanById(id)
                        plan?.let {
                            plansForReset.add(it)
                        }
                    }

                    SlideToPerformDialog(title = getString(R.string.will_do_reset),
                            message = getString(R.string.you_will_reset_plans),
                            onSliderFinishedListener = {
                                plansForReset.forEach { plan ->
                                    if (plan is PlanDoneOutside) {
                                        plansVM.updatePlan(
                                                PlanTable(
                                                        plan.id, plan.type, plan.sum,
                                                        plan.name, 0L, plan.planning_date
                                                )).observe(this, {
                                            if (plan == plansForReset.last()) {
                                                Toast.makeText(requireContext(), "Сброшено", Toast.LENGTH_LONG).show()
                                                plansVM.updatePlans()
                                            }
                                        })
                                    }
                                }
                            }
                    ).show(childFragmentManager, "confirm-delete-dialog")
                }
                true
            }

            R.id.plans_done_outside_period_delete -> {
                selectionTracker?.let { tracker ->
                    val plansForDelete = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansDone.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }

                    SlideToPerformDialog(title = getString(R.string.will_do_delete),
                            message = getString(R.string.you_delete_plan_from_list),
                            onSliderFinishedListener = {
                                plansForDelete.forEach { plan ->
                                    if (plan is PlanDoneOutside) {
                                        plansVM.deletePlan(
                                                PlanTable(
                                                        plan.id, plan.type, plan.sum,
                                                        plan.name, plan.operation_id, plan.planning_date
                                                )).observe(this, {
                                            if (plan == plansForDelete.last()) {
                                                Toast.makeText(requireContext(), "Удалено", Toast.LENGTH_LONG).show()
                                                plansVM.updatePlans()
                                            }
                                        })
                                    }
                                }
                            }
                    ).show(childFragmentManager, "confirm-delete-dialog")
                }
                true
            }
            else -> false
        }
    }
}