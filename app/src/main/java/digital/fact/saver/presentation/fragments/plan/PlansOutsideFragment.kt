package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
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
import digital.fact.saver.presentation.adapters.recycler.PlansOutsideAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
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
                (resources.getDimension(R.dimen._32dp).toInt())
        )

        setObservers(this)
        binding.includeEmptyData.textViewNotFoundData.text =
                resources.getString(R.string.not_found_plans_outside)
        binding.includeEmptyData.textViewDescription.text =
                resources.getString(R.string.description_not_found_plans_outside)
    }

    override fun onResume() {
        super.onResume()
        plansVM.getPeriod()
        plansVM.updatePlans()
    }

    private fun initializedAdapters() {
        plansCurrentAdapter = PlansOutsideAdapter(
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
                val plansOutside = plans.filter {
                    it.planning_date <= unixFrom || it.planning_date >= unixTo
                }
                visibilityViewEmptyData(plansOutside.isEmpty())
                plansCurrentAdapter.submitList(plansOutside)
            }
        })

        plansVM.period.value?.let { period ->
            plansVM.getAllPlans().value?.let {plans ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansOutside = plans.filter {
                    it.planning_date <= unixFrom || it.planning_date >= unixTo
                }
                visibilityViewEmptyData(plansOutside.isEmpty())
                plansCurrentAdapter.submitList(plansOutside)
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

    private fun setSelectedTitle(selected: Int) {
        actionMode?.title = "${resources.getString(R.string.selected)} ${resources.getString(R.string.items)} $selected"
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

    override fun onPause() {
        super.onPause()
        actionMode?.finish()
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.delete_plans -> {
                selectionTracker?.let { tracker ->
                    val plansForDelete = mutableListOf<PlanTable>()
                    tracker.selection.forEach { id ->
                        val plan = plansCurrentAdapter.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }
                    plansForDelete.forEach {
                        plansVM.deletePlan(it).observe(this, {
                            plansVM.updatePlans()
                        })
                    }
                }
                true
            }
            else -> false
        }
    }
}