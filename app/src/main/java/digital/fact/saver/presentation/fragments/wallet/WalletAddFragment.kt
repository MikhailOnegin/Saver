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
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletAddBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.viewmodels.WalletsViewModel
import java.text.SimpleDateFormat
import java.util.*

class WalletAddFragment : Fragment() {

    private lateinit var binding: FragmentWalletAddBinding
    private lateinit var walletsVM: WalletsViewModel
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
        binding.type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.active -> binding.hint.setText(R.string.hintActiveWallet)
                R.id.inactive -> binding.hint.setText(R.string.hintInactiveWallet)
            }
        }
        binding.walletCreateDate.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            binding.date.setText(sdf.format(it).toString())
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun addWallet() {
        val category = when (binding.type.checkedRadioButtonId) {
            R.id.active -> Source.SourceCategory.WALLET_ACTIVE
            else -> Source.SourceCategory.WALLET_INACTIVE
        }
        walletsVM.insertSource(
            Source(
                name = binding.walletName.text.toString(),
                category = category,
                start_sum = binding.startMoney.text.toString().toInt(),
                adding_date = sdf.parse(binding.date.text.toString())?.time ?: 0L,
                order_number = 0,
                visibility = Source.SourceVisibility.VISIBLE
            )
        )
        findNavController().popBackStack()
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> findNavController().popBackStack()
        }
        true
    }
}