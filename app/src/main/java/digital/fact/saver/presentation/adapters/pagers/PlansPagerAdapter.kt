package digital.fact.saver.presentation.adapters.pagers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import digital.fact.saver.presentation.fragments.plan.PlansCurrentFragment
import digital.fact.saver.presentation.fragments.plan.PlansDoneFragment
import digital.fact.saver.presentation.fragments.plan.PlansOutsideFragment

class PlansPagerAdapter(
    fm: FragmentManager, lifecycle: Lifecycle,
    private val fragments: List<Fragment> = listOf(PlansCurrentFragment(), PlansDoneFragment(), PlansOutsideFragment())
) : FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}