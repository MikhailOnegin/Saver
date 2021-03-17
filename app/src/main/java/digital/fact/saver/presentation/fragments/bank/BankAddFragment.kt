package digital.fact.saver.presentation.fragments.bank

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentBankAddBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import java.text.SimpleDateFormat
import java.util.*

class BankAddFragment : Fragment() {

    private lateinit var binding: FragmentBankAddBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var mainVM: MainViewModel
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBankAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setDefaultDateToButton()
        setListeners()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDefaultDateToButton() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        binding.walletCreateDate.text = sdf.format(mainVM.currentDate.value?.time).toString()
    }

    private fun setListeners() {
        binding.walletCreateDate.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(mainVM.currentDate.value?.time)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            binding.walletCreateDate.text = sdf.format(it).toString()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> addSaver()
        }
        true
    }

    private fun addSaver() {
        sourcesVM.insertSource(
            Source(
                name = binding.walletName.text.toString(),
                type = Source.SourceCategory.SAVER.value,
                start_sum = 0L,
                adding_date = sdf.parse(binding.walletCreateDate.text.toString())?.time ?: 0L,
                sort_order = 0,
                visibility = Source.SourceVisibility.VISIBLE.value
            )
        )
        findNavController().popBackStack()
    }

}