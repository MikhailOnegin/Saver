package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
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
import digital.fact.saver.databinding.FragmentPlansCurrentBinding
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.toPlans
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator
import java.util.*

class PlansCurrentFragment : Fragment(), ActionMode.Callback {

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlansCurrent: PlansCurrentAdapter
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    private var actionMode: ActionMode? = null
    var selectionTracker: SelectionTracker<Long>? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansCurrentBinding.inflate(inflater, container, false)
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
        binding.recyclerPlansCurrent.adapter = adapterPlansCurrent
        selectionTracker = getSelectionTracker(adapterPlansCurrent, binding.recyclerPlansCurrent)
        adapterPlansCurrent.selectionTracker = selectionTracker
        binding.recyclerPlansCurrent.addCustomItemDecorator(
                (resources.getDimension(R.dimen._32dp).toInt())
        )
        setObservers(this)
        binding.includeEmptyData.textViewNotFoundData.text =
                resources.getString(R.string.not_found_plans_current)
        binding.includeEmptyData.textViewDescription.text =
                resources.getString(R.string.description_not_found_plans_current)
    }

    override fun onResume() {
        super.onResume()
        plansVM.getPeriod()
        plansVM.updatePlans()
        plansVM.showRecyclerAnim.value?.let {
            if (it) {
                binding.recyclerPlansCurrent.layoutAnimation = AnimationUtils.loadLayoutAnimation(requireContext(), R.anim.anim_fall_down)
            } else {
                binding.recyclerPlansCurrent.layoutAnimation = null
            }
            plansVM.showRecyclerAnim.value = false
        }
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.period.observe(owner, { period ->
            plansVM.getAllPlans().value?.let { plans ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansCurrent = plans.filter {
                    it.operation_id == 0L && it.planning_date >= unixFrom && it.planning_date <= unixTo
                }
                val plansCurrentSorted = plansCurrent.sortedBy { it.planning_date }
                visibilityViewEmptyData(plansCurrent.isEmpty())
                adapterPlansCurrent.submitList(plansCurrentSorted.toPlans())
            }
        })
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansCurrent = plans.filter {
                    it.operation_id == 0L && it.planning_date > unixFrom && it.planning_date < unixTo
                }
                val plansCurrentSorted = plansCurrent.sortedBy { it.planning_date }
                visibilityViewEmptyData(plansCurrent.isEmpty())
                adapterPlansCurrent.submitList(plansCurrentSorted.toPlans())
            }
        })

        selectionTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                selectionTracker?.let {
                    if (it.hasSelection() && this@PlansCurrentFragment.actionMode == null) {
                        actionMode = requireActivity().startActionMode(this@PlansCurrentFragment)
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

    private fun initializedAdapters() {
        adapterPlansCurrent = PlansCurrentAdapter(
                click = { id ->
                    val bundle = Bundle()
                    bundle.putLong("planId",id)
                    navC.navigate(R.id.action_plansFragment_toRefactorPlanFragment, bundle)
                }
        )
    }

    private fun setSelectedTitle(selected: Int) {
        actionMode?.title = "${resources.getString(R.string.selected)} ${resources.getString(R.string.items)} $selected"
    }

    private fun getSelectionTracker(
            currentAdapter: PlansCurrentAdapter,
            recycler: RecyclerView
    ): SelectionTracker<Long> {
        return SelectionTracker.Builder(
                "mySelection",
                recycler,
                PlansCurrentAdapter.MyItemKeyProvider(currentAdapter),
                PlansCurrentAdapter.MyItemDetailsLookup(recycler),
                StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
                SelectionPredicates.createSelectAnything()
        ).build()
    }

    private fun visibilityViewEmptyData(visibility: Boolean) {
        if (visibility) {
            binding.recyclerPlansCurrent.visibility = View.GONE
            binding.includeEmptyData.root.visibility = View.VISIBLE
        } else {
            binding.recyclerPlansCurrent.visibility = View.VISIBLE
            binding.includeEmptyData.root.visibility = View.GONE
        }
    }

    private fun onBlurViewHeightChanged(newHeight: Int) {
        binding.recyclerPlansCurrent.updatePadding(bottom = newHeight)
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
                    val plansForDelete = mutableListOf<Plan>()
                    tracker.selection.forEach { id ->
                        val plan = adapterPlansCurrent.getPlanById(id)
                        plan?.let {
                            plansForDelete.add(it)
                        }
                    }
                    plansForDelete.forEach {
                        plansVM.deletePlan(
                                PlanTable(
                                        it.id, it.type, it.sum,
                                        it.name, it.operation_id, it.planning_date
                                )).observe(this, {
                            Toast.makeText(requireContext(), "Планы удалены", Toast.LENGTH_LONG).show()
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