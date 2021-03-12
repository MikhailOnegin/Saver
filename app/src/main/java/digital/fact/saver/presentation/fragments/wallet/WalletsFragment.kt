package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.databinding.FragmentWalletsBinding
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.toSources
import digital.fact.saver.presentation.adapters.recycler.WalletsAdapter
import digital.fact.saver.presentation.viewmodels.WalletsViewModel

class WalletsFragment : Fragment() {

    private lateinit var binding: FragmentWalletsBinding
    private lateinit var walletsVM: WalletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        walletsVM = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        walletsVM.getAllSources()
        setObservers()
    }

    private fun setObservers() {
        walletsVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it.toSources()) })
    }

    private fun onSourcesChanged(list: List<SourceItem>) {
        val onActionClicked = { id: Int ->
//            val bundle = Bundle()
//            bundle.putInt(WALLET_ID, id)
//            findNavController().navigate(
//                R.id.action_walletsFragment_to_detailWalletFragment,
//                bundle
//            )
        }
        val adapter = WalletsAdapter(onWalletClick = onActionClicked)
        binding.list.adapter = adapter
        adapter.submitList(list)
    }

    companion object {
        const val WALLET_ID = "WALLET_ID"
    }

}

