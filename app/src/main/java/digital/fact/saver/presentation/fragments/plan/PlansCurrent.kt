package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import digital.fact.saver.databinding.FragmentPlansCurrentBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.PlansCurrentAdapter

class PlansCurrent: Fragment() {

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlans: PlansCurrentAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansCurrentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        initializedAdapters()
        binding.recyclerViewPlans.adapter = adapterPlans
        // test
        val plans = listOf(Plan(Plan.PlanCategory.CONSUMPTION, 2,2,2,2),
            Plan(Plan.PlanCategory.CONSUMPTION, 12,2,2,2),
            Plan(Plan.PlanCategory.CONSUMPTION, 24,62,12,4)
            )
        adapterPlans.submitList(plans)
    }

    private fun initializedAdapters() {
        adapterPlans = PlansCurrentAdapter()
    }
}