package digital.fact.saver.presentation.fragments.bank

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import digital.fact.saver.databinding.FragmentBanksBinding
import digital.fact.saver.databinding.FragmentWalletsBinding

class BanksFragment : Fragment() {

    private lateinit var binding: FragmentBanksBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentBanksBinding.inflate(inflater, container, false)
        return binding.root
    }

}