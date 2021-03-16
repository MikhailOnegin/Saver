package digital.fact.saver.presentation.fragments.wallet

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.viewmodels.WalletsViewModel
import java.text.SimpleDateFormat
import java.util.*

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
        setListeners()
    }

    private fun setListeners() {
        binding.type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.active -> binding.hint.setText(R.string.hintActiveWalletDetail)
                R.id.inactive -> binding.hint.setText(R.string.hintInactiveWalletDetail)
            }
        }
        binding.toolbar.apply {
            setNavigationOnClickListener { findNavController().popBackStack() }
            setOnMenuItemClickListener(onMenuItemClicked)
        }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> updateSource()
            R.id.delete -> deleteSource()
        }
        true
    }

    private fun deleteSource() {
        walletsVM.deleteSource(
            Source(
                _id = wallet._id,
                name = binding.walletName.text.toString(),
                category = checkCategory(),
                start_sum = wallet.start_sum,
                adding_date = wallet.adding_date,
                order_number = wallet.order_number,
                visibility = checkVisibility(),
            )
        )
        findNavController().popBackStack()
    }

    private fun updateSource() {
        walletsVM.updateSource(
            Source(
                _id = wallet._id,
                name = binding.walletName.text.toString(),
                category = checkCategory(),
                start_sum = wallet.start_sum,
                adding_date = wallet.adding_date,
                order_number = wallet.order_number,
                visibility = checkVisibility()
            )
        )
        findNavController().popBackStack()
    }

    private fun checkVisibility(): Int {
        return when (binding.visibility.isChecked) {
            true -> Source.SourceVisibility.INVISIBLE.value
            false -> Source.SourceVisibility.VISIBLE.value
        }
    }

    private fun checkCategory(): Int {
        return when (binding.type.checkedRadioButtonId) {
            R.id.active -> Source.SourceCategory.WALLET_ACTIVE.value
            R.id.inactive -> Source.SourceCategory.WALLET_INACTIVE.value
            else -> Source.SourceCategory.WALLET_ACTIVE.value
        }
    }

    private fun getWalletData() {
        val id = arguments?.getInt(WalletsFragment.WALLET_ID) ?: 0L
        val wallets = walletsVM.getAllSources()
        wallets.value?.let { list ->
            wallet = list.first { it._id == id }
            setData()
        }
    }

    private fun setData() {
        binding.walletName.setText(wallet.name)
        binding.startSum.text = wallet.start_sum.toString()
        wallet.category.let {
            if (it == Source.SourceCategory.WALLET_ACTIVE.value) {
                binding.active.isChecked = true
            } else {
                binding.inactive.isChecked = true
            }
        }
        setCreationDate()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setCreationDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wallet.adding_date
        binding.createDate.text = sdf.format(calendar.time)
    }
}