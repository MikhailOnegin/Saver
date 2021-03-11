package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletAddBinding
import digital.fact.saver.presentation.dialogs.DatePickerDialog

class WalletAddFragment : Fragment() {

    private lateinit var binding: FragmentWalletAddBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        binding.walletCreateDate.setOnClickListener {
            DatePickerDialog(binding.walletCreateDate).show(
                childFragmentManager,
                "date_picker"
            )
        }
        binding.active.setOnCheckedChangeListener { _, isActive ->
            if (isActive) {
                binding.hint.setText(R.string.hintActiveWallet)
                binding.inactive.isChecked = false
            }
        }
        binding.inactive.setOnCheckedChangeListener { _, isActive ->
            if (isActive) {
                binding.hint.setText(R.string.hintInactiveWallet)
                binding.active.isChecked = false
            }
        }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> findNavController().popBackStack()
        }
        true
    }
}