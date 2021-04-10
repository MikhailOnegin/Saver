package digital.fact.saver.presentation.fragments.bank

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
import digital.fact.saver.databinding.FragmentBankBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSavers
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.SumInputFilter
import digital.fact.saver.utils.events.OneTimeEvent
import digital.fact.saver.utils.toLongFormatter
import digital.fact.saver.utils.formatToMoney
import java.text.SimpleDateFormat
import java.util.*

class BankFragment : Fragment() {

    private lateinit var binding: FragmentBankBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var saver: Sources

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBankBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        binding.saverAim.filters = arrayOf(SumInputFilter())
        getSaverData()
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

    private fun getSaverData() {
        val id = arguments?.getLong(BanksFragment.BANK_ID) ?: 0L
        val banks = sourcesVM.sources.value?.toSavers(
            operations = operationsVM.operations.value?.toOperations(),
            isHidedForShow = true
        )
        banks?.let { list ->
            saver =
                list.first { it.itemId == id && it is Sources } as Sources
            setData()
        }
    }

    private fun setData() {
        binding.visibility.isChecked = saver.visibility == Source.Visibility.VISIBLE.value
        binding.balance.text = getCurrentSum()
        binding.walletName.setText(saver.name)
        binding.saverAim.setText(saver.aimSum.formatToMoney(false))
        setCreationDate()
    }

    @SuppressLint("SimpleDateFormat")
    private fun setCreationDate() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = saver.addingDate
        binding.createDate.text = sdf.format(calendar.time)
    }

    private fun getCurrentSum(): CharSequence {
        return if (saver.currentSum == 0L && saver.startSum != 0L) {
            saver.startSum
        } else {
            saver.currentSum
        }.formatToMoney()
    }

    private fun setListeners() {
        binding.toolbar.apply {
            setNavigationOnClickListener { findNavController().popBackStack() }
            setOnMenuItemClickListener(onMenuItemClicked)
        }
        binding.updateSaver.setOnClickListener { updateSource() }
        binding.walletName.doOnTextChanged { text, _, _, _ ->
            binding.updateSaver.isEnabled = !text.isNullOrEmpty() && text.length > 2
        }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.delete -> SlideToPerformDialog(
                title = getString(R.string.deleteWallet),
                description = getString(R.string.deleteSaverDescription),
                onSliderFinishedListener = {
                    sourcesVM.deleteSource(
                        Source(
                            _id = saver.id,
                            name = saver.name,
                            type = saver.type,
                            start_sum = saver.startSum,
                            adding_date = saver.addingDate,
                            sort_order = saver.sortOrder,
                            visibility = saver.visibility,
                        )
                    )
                    sourcesVM.deleteSourceEvent.value = OneTimeEvent()
                }).show(childFragmentManager, "confirm-delete-dialog")
        }
        true
    }

    private fun updateSource() {
        sourcesVM.updateSource(
            Source(
                _id = saver.id,
                name = binding.walletName.text.toString(),
                type = Source.Type.SAVER.value,
                start_sum = saver.startSum,
                adding_date = saver.addingDate,
                sort_order = saver.sortOrder,
                visibility = checkVisibility(),
                aim_sum = binding.saverAim.text.toString().toLongFormatter()
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

}