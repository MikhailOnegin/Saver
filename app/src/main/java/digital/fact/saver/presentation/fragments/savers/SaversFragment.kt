package digital.fact.saver.presentation.fragments.savers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.databinding.FragmentSaversBinding
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSavers
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.adapters.recycler.SourcesAdapter
import digital.fact.saver.presentation.fragments.savers.saver.SaverFragment
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class SaversFragment : Fragment() {

    private lateinit var binding: FragmentSaversBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        sourcesVM.getAllSources()
        setListeners()
        setObservers()
    }

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
        binding.toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).openDrawer() }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.add -> findNavController().navigate(R.id.action_banksFragment_to_bankAddFragment)
        }
        true
    }

    private fun setObservers() {
        sourcesVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(listUnsorted: List<Source>) {
        val list = listUnsorted.toSavers(operationsVM.operations.value?.toOperations())
        if (list.isNullOrEmpty()) {
            binding.list.visibility = View.GONE
            setEmptyMessage()
        } else {
            setEmptyMessage(true)
            binding.list.visibility = View.VISIBLE
        }
        val onActionClicked = { id: Long ->
            val bundle = Bundle()
            bundle.putLong(SaverFragment.EXTRA_SAVER_ID, id)
            findNavController().navigate(R.id.action_banksFragment_to_bankFragment, bundle)
        }
        val adapter = SourcesAdapter(onWalletClick = onActionClicked, sourcesVM, operationsVM)
        binding.list.adapter = adapter
        adapter.submitList(list)
    }

    private fun setEmptyMessage(hideAll: Boolean = false) {
        if (hideAll) {
            binding.empty.root.visibility = View.GONE
            return
        }
        binding.empty.apply {
            root.visibility = View.VISIBLE
            image.setImageResource(R.drawable.ic_empty_bank)
            title.setText(R.string.bankEmptyTitle)
            hint.setText(R.string.bankEmptyDescription)
        }
    }

}