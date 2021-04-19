package digital.fact.saver.presentation.fragments.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentAboutAppBinding
import digital.fact.saver.presentation.adapters.pagers.AboutAppPagerAdapter

class AboutAppFragment : Fragment() {

    private lateinit var binding: FragmentAboutAppBinding
    private lateinit var pagerAdapter: AboutAppPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializedAdapters()
        setListeners()
    }

    private fun initializedAdapters() {
        pagerAdapter = AboutAppPagerAdapter(activity?.supportFragmentManager!!, this.lifecycle)
        binding.viewPager2.adapter = pagerAdapter
    }

    private fun setListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        TabLayoutMediator(binding.tableLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.description)
                1 -> tab.text = resources.getString(R.string.historyChanges)
            }
        }.attach()
    }
}