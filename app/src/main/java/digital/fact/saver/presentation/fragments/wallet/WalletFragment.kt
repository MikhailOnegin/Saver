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
import digital.fact.saver.models.*
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import java.text.SimpleDateFormat
import java.util.*

class WalletFragment : Fragment() {
    private lateinit var binding: FragmentWalletBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var wallet: Sources

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
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
        sourcesVM.deleteSource(
            Source(
                _id = wallet.id,
                name = binding.walletName.text.toString(),
                type = checkCategory(),
                start_sum = wallet.startSum,
                adding_date = wallet.addingDate,
                sort_order = wallet.sortOrder,
                visibility = wallet.visibility,
            )
        )
        findNavController().popBackStack()
    }

    private fun updateSource() {
        sourcesVM.updateSource(
            Source(
                _id = wallet.id,
                name = binding.walletName.text.toString(),
                type = checkCategory(),
                start_sum = wallet.startSum,
                adding_date = wallet.addingDate,
                sort_order = wallet.sortOrder,
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
        val id = arguments?.getLong(WalletsFragment.WALLET_ID) ?: 0L
        val wallets = sourcesVM.getAllSources().value?.toSources(
            operations = operationsVM.operations.value?.toOperations(),
            isShowed = true
        )
        wallets?.let { list ->
            wallet =
                list.first { it.itemId == id && it is Sources } as Sources
            setData()
        }
    }

    private fun setData() {
        binding.visibility.isChecked = wallet.visibility == Source.SourceVisibility.INVISIBLE.value
        binding.balance.text = getCurrentSum()
        binding.walletName.setText(wallet.name)
        binding.startSum.text = wallet.startSum.toString()
        wallet.type.let {
            if (it == Source.SourceCategory.WALLET_ACTIVE.value) {
                binding.active.isChecked = true
            } else {
                binding.inactive.isChecked = true
            }
        }
        setCreationDate()
    }

    private fun getCurrentSum(): CharSequence {
        return if (wallet.currentSum == 0L && wallet.startSum != 0L) {
            wallet.startSum
        } else {
            wallet.currentSum
        }.toString()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setCreationDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wallet.addingDate
        binding.createDate.text = sdf.format(calendar.time)
    }
}