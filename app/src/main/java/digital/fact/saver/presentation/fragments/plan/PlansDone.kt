package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.databinding.FragmentPlansDoneBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.recycler.PlansAdapter
import digital.fact.saver.presentation.viewmodels.PlansViewModel

class PlansDone: Fragment() {

    private lateinit var binding: FragmentPlansDoneBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var plansAdapter: PlansAdapter
    var plans: List<Plan>? = null
    var selectedPlans: MutableList<Plan>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansDoneBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider.AndroidViewModelFactory(requireActivity().application).create(PlansViewModel::class.java)
        initializedAdapters()
        setObservers(this)
    }

    private fun initializedAdapters() {
        plansAdapter = PlansAdapter{
            plans?.let {plans ->
            for (i in plans.indices){
                plans
            }

            }
        }
        binding.recyclerPlansDone.adapter = plansAdapter
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            val plansDone = plans.filter { it.operation_id != 0 }
            this.plans = plansDone
            plansAdapter.submitList(plansDone)
        })
    }
}