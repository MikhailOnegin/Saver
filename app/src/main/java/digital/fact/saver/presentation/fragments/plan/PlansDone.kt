package digital.fact.saver.presentation.fragments.plan

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansDoneBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.recycler.PlansDoneAdapter
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansDone: Fragment() {

    private lateinit var binding: FragmentPlansDoneBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var plansDoneAdapter: PlansDoneAdapter
    var plans: List<Plan>? = null

    var countSelectedPlans:Int = 0

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
        binding.recyclerPlansDone.adapter = plansDoneAdapter
        binding.recyclerPlansDone.addCustomItemDecorator((resources.getDimension(R.dimen._32dp).toInt()))
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

    private fun initializedAdapters() {
        plansDoneAdapter = PlansDoneAdapter(
         longClickPlan = {

             if(it){
                 --countSelectedPlans
                 if(countSelectedPlans == 0){
                     binding.floatingActionButton.visibility = View.INVISIBLE
                 }
             }
             else{
                 binding.floatingActionButton.visibility = View.VISIBLE
                 ++countSelectedPlans
             }
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
                this.plans = plansDone
                if(plansDone.isNotEmpty()) {
                    binding.constraintRecycler.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE
                }
                else {
                    binding.recyclerPlansDone.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                plansDoneAdapter.submitList(plansDone)
            }
        })
    }
}