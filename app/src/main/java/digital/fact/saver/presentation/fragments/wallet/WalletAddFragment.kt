package digital.fact.saver.presentation.fragments.wallet

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletAddBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.SumInputFilter
import digital.fact.saver.utils.resetTimeInMillis
import digital.fact.saver.utils.toLongFormatter
import java.text.SimpleDateFormat
import java.util.*

class WalletAddFragment : Fragment() {

    private lateinit var binding: FragmentWalletAddBinding
    private lateinit var sourcesVM: SourcesViewModel
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
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        binding.startMoney.filters = arrayOf(SumInputFilter())
        setDefaultDateToButton()
        setListeners()
    }

    override fun onStop() {
        super.onStop()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDefaultDateToButton() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        binding.walletCreateDate.text = sdf.format(Date()).toString()
    }

    private fun setListeners() {
        binding.createWallet.setOnClickListener { addWallet() }
        binding.type.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.active -> binding.hintType.setText(R.string.hintActiveWallet)
                R.id.inactive -> binding.hintType.setText(R.string.hintInactiveWallet)
            }
        }
        binding.walletCreateDate.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.walletName.doOnTextChanged { text, _, _, _ ->
            binding.createWallet.isEnabled = !text.isNullOrEmpty() && text.length > 2
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(Date().time)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            binding.walletCreateDate.text = sdf.format(it).toString()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun addWallet() {
        val category = when (binding.type.checkedRadioButtonId) {
            R.id.active -> Source.Type.ACTIVE.value
            else -> Source.Type.INACTIVE.value
        }
        sourcesVM.insertSource(
            Source(
                name = binding.walletName.text.toString(),
                type = category,
                start_sum = binding.startMoney.text.toString().toLongFormatter(),
                adding_date = resetTimeInMillis(
                    sdf.parse(binding.walletCreateDate.text.toString())?.time ?: 0L
                ),
                sort_order = 0,
                visibility = Source.Visibility.VISIBLE.value
            )
        )
        findNavController().popBackStack()
    }
}