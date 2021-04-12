package digital.fact.saver.presentation.fragments.wallets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletsHostBinding
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class WalletsHostFragment : Fragment() {

    private lateinit var binding: FragmentWalletsHostBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        sourcesVM.getAllSources()
        setViewPager()
        setListeners()
    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
        binding.toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).openDrawer() }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.add -> findNavController().navigate(R.id.action_walletsFragment_to_walletAddFragment)
        }
        true
    }

    private fun setViewPager() {
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = getString(R.string.activeWallest)
                1 -> tab.text = getString(R.string.inactiveWallets)
            }
        }.attach()
    }

    private inner class ScreenSlidePagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2


        override fun createFragment(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putBoolean(IS_ACTIVE, position == 0)
            val fragment = WalletsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    companion object {
        const val WALLET_ID = "WALLET_ID"
        const val IS_ACTIVE = "IS_ACTIVE"
    }

}

