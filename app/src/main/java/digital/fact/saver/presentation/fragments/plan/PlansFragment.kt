package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.databinding.FragmentPlansBinding
import digital.fact.saver.presentation.adapters.PlansPagerAdapter

class PlansFragment : Fragment() {

    private lateinit var binding: FragmentPlansBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.viewPager2.adapter = PlansPagerAdapter(activity?.supportFragmentManager!!, this.lifecycle)
       TabLayoutMediator(binding.tableLayout, binding.viewPager2){tab, position ->
           when(position){
               0 -> tab.text = "Текущие"
               1 -> tab.text = "Выполненные"
               2 -> tab.text = "Вне периода"
           }
        }.attach()

    }
}