package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.databinding.FragmentPlansCurrentBinding
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import java.util.*

class PlansCurrent : Fragment() {

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlansCurrent: PlansCurrentAdapter
    private lateinit var plansVM: PlansViewModel
    private var selectedMode: Boolean = false

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
        initializedAdapters()
        setObservers(this)
        binding.recyclerViewPlans.adapter = adapterPlansCurrent
        plansVM.getPeriod()
        plansVM.updatePlans()
    }

    override fun onResume() {
        super.onResume()
        plansVM.updatePlans()
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, { plans ->
            plansVM.period.value?.let { period ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansCurrent = plans.filter { it.operation_id == 0 && it.planning_date > unixFrom && it.planning_date < unixTo }
                adapterPlansCurrent.submitList(plansCurrent)
            }

            //binding.recyclerViewPlans.addItemDecoration(RecyclerView.ItemDecoration(), 0 )
        })

    }

    private fun initializedAdapters() {
        adapterPlansCurrent = PlansCurrentAdapter(
            clickPlan = { id ->
                RefactorPlanDialog(id).show(
                    childFragmentManager, "refactor Plan"
                )
            },
            longClickPlan = {
                if(it){

                }
                else {

                }
            })
    }
}