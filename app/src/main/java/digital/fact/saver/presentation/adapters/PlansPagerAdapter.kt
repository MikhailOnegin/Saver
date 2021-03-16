package digital.fact.saver.presentation.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import digital.fact.saver.presentation.fragments.plan.PlansCurrent
import digital.fact.saver.presentation.fragments.plan.PlansDone
import digital.fact.saver.presentation.fragments.plan.PlansOutside

class PlansPagerAdapter(fm: FragmentManager, lifecycle: Lifecycle,
                        private val  fragments: List<Fragment> = listOf(PlansCurrent(), PlansDone(), PlansOutside())): FragmentStateAdapter(fm, lifecycle){
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}