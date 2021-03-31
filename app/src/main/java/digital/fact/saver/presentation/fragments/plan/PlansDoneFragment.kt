package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansDoneBinding
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.adapters.recycler.PlansDoneAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansDoneFragment : Fragment(), ActionMode.Callback {

    var selectionTracker: SelectionTracker<Long>? = null
    private var actionMode: ActionMode? = null
    private lateinit var binding: FragmentPlansDoneBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var adapterPlansDone: PlansDoneAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application)).get(
            PlansViewModel::class.java)
        initializedAdapters()
        binding.recyclerPlansDone.adapter = adapterPlansDone
        binding.recyclerPlansDone.addCustomItemDecorator(
                (resources.getDimension(R.dimen._32dp).toInt())
        )
        binding.includeEmptyData.textViewNotFoundData.text =
                resources.getString(R.string.not_found_plans_done)
        binding.includeEmptyData.textViewDescription.text =
                resources.getString(R.string.description_not_found_plans_done)
        selectionTracker = getSelectionTracker(adapterPlansDone, binding.recyclerPlansDone)
        adapterPlansDone.selectionTracker = selectionTracker
        setObservers(this)
    }

    override fun onResume() {
        super.onResume()
        plansVM.getPeriod()
        plansVM.updatePlans()
    }

    private fun initializedAdapters() {
        adapterPlansDone = PlansDoneAdapter(
                click = { id ->
                    RefactorPlanDialog(id).show(
                            childFragmentManager,
                            "Refactor Plan"
                    )
                }
        )
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansDone = plans.filter {
                    it.operation_id != 0 && it.planning_date > unixFrom && it.planning_date < unixTo
                }
                val plansDoneOutside = plans.filter { it.operation_id !=0 && it.planning_date < unixFrom || it.planning_date > unixTo }
                val planItems = toPlansItems(plansDone.toPlans(), plansDoneOutside.toPlansDoneOutside())
                visibilityViewEmptyData(plansDone.isEmpty() && plansDoneOutside.isEmpty())
                adapterPlansDone.submitList(planItems)
            }
        })

        plansVM.period.observe(owner, { period ->
            plansVM.getAllPlans().value?.let { plans ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansDone = plans.filter {
                    it.operation_id != 0 && it.planning_date > unixFrom && it.planning_date < unixTo
                }
                val plansDoneOutside = plans.filter { it.operation_id !=0 && it.planning_date < unixFrom || it.planning_date > unixTo }
                val planItems = toPlansItems(plansDone.toPlans(), plansDoneOutside.toPlansDoneOutside())
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
                        this@PlansDoneFragment.actionMode == null
                    } else {
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
        actionMode?.title = "${resources.getString(R.string.selected)} ${resources.getString(R.string.items)} $selected"
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
        mode?.menuInflater?.inflate(R.menu.plans_done_menu, menu) ?: return false
        actionMode = mode
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
            R.id.return_plans -> {
                selectionTracker?.let { tracker ->
                    val plansForReset = mutableListOf<PlanItem>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansDone.getPlanById(id)
                        plan?.let {
                            plansForReset.add(it)
                        }
                    }
                    plansForReset.forEach {
                        if (it is Plan) {
                            val planUpdate =
                                PlanTable(it.id, it.type, it.sum, it.name, 0, it.planning_date)
                            plansVM.updatePlan(planUpdate).observe(viewLifecycleOwner, {
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