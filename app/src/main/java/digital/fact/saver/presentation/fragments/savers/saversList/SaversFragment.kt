package digital.fact.saver.presentation.fragments.savers.saversList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentSaversBinding
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.recycler.SourcesAdapter
import digital.fact.saver.presentation.fragments.savers.saver.SaverFragment

class SaversFragment : Fragment() {

    private lateinit var binding: FragmentSaversBinding
    private lateinit var sourcesVM: SourcesViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaversBinding.inflate(inflater, container, false)
        setEmptyView()
        setRecyclerView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = SourcesViewModel.SourcesVMFactory(mainVM)
        sourcesVM = ViewModelProvider(requireActivity(), factory)[SourcesViewModel::class.java]
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        sourcesVM.savers.observe(viewLifecycleOwner, { onSaversChanged(it) })
    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.add -> findNavController().navigate(R.id.action_banksFragment_to_bankAddFragment)
        }
        true
    }

    private fun onSaversChanged(savers: List<SourceItem>?) {
        if (savers?.isNullOrEmpty() == true) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.root.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.root.visibility = View.GONE
        }
        (binding.recyclerView.adapter as SourcesAdapter).submitList(savers)
    }

    private fun setEmptyView() {
        binding.emptyView.apply {
            image.setImageResource(R.drawable.image_saver_empty_view)
            title.setText(R.string.bankEmptyTitle)
            hint.setText(R.string.bankEmptyDescription)
        }
    }

    private fun setRecyclerView() {
        /*val adapter = SourcesAdapter(onWalletClick = onActionClicked)
        binding.recyclerView.adapter = adapter*/
    }

    private val onSaverClicked = { id: Long ->
        val bundle = Bundle()
        bundle.putLong(SaverFragment.EXTRA_SAVER_ID, id)
        findNavController().navigate(R.id.action_banksFragment_to_bankFragment, bundle)
    }

}