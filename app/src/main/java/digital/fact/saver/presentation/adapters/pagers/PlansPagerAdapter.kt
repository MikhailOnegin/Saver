package digital.fact.saver.presentation.adapters.pagers

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import digital.fact.saver.presentation.fragments.plan.PlansFragment
import digital.fact.saver.presentation.fragments.wallets.walletsList.WalletsFragment
import java.lang.IllegalArgumentException

class PlansPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = Bundle()
        val type = when (position) {
            0 -> PlansFragment.FragmentPlanType.FRAGMENT_CURRENT.value
            1 -> PlansFragment.FragmentPlanType.FRAGMENT_DONE.value
            2 -> PlansFragment.FragmentPlanType.FRAGMENT_OUTSIDE.value
            else -> throw IllegalArgumentException("Illegal position.")
        }
        bundle.putInt(PlansFragment.EXTRA_PLAN_TYPE, type)
        return PlansFragment().apply { arguments = bundle }
    }
}