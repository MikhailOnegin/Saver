package digital.fact.saver.presentation.fragments.wallet

import android.os.Bundle
import android.support.v4.os.IResultReceiver
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletAddBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.viewmodels.WalletsViewModel
import java.text.SimpleDateFormat
import java.util.*

class WalletAddFragment : Fragment() {

    private lateinit var binding: FragmentWalletAddBinding
    private lateinit var walletsVM: WalletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        walletsVM = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        setListeners()
    }

    private fun setListeners() {
        binding.addWallet.setOnClickListener { addWallet() }
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

    private fun addWallet() {
        var category = Source.SourceCategory.WALLET_ACTIVE
        if (binding.inactive.isChecked) {
            category = Source.SourceCategory.WALLET_INACTIVE
        }
//        val unixtime = getUnixDate()
        walletsVM.insertSource(
            Source(
                name = binding.walletName.text.toString(),
                category = category,
                start_sum = binding.startMoney.text.toString().toInt(),
                adding_date = binding.walletCreateDate.text.toString().toInt(), //yunusov: поменять на unixtime
                order_number = 0,
                visibility = Source.SourceVisibility.VISIBLE
            )
        )
        findNavController().popBackStack()
    }

    private fun getUnixDate(): Long {
        val sdf = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
        val date = sdf.parse(binding.walletCreateDate.text.toString())
        return date.time
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> findNavController().popBackStack()
        }
        true
    }
}