package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import digital.fact.saver.databinding.FragmentWalletsBinding

class WalletsFragment : Fragment() {

    private lateinit var binding: FragmentWalletsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

}

