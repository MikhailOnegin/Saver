package digital.fact.saver.presentation.adapters.pagers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import digital.fact.saver.presentation.fragments.plan.PlansFragment

class PlansPagerAdapter(
    fm: FragmentManager, lifecycle: Lifecycle,
    private val fragments: List<Fragment> = listOf(PlansFragment(FragmentPlanType.FRAGMENT_CURRENT),
        PlansFragment(FragmentPlanType.FRAGMENT_DONE), PlansFragment(FragmentPlanType.FRAGMENT_OUTSIDE))
) : FragmentStateAdapter(fm, lifecycle) {

    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

    enum class FragmentPlanType{
        FRAGMENT_CURRENT,
        FRAGMENT_DONE,
        FRAGMENT_OUTSIDE
    }
}