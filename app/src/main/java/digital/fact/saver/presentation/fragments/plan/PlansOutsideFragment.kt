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
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.databinding.FragmentPlansOutsideBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.toPlans
import digital.fact.saver.presentation.adapters.recycler.PlansOutsideAdapter
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansOutsideFragment : Fragment(), ActionMode.Callback {

    private lateinit var plansVM: PlansViewModel
    private lateinit var plansCurrentAdapter: PlansOutsideAdapter
    private lateinit var binding: FragmentPlansOutsideBinding
    private lateinit var navC: NavController
    private var actionMode: ActionMode? = null
    var selectionTracker: SelectionTracker<Long>? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansOutsideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
                .get(PlansViewModel::class.java)
        navC = findNavController()
        initializedAdapters()
        binding.recyclerPlansOutside.adapter = plansCurrentAdapter
        selectionTracker = getSelectionTracker(plansCurrentAdapter, binding.recyclerPlansOutside)
        plansCurrentAdapter.selectionTracker = selectionTracker
        binding.recyclerPlansOutside.addCustomItemDecorator(
                (resources.getDimension(R.dimen.xLargeMargin).toInt())
        )

        setObservers(this)
        binding.includeEmptyData.title.text =
                resources.getString(R.string.not_found_plans_outside)
        binding.includeEmptyData.hint.text =
                resources.getString(R.string.description_not_found_plans_outside)
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
        plansCurrentAdapter = PlansOutsideAdapter(
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
                val plansOutside = plans.filter { it.operation_id == 0L }.filter { it.planning_date !in unixFrom..unixTo && it.planning_date != 0L }
                visibilityViewEmptyData(plansOutside.isEmpty())
                plansCurrentAdapter.submitList(plansOutside.toPlans())
            }
        })

        plansVM.period.value?.let { period ->
            plansVM.getAllPlans().value?.let { plans ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansOutside = plans.filter{it.operation_id == 0L}.filter{it.planning_date !in unixFrom..unixTo && it.planning_date != 0L}
                visibilityViewEmptyData(plansOutside.isEmpty())
                plansCurrentAdapter.submitList(plansOutside.toPlans())
            }
        }

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                selectionTracker?.let {
                    if (it.hasSelection() && this@PlansOutsideFragment.actionMode == null) {
                        actionMode = requireActivity().startActionMode(this@PlansOutsideFragment)
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
            binding.recyclerPlansOutside.visibility = View.GONE
            binding.includeEmptyData.root.visibility = View.VISIBLE
        } else {
            binding.recyclerPlansOutside.visibility = View.VISIBLE
            binding.includeEmptyData.root.visibility = View.GONE
        }
    }

    private fun getSelectionTracker(
            currentAdapter: PlansOutsideAdapter,
            recycler: RecyclerView
    ): SelectionTracker<Long> {
        return SelectionTracker.Builder(
                "mySelection",
                recycler,
                PlansOutsideAdapter.MyItemKeyProvider(currentAdapter),
                PlansOutsideAdapter.MyItemDetailsLookup(recycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
        ).build()
    }

    private fun onBlurViewHeightChanged(newHeight: Int) {
        binding.recyclerPlansOutside.updatePadding(bottom = newHeight)
    }


    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.menu_plans_current_menu, menu) ?: return false
        this.actionMode = mode
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        selectionTracker?.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_plans -> {
                selectionTracker?.let { tracker ->
                    val plansForDelete = mutableListOf<Plan>()
                    tracker.selection.forEach { id ->
                        val plan = plansCurrentAdapter.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }
                    SlideToPerformDialog(title = getString(R.string.will_do_delete),
                            description = getString(R.string.you_delete_plan_from_list),
                            onSliderFinishedListener = {
                                plansForDelete.forEach { plan ->
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
                    ).show(childFragmentManager, "confirm-delete-dialog")

                }
                true
            }
            else -> false
        }
    }
}