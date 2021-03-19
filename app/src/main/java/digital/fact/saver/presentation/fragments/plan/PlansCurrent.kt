package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.*
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
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansCurrent : Fragment(), ActionMode.Callback{
    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }
    override fun onDestroyActionMode(mode: ActionMode?) {
    }
    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        return true
    }

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
       return false
    }

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlansCurrent: PlansCurrentAdapter
    private lateinit var plansVM: PlansViewModel
    private lateinit var navC: NavController
    private var actionMode: ActionMode? = null
    var selectedTracker: SelectionTracker<Long>? = null

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
        selectedTracker = getSelectionTracker(adapterPlansCurrent, binding.recyclerPlansCurrent)
        adapterPlansCurrent.selectionTracker = selectedTracker
        binding.recyclerPlansCurrent.addCustomItemDecorator((resources.getDimension(R.dimen._32dp).toInt()))
        setObservers(this)
        binding.includeEmptyData.textViewNotFoundData.text = "Нет выполненных планов"
        binding.includeEmptyData.textViewDescription.text = "Здесь будут отображаться выполненные планы\n" +
                "Планы прошедших периодов можно сбросить, чтобы перенести их в текущий спланированный период"

    }

    override fun onResume() {
        super.onResume()
        plansVM.getPeriod()
        plansVM.updatePlans()
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                if (plans.isNotEmpty()) {
                    binding.recyclerPlansCurrent.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE

                } else {
                    binding.recyclerPlansCurrent.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansCurrent = plans.filter { it.operation_id == 0 && it.planning_date > unixFrom && it.planning_date < unixTo }
                if (plansCurrent.isNotEmpty()) {
                    binding.recyclerPlansCurrent.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE
                } else {
                    binding.recyclerPlansCurrent.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                adapterPlansCurrent.submitList(plansCurrent)
            }
        })


        selectedTracker?.addObserver(object : SelectionTracker.SelectionObserver<Long>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                selectedTracker?.let {
                    if (it.hasSelection() && actionMode == null) {
                        actionMode = requireActivity().startActionMode(ActionModeController(it))
                        setSelectedTitle(it.selection.size())
                    } else if (!it.hasSelection()) {
                        actionMode?.finish()
                        actionMode = null
                    } else {
                        setSelectedTitle(it.selection.size())
                    }
                }

            }
        })
    }


    private fun initializedAdapters() {
        adapterPlansCurrent = PlansCurrentAdapter(
            click = { id ->RefactorPlanDialog(id).show(
                childFragmentManager,
                "Refactor Plan"
            )}
        )
    }

class ActionModeController(
        private val tracker: SelectionTracker<*>
) : ActionMode.Callback {

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.plans_menu, menu)
        return true
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        tracker.clearSelection()
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = true

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean = when (item.itemId) {
        R.id.delete-> {
            mode.finish()
            true
        }
        else -> false
    }

}

private fun setSelectedTitle(selected: Int) {
    actionMode?.title = "Selected: $selected"
}



    private fun getSelectionTracker(
            adapter: PlansCurrentAdapter,
            recycler: RecyclerView
    ) : SelectionTracker<Long>{
        return SelectionTracker.Builder(
            "mySelection",
            recycler,
            PlansCurrentAdapter.MyItemKeyProvider(adapter),
            PlansCurrentAdapter.MyItemDetailsLookup(recycler),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()
    }


}