package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.databinding.FragmentPlansBinding
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.adapters.pagers.PlansPagerAdapter
import digital.fact.saver.presentation.adapters.recycler.PlansAdapter
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.LinearRvItemDecorations

class PlansFragment(private val typeFragment: PlansPagerAdapter.FragmentPlanType) : Fragment(),
    ActionMode.Callback {

    private lateinit var binding: FragmentPlansBinding
    private lateinit var adapterPlans: PlansAdapter
    private lateinit var plansVM: PlansViewModel
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var navC: NavController
    private var actionMode: ActionMode? = null
    var selectionTracker: SelectionTracker<Long>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
            .get(PlansViewModel::class.java)
        operationsVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(
            OperationsViewModel::class.java
        )
        navC = findNavController()
        initializedAdapters()
        selectionTracker = getSelectionTracker(adapterPlans, binding.recyclerPlans)
        adapterPlans.selectionTracker = selectionTracker
        binding.recyclerPlans.addItemDecoration(
            LinearRvItemDecorations(
                sideMarginsDimension = R.dimen.screenContentPadding,
                marginBetweenElementsDimension = R.dimen.verticalMarginBetweenListElements
            )
        )
        setObservers(this)
        binding.includeEmptyData.title.text =
            resources.getString(R.string.not_found_plans_current)
        binding.includeEmptyData.hint.text =
            resources.getString(R.string.description_not_found_plans_current)
    }

    override fun onResume() {
        super.onResume()
        //java.lang.NullPointerException: Attempt to invoke virtual method 'boolean android.view.animation.LayoutAnimationController.isDone()' on a null object reference
        //plansVM.showRecyclerAnim.value?.let {
        //    val g = 6
        //    if (it) {
        //        binding.recyclerPlans.layoutAnimation =
        //            AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.anim_fall_down)
        //    } else {
        //        binding.recyclerPlans.layoutAnimation = null
        //    }
        //    plansVM.showRecyclerAnim.value = false
        //}
    }

    override fun onPause() {
        super.onPause()
        onDestroyActionMode(actionMode)
    }

    private fun getSelectionTracker(
        currentAdapter: PlansAdapter,
        recycler: RecyclerView
    ): SelectionTracker<Long> {
        return SelectionTracker.Builder(
            "mySelection",
            recycler,
            PlansAdapter.MyItemKeyProvider(currentAdapter),
            PlansAdapter.MyItemDetailsLookup(recycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                submitData(typeFragment, plans, period, operationsVM)
            }
        })
        plansVM.period.observe(owner, { period ->
            plansVM.getAllPlans().observe(owner, { plans ->
                submitData(typeFragment, plans, period, operationsVM)
            })
        })
    }

    private fun initializedAdapters() {
        adapterPlans = PlansAdapter(
            clickPlanCurrent = { id ->
                val bundle = Bundle()
                bundle.putLong("planId", id)
                navC.navigate(R.id.action_plansFragment_toRefactorPlanFragment, bundle)
            },
            clickPlanDone = { id ->
                val bundle = Bundle()
                bundle.putLong("planId", id)
                navC.navigate(R.id.action_plansFragment_toRefactorCompletedPlanFragment, bundle)
            },
            clickPlanOutside = { id ->
                val bundle = Bundle()
                bundle.putLong("planId", id)
                navC.navigate(R.id.action_plansFragment_toRefactorCompletedPlanFragment, bundle)
            }
        )
        binding.recyclerPlans.adapter = adapterPlans
    }

    private fun visibilityViewEmptyData(visibility: Boolean) {
        if (visibility) {
            binding.recyclerPlans.visibility = View.GONE
            binding.includeEmptyData.root.visibility = View.VISIBLE
        } else {
            binding.recyclerPlans.visibility = View.VISIBLE
            binding.includeEmptyData.root.visibility = View.GONE
        }
    }

    private fun submitData(
        typeFragment: PlansPagerAdapter.FragmentPlanType, plans: List<DbPlan>, period: Period,
        operationsViewModel: OperationsViewModel
    ) {
        val unixFrom = period.dateFrom.time.time
        val unixTo = period.dateTo.time.time
        val submitList = arrayListOf<PlanItem>()
        when (typeFragment) {
            PlansPagerAdapter.FragmentPlanType.FRAGMENT_CURRENT -> {
                val plansCurrent = plans.filter {
                    it.operation_id == 0L
                }.filter { it.planning_date in unixFrom..unixTo || it.planning_date == 0L }
                val plansCurrentSorted = plansCurrent.sortedBy { it.planning_date }
                val result = plansCurrentSorted.toPlans()
                result.forEach {
                    it.inPeriod = true
                    it.status = PlanStatus.CURRENT
                }
                submitList.addAll(result)
            }
            PlansPagerAdapter.FragmentPlanType.FRAGMENT_DONE -> {

                val plansDone = plans.filter {
                    it.operation_id != 0L
                }.filter { it.planning_date in unixFrom..unixTo }
                val plansDoneWithOperation = mutableListOf<Plan>()
                plansDone.forEach { planDone ->
                    var sumFact = 0L
                    operationsViewModel.getAllOperations().value?.let { operations ->
                        operations.firstOrNull { it.plan_id == planDone.operation_id }
                            ?.let {
                                sumFact = it.sum
                            }
                    }
                    plansDoneWithOperation.add(
                        Plan(
                            planDone.id,
                            planDone.type,
                            planDone.sum,
                            planDone.name,
                            planDone.operation_id,
                            planDone.planning_date,
                            sumFact,
                            true,
                            PlanStatus.DONE
                        )
                    )
                }

                val plansDoneOutside = plans.filter { it.operation_id != 0L }
                    .filter { it.planning_date !in unixFrom..unixTo }
                val plansDoneOutsideWithOperation = mutableListOf<Plan>()
                plansDoneOutside.forEach { planDone ->
                    var sumFact = 0L
                    operationsVM.getAllOperations().value?.let { operations ->
                        plansDone.forEach { plan ->
                            operations.firstOrNull { it.plan_id == plan.operation_id }
                                ?.let { sumFact = it.sum }
                        }
                    }
                    plansDoneOutsideWithOperation.add(
                        Plan(
                            planDone.id,
                            planDone.type,
                            planDone.sum,
                            planDone.name,
                            planDone.operation_id,
                            planDone.planning_date,
                            sumFact,
                            true,
                            PlanStatus.DONE_OUTSIDE
                        )
                    )
                }
                submitList.addAll(
                    toPlansItems(
                        plansDoneWithOperation,
                        plansDoneOutsideWithOperation
                    )
                )
            }
            PlansPagerAdapter.FragmentPlanType.FRAGMENT_OUTSIDE -> {
                val plansOutside = plans.filter { it.operation_id == 0L }
                    .filter { it.planning_date !in unixFrom..unixTo && it.planning_date != 0L }
                val result = plansOutside.toPlans()
                result.forEach {
                    it.inPeriod = false
                    it.status = PlanStatus.OUTSIDE
                }
                submitList.addAll(result)
            }
        }

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                selectionTracker?.let {
                    if (it.hasSelection() && this@PlansFragment.actionMode == null) {
                        actionMode = requireActivity().startActionMode(this@PlansFragment)
                    } else if (!it.hasSelection()) {
                        actionMode?.finish()
                        actionMode = null
                    } else {
                        setSelectedTitle(it.selection.size())
                        actionMode?.invalidate()
                    }
                }
            }
        })

        visibilityViewEmptyData(submitList.isEmpty())
        adapterPlans.submitList(submitList)
    }

    private fun setSelectedTitle(selected: Int) {
        actionMode?.title = "${resources.getString(R.string.selected_plans)} $selected"
    }

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        this.actionMode = mode
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menu?.clear()
        val plans = arrayListOf<Plan>()
        selectionTracker?.selection?.let { selection ->
            selection.forEach {
                val planItem = adapterPlans.getPlanById(it)
                if (planItem is Plan)
                    plans.add(planItem)
            }
        }
        val type: PlanStatus? = when {
            plans.all { it.status == PlanStatus.CURRENT } -> PlanStatus.CURRENT
            plans.all { it.status == PlanStatus.DONE } -> PlanStatus.DONE
            plans.all { it.status == PlanStatus.DONE_OUTSIDE } -> PlanStatus.DONE_OUTSIDE
            plans.all { it.status == PlanStatus.OUTSIDE } -> PlanStatus.OUTSIDE
            else -> null
        }

        when (type) {
            PlanStatus.CURRENT ->
                mode?.menuInflater?.inflate(R.menu.menu_plans_current_menu, menu) ?: return false
            PlanStatus.DONE -> {
                mode?.menuInflater?.inflate(R.menu.menu_no_action, menu) ?: return false
                mode.title = "Нет доступных дейстивий"
            }
            PlanStatus.DONE_OUTSIDE -> {
                mode?.menuInflater?.inflate(R.menu.menu_plans_done_outside_period, menu)
                    ?: return false
            }
            PlanStatus.OUTSIDE -> {
                mode?.menuInflater?.inflate(R.menu.menu_plans_current_menu, menu) ?: return false
            }
            else -> {
                mode?.menuInflater?.inflate(R.menu.menu_no_action, menu) ?: return false
                mode.title = ""
            }
        }
        onCreateActionMode(mode, menu)
        return true
    }

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.plans_done_outside_period_reset -> {
                selectionTracker?.let { tracker ->
                    val plansForReset = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlans.getPlanById(id)
                        plan?.let {
                            plansForReset.add(it)
                        }
                    }

                    SlideToPerformDialog(title = getString(R.string.will_do_reset),
                        message = getString(R.string.you_will_reset_plans),
                        onSliderFinishedListener = {
                            plansForReset.forEach { plan ->
                                if (plan is Plan) {
                                    plansVM.updatePlan(
                                        DbPlan(
                                            plan.id, plan.type, plan.sum,
                                            plan.name, 0L, 0
                                        )
                                    ).observe(this, {
                                        if (plan == plansForReset.last()) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Сброшено",
                                                Toast.LENGTH_LONG
                                            ).show()
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
                        val plan = adapterPlans.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }

                    SlideToPerformDialog(title = getString(R.string.will_do_delete),
                        message = getString(R.string.you_delete_plan_from_list),
                        onSliderFinishedListener = {
                            plansForDelete.forEach { plan ->
                                if (plan is Plan) {
                                    plansVM.deletePlan(
                                        DbPlan(
                                            plan.id, plan.type, plan.sum,
                                            plan.name, plan.operation_id, plan.planning_date
                                        )
                                    ).observe(this, {
                                        if (plan == plansForDelete.last()) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Удалено",
                                                Toast.LENGTH_LONG
                                            ).show()
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

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectionTracker?.clearSelection()
    }
}