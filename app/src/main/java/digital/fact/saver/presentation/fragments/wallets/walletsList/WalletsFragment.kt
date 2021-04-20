package digital.fact.saver.presentation.fragments.wallets.walletsList

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.databinding.FragmentWalletsBinding
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.recycler.SourcesAdapter

class WalletsFragment : Fragment() {

    private lateinit var binding: FragmentWalletsBinding
    private lateinit var walletsVM: WalletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsBinding.inflate(inflater, container, false)
        setEmptyView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = WalletsViewModel.WalletsVMFactory(mainVM)
        walletsVM = ViewModelProvider(requireActivity(), factory)[WalletsViewModel::class.java]
        setRecyclerView()
        setObservers()
    }

    private fun setObservers() {
        when (arguments?.getInt(EXTRA_SOURCE_TYPE)) {
            DbSource.Type.ACTIVE.value ->
                walletsVM.activeWallets.observe(viewLifecycleOwner) { onWalletsChanged(it) }
            DbSource.Type.INACTIVE.value ->
                walletsVM.inactiveWallets.observe(viewLifecycleOwner) { onWalletsChanged(it) }
        }
    }

    private fun onWalletsChanged(wallets: List<SourceItem>?) {
        if (wallets?.isNullOrEmpty() == true) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.root.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.emptyView.root.visibility = View.GONE
        }
        (binding.recyclerView.adapter as SourcesAdapter).submitList(wallets)
    }

    private fun setEmptyView() {
        binding.emptyView.apply {
            image.setImageResource(R.drawable.image_wallet_empty_view)
            title.setText(R.string.hint_empty_wallet_title)
            hint.setText(R.string.hint_empty_wallet_description)
        }
    }

    private fun setRecyclerView() {
        val adapter = SourcesAdapter(walletsVM, onWalletClicked)
        binding.recyclerView.adapter = adapter
    }

    private val onWalletClicked = { _: Long ->
        //sergeev: Переход в деталку кошелька.
    }

    companion object {

        const val EXTRA_SOURCE_TYPE = "extra_source_type"

    }

}