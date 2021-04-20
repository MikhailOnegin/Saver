package digital.fact.saver.presentation.fragments.savers.saver

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.databinding.FragmentSaverBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.dialogs.ConfirmationDialog
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.fragments.savers.newSaver.NewSaverFragment
import digital.fact.saver.utils.*
import java.text.SimpleDateFormat
import java.util.*

class SaverFragment : Fragment() {

    private lateinit var binding: FragmentSaverBinding
    private lateinit var saverVM: SaverViewModel

    private val fullDateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private var previousPositionHolder = NewSaverFragment.PreviousPositionHolder(0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaverBinding.inflate(inflater, container, false)
        binding.aimSum.filters = arrayOf(SumInputFilter())
        setupViewPager()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = SaverViewModel.SaverVMFactory(mainVM)
        saverVM = ViewModelProvider(this, factory)[SaverViewModel::class.java]
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

    override fun onStart() {
        super.onStart()
        requireActivity().run {
            onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                if (saverVM.hasChanges.value == true) {
                    showSaveChangesDialog()
                    return@addCallback
                } else {
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = NewSaverFragment.SaverHintsViewPagerAdapter(this)
        binding.viewPagerLayout.viewPager.adapter = pagerAdapter
        binding.viewPagerLayout.viewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                NewSaverFragment.animateSelection(
                    position,
                    binding.viewPagerLayout.customTabLayout,
                    previousPositionHolder
                )
            }
        })
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
            hasChanges.observe(viewLifecycleOwner) { binding.save.isEnabled = it }
            exitEvent.observe(viewLifecycleOwner) { findNavController().popBackStack() }
            noNameEvent.observe(viewLifecycleOwner) { onNoNameEvent() }
        }
    }

    private fun onNoNameEvent() {
        createSnackBar(
            anchorView = binding.root,
            text = getString(R.string.snackbarNoSaverName)
        ).show()
    }

    private fun onAimDateChanged(date: Date) {
        binding.aimDate.text = shortDateFormat.format(date)
    }

    private fun onSaverChanged(saver: Source) {

        binding.run {
            val title = "${getString(R.string.saverToolbarTitle)} " +
                    "(${saver.currentSum.formatToMoney(true)})"
            toolbar.title = title
            val subtitle = "${getString(R.string.saverToolbarSubtitle)} " +
                    fullDateFormat.format(Date(saver.addingDate))
            toolbar.subtitle = subtitle
            name.setText(saver.name)
            aimSum.setText(getSumStringFromLong(saver.aimSum))
            setVisibility(saver.visibility)
        }
    }

    private fun setVisibility(visibility: Int) {
        when (visibility) {
            DbSource.Visibility.VISIBLE.value -> binding.visibility.isChecked = true
            DbSource.Visibility.INVISIBLE.value -> binding.visibility.isChecked = false
        }
    }

    private fun setListeners() {
        binding.aimDate.setOnClickListener { showAimDatePicker() }
        binding.visibility.setOnCheckedChangeListener(onCheckChangeListener)
        binding.aimSum.doOnTextChanged { text, _, _, _ ->
            saverVM.setAimSum(getLongSumFromString(text.toString()))
            saverVM.updateHasChanges()
        }
        binding.name.doOnTextChanged { text, _, _, _ ->
            saverVM.setName(text.toString())
            saverVM.updateHasChanges()
        }
        binding.toolbar.setNavigationOnClickListener { onNavigationItemClicked() }
        binding.save.setOnClickListener { saverVM.saveChanges() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.delete -> showDeleteDialog()
        }
        true
    }

    private fun showDeleteDialog() {
        SlideToPerformDialog(
            title = getString(R.string.dialogDeleteSaverTitle),
            message = getString(R.string.dialogDeleteSaverMessage),
            onSliderFinishedListener = { saverVM.deleteSaver() }
        ).show(childFragmentManager, "delete_dialog")
    }

    private fun onNavigationItemClicked() {
        if (saverVM.hasChanges.value == true) showSaveChangesDialog()
        else findNavController().popBackStack()
    }

    private fun showSaveChangesDialog() {
        ConfirmationDialog(
            title = getString(R.string.dialogSaveChangesTitle),
            message = getString(R.string.dialogSaveChangesMessage),
            positiveButtonText = getString(R.string.buttonSave),
            negativeButtonText = getString(R.string.buttonNo),
            onNegativeButtonClicked = { findNavController().popBackStack() },
            onPositiveButtonClicked = { saverVM.saveChanges() }
        ).show(childFragmentManager, "save_changes_dialog")
    }

    private val onCheckChangeListener = { _: View, isChecked: Boolean ->
        saverVM.setVisibility(
            if (isChecked) DbSource.Visibility.VISIBLE.value else DbSource.Visibility.INVISIBLE.value
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