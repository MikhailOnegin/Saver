package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletAddBinding

class WalletAddFragment : Fragment() {

    private lateinit var binding: FragmentWalletAddBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWalletAddBinding.inflate(inflater, container, false)
        return binding.root
    }
}