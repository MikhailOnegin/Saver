package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansOutsideBinding
import digital.fact.saver.presentation.adapters.recycler.PlansOutsideAdapter
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.addCustomItemDecorator

class PlansOutside: Fragment() {

    private lateinit var plansVM: PlansViewModel
    private lateinit var plansAdapter: PlansOutsideAdapter
    private lateinit var binding: FragmentPlansOutsideBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansOutsideBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(requireActivity(), ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(PlansViewModel::class.java)
        initializedAdapters()
        binding.recyclerPlansOutside.addCustomItemDecorator((resources.getDimension(R.dimen._32dp).toInt()))
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
        plansAdapter = PlansOutsideAdapter()
        binding.recyclerPlansOutside.adapter = plansAdapter
    }

    private fun setObservers(owner: LifecycleOwner) {
        plansVM.getAllPlans().observe(owner, {
            plansVM.period.value?.let {period ->
                val unixFrom = period.dateFrom.time.time
                val unixTo = period.dateTo.time.time
                val plansOutside = it.filter {
                    it.planning_date < unixFrom || it.planning_date > unixTo  }
                if(plansOutside.isNotEmpty()) {
                    binding.recyclerPlansOutside.visibility = View.VISIBLE
                    binding.includeEmptyData.root.visibility = View.GONE
                }
                else {
                    binding.recyclerPlansOutside.visibility = View.GONE
                    binding.includeEmptyData.root.visibility = View.VISIBLE
                }
                plansAdapter.submitList(plansOutside)
            }
        })
    }
}