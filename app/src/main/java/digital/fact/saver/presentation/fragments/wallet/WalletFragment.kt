package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletBinding
import digital.fact.saver.databinding.RvWalletInactiveCountBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.viewmodels.WalletsViewModel

class WalletFragment : Fragment() {
    private lateinit var binding: FragmentWalletBinding
    private lateinit var walletsVM: WalletsViewModel
    private lateinit var wallet: Source

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        walletsVM = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        getWalletData()
    }

    private fun getWalletData() {
        val id = arguments?.getInt(WalletsFragment.WALLET_ID) ?: 0
//        wallet = walletsVM.getAllSources().value?.first { it._id == id }

    }
}