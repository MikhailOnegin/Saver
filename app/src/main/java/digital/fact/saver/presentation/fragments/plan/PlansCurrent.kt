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
import digital.fact.saver.databinding.FragmentPlansCurrentBinding
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.dialogs.RefactorPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator
import digital.fact.saver.utils.round
import java.util.*

class PlansCurrent : Fragment() {

    private lateinit var binding: FragmentPlansCurrentBinding
    private lateinit var adapterPlansCurrent: PlansCurrentAdapter
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
        plansVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
            .get(PlansViewModel::class.java)
        initializedAdapters()
        binding.recyclerPlansCurrent.adapter = adapterPlansCurrent
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
                if(plans.isNotEmpty()) {
                    binding.recyclerPlansCurrent.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE
                }
                else {
                    binding.recyclerPlansCurrent.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansCurrent = plans.filter { it.operation_id == 0 && it.planning_date > unixFrom && it.planning_date < unixTo }
                if(plansCurrent.isNotEmpty()) {
                    binding.recyclerPlansCurrent.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE
                }
                else {
                    binding.recyclerPlansCurrent.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                adapterPlansCurrent.submitList(plansCurrent)
            }
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