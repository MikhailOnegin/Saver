package digital.fact.saver.presentation.fragments.savers.saver

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
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.databinding.FragmentSaverBinding
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.presentation.fragments.savers.newSaver.NewSaverFragment
import digital.fact.saver.utils.createSnackBar
import digital.fact.saver.utils.formatToMoney
import digital.fact.saver.utils.getLongSumFromString
import digital.fact.saver.utils.getSumStringFromLong
import java.text.SimpleDateFormat
import java.util.*

class SaverFragment : Fragment() {

    private lateinit var binding: FragmentSaverBinding
    private lateinit var saverVM: SaverViewModel

    private val fullDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        saverVM = ViewModelProvider(this)[SaverViewModel::class.java]
        val saverId = arguments?.getLong(EXTRA_SAVER_ID) ?: 0L
        if (saverId == 0L) {
            createSnackBar(
                binding.root,
                getString(R.string.snackbarCantFindSaver)
            ).show()
            findNavController().popBackStack()
        } else saverVM.initialize(saverId)
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        saverVM.run {
            saver.observe(viewLifecycleOwner) { onSaverChanged(it) }
            aimDate.observe(viewLifecycleOwner) { onAimDateChanged(it) }
            dailyFee.observe(viewLifecycleOwner) {
                NewSaverFragment.onDailyFeeChanged(
                    it,
                    binding.dailyFeeHint
                )
            }
        }
    }

    private fun onAimDateChanged(date: Date) {
        binding.aimDate.text = shortDateFormat.format(date)
    }

    private fun onSaverChanged(saver: Sources) {
        val subtitle = "${getString(R.string.saverToolbarSubtitle)} " +
                fullDateFormat.format(Date(saver.addingDate))
        binding.run {
            toolbar.subtitle = subtitle
            name.setText(saver.name)
            currentSum.text = saver.currentSum.formatToMoney(true)
            aimSum.setText(getSumStringFromLong(saver.aimSum))
            setVisibility(saver.visibility)
        }
    }

    private fun setVisibility(visibility: Int) {
        when (visibility) {
            Source.Visibility.VISIBLE.value -> binding.visibility.isChecked = true
            Source.Visibility.INVISIBLE.value -> binding.visibility.isChecked = false
        }
    }

    private fun setListeners() {
        binding.aimDate.setOnClickListener { showAimDatePicker() }
        binding.visibility.setOnCheckedChangeListener(onCheckChangeListener)
        binding.aimSum.doOnTextChanged { text, _, _, _ ->
            saverVM.setAimSum(getLongSumFromString(text.toString()))
        }
    }

    private val onCheckChangeListener = { _: View, isChecked: Boolean ->
        saverVM.setVisibility(
            if (isChecked) Source.Visibility.VISIBLE.value else Source.Visibility.INVISIBLE.value
        )
    }

    private fun showAimDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(saverVM.aimDate.value?.time ?: Date().time)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            saverVM.setAimDate(Date(it))
        }
        picker.show(childFragmentManager, "creation_date_picker")
    }

    companion object {

        const val EXTRA_SAVER_ID = "extra_saver_id"

    }

}