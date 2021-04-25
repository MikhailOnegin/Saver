package digital.fact.saver.presentation.fragments.wallets.newWallet

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.databinding.FragmentWalletNewBinding
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.SumInputFilter
import digital.fact.saver.utils.getLongSumFromString
import java.text.SimpleDateFormat
import java.util.*

class NewWalletFragment : Fragment() {

    private lateinit var binding: FragmentWalletNewBinding
    private lateinit var newWalletVM: NewWalletViewModel

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletNewBinding.inflate(inflater, container, false)
        binding.startSum.filters = arrayOf(SumInputFilter())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = NewWalletViewModel.NewWalletVMFactory(mainVM)
        newWalletVM = ViewModelProvider(this, factory)[NewWalletViewModel::class.java]
        setRadioButtons()
        setObservers()
        setListeners()
    }

    private fun setRadioButtons() {
        when (newWalletVM.type) {
            DbSource.Type.ACTIVE.value -> binding.active.isChecked = true
            DbSource.Type.INACTIVE.value -> binding.inactive.isChecked = true
        }
    }

    private fun setObservers() {
        newWalletVM.walletCreatedEvent.observe(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
        newWalletVM.creationDate.observe(viewLifecycleOwner) { onCreationDateChanged(it) }
    }

    private fun setListeners() {
        binding.run {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            type.setOnCheckedChangeListener { _, checkedId ->
                when (checkedId) {
                    R.id.active -> {
                        binding.hintType.setText(R.string.hintActiveWallet)
                        newWalletVM.setType(DbSource.Type.ACTIVE.value)
                    }
                    R.id.inactive -> {
                        binding.hintType.setText(R.string.hintInactiveWallet)
                        newWalletVM.setType(DbSource.Type.INACTIVE.value)
                    }
                }
            }
            creationDate.setOnClickListener { showDatePicker() }
            name.doOnTextChanged { text, _, _, _ ->
                createWallet.isEnabled = !text.isNullOrEmpty()
            }
            startSum.doOnTextChanged { text, _, _, _ ->
                if (text.isNullOrEmpty()) newWalletVM.setStartSum(0L)
                else newWalletVM.setStartSum(getLongSumFromString(text.toString()))
            }
            createWallet.setOnClickListener {
                newWalletVM.createWallet(binding.name.text.toString())
            }
        }
    }

    private fun onCreationDateChanged(date: Date?) {
        date?.run {
            binding.creationDate.text = dateFormat.format(this)
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(newWalletVM.creationDate.value?.time ?: Date().time)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            newWalletVM.setCreationDate(Date(it))
        }
        picker.show(childFragmentManager, "creation_date_picker")
    }

}