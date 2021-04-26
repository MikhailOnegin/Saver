package digital.fact.saver.presentation.adapters.pagers

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import digital.fact.saver.presentation.fragments.about.AboutAppDescriptionFragment
import digital.fact.saver.presentation.fragments.about.AboutAppHistoryFragment

class AboutAppPagerAdapter (
    fm: FragmentManager, lifecycle: Lifecycle,
    private val fragments: List<Fragment> = listOf(AboutAppDescriptionFragment(), AboutAppHistoryFragment())
) : FragmentStateAdapter(fm, lifecycle) {
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}