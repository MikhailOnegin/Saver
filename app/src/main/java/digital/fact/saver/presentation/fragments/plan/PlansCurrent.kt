package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.databinding.FragmentPlansCurrentBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.adapters.recycler.PlansAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.presentation.viewmodels.TestDatabaseViewModel

class PlansCurrent: Fragment() {

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlans: PlansAdapter
    private lateinit var plansVM: PlansViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansCurrentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(PlansViewModel::class.java)
        initializedAdapters()
        setObservers(this)
        binding.recyclerViewPlans.adapter = adapterPlans

    }

    override fun onResume() {
        super.onResume()
        plansVM.updatePlans()
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe( owner, {plans ->
            val plansCurrent = plans.filter { it.operation_id == 0}
            adapterPlans.submitList(plansCurrent)
        //binding.recyclerViewPlans.addItemDecoration(RecyclerView.ItemDecoration(), 0 )
        })

    }

    private fun initializedAdapters() {
        adapterPlans = PlansAdapter { id ->
            RefactorPlanDialog(id).show(
                childFragmentManager, "refactor Plan")
        }
    }
}