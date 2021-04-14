package digital.fact.saver.presentation.fragments.wallets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.formatToMoney
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
        setObservers()
    }

    private fun setObservers() {
        sourcesVM.deleteSourceEvent.observe(
                viewLifecycleOwner,
                OneTimeEvent.Observer { findNavController().popBackStack() }
        )
    }

    override fun onStop() {
        super.onStop()
        val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun setListeners() {
        binding.saveChanges.setOnClickListener { updateSource() }
        binding.type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.active -> binding.hint.setText(R.string.hintActiveWallet)
                R.id.inactive -> binding.hint.setText(R.string.hintInactiveWallet)
            }
        }
        binding.toolbar.apply {
            setNavigationOnClickListener { findNavController().popBackStack() }
            setOnMenuItemClickListener(onMenuItemClicked)
        }
        binding.walletName.doOnTextChanged { text, _, _, _ ->
            binding.saveChanges.isEnabled = !text.isNullOrEmpty() && text.length > 2
        }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.delete -> SlideToPerformDialog(
                    title = getString(R.string.deleteWallet),
                    message = getString(R.string.confirmDeleteDescription),
                    warning = getString(R.string.confirmDeleteWarning),
                    onSliderFinishedListener = {
                        sourcesVM.deleteSource(
                                Source(
                                        _id = wallet.id,
                                        name = wallet.name,
                                        type = wallet.type,
                                        start_sum = wallet.startSum,
                                        adding_date = wallet.addingDate,
                                        sort_order = wallet.sortOrder,
                                        visibility = wallet.visibility,
                                        aim_sum = wallet.aimSum,
                                        aim_date = wallet.aimDate
                                )
                        )
                        sourcesVM.deleteSourceEvent.value = OneTimeEvent()
                    },
            ).show(childFragmentManager, "confirm-delete-dialog")
        }
        true
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
                        visibility = checkVisibility(),
                        aim_date = wallet.aimDate,
                        aim_sum = wallet.aimSum
                )
        )
        findNavController().popBackStack()
    }

    private fun checkVisibility(): Int {
        return when (binding.visibility.isChecked) {
            true -> Source.Visibility.VISIBLE.value
            false -> Source.Visibility.INVISIBLE.value
        }
    }

    private fun checkCategory(): Int {
        return when (binding.type.checkedRadioButtonId) {
            R.id.active -> Source.Type.ACTIVE.value
            R.id.inactive -> Source.Type.INACTIVE.value
            else -> Source.Type.ACTIVE.value
        }
    }

    private fun getWalletData() {
        val id = arguments?.getLong(WalletsHostFragment.WALLET_ID) ?: 0L
        val wallets = if (arguments?.getBoolean(WalletsHostFragment.IS_ACTIVE) == true) {
            sourcesVM.sources.value?.toActiveSources(
                    operations = operationsVM.operations.value?.toOperations(),
                    isHidedForShow = true
            ) ?: listOf()
        } else {
            sourcesVM.sources.value?.toInactiveSources(
                    operations = operationsVM.operations.value?.toOperations(),
                    isHidedForShow = true
            ) ?: listOf()
        }
        wallet = wallets.first { it.itemId == id && it is Sources } as Sources
        setData()
    }

    private fun setData() {
        binding.visibility.isChecked = wallet.visibility == Source.Visibility.VISIBLE.value
        binding.balance.text = getCurrentSum()
        binding.walletName.setText(wallet.name)
        binding.startSum.text = wallet.startSum.formatToMoney()
        wallet.type.let {
            if (it == Source.Type.ACTIVE.value) {
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
        }.formatToMoney()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setCreationDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = wallet.addingDate
        binding.createDate.text = sdf.format(calendar.time)
    }

}