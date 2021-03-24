package digital.fact.saver.presentation.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentHistoryBinding
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.fragments.operation.NewOperationFragment
import digital.fact.saver.utils.WordEnding
import digital.fact.saver.utils.getFormattedDateForHistory
import digital.fact.saver.utils.getWordEndingType
import digital.fact.saver.utils.showNotReadyToast
import eightbitlab.com.blurview.RenderScriptBlur
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var mainVM: MainViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupBlurView()
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setObservers()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        binding.datePicker.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener { showNotReadyToast(requireContext()) }
        binding.weekCalendar.setOnDateChangedListener { mainVM.setCurrentDate(it.time) }
        binding.add.setOnClickListener { navigateToAddOperation() }
        binding.toolbar.setOnMenuItemClickListener (onMenuItemClickListener)
    }

    private val onMenuItemClickListener: (MenuItem)->Boolean = {
        if (it.itemId == R.id.about) {
            findNavController().navigate(R.id.action_historyFragment_to_aboutFragment)
        }
        true
    }

    private fun setObservers() {
        mainVM.currentDate.observe(viewLifecycleOwner) { onCurrentDateChanged(it) }
        mainVM.periodDaysLeft.observe(viewLifecycleOwner) { onPeriodDaysLeftChanged(it) }
    }

    private fun navigateToAddOperation() {
        val bundle = Bundle()
        val date = mainVM.currentDate.value ?: Date()
        bundle.putLong(NewOperationFragment.EXTRA_OPERATION_DATE, date.time)
        findNavController().navigate(R.id.action_historyFragment_to_newOperationFragment, bundle)
    }

    private fun setupRecyclerView() {
        binding.viewPager.adapter = ViewPagerAdapter(this)
        binding.blurView.doOnLayout {
            mainVM.setHistoryBlurViewWidth(it.height)
        }
    }

    private fun setupBlurView() {
        val radius = 10f
        binding.blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(requireActivity()))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(mainVM.currentDate.value?.time)
        val fragment = builder.build()
        fragment.addOnPositiveButtonClickListener { mainVM.setCurrentDate(Date(it)) }
        fragment.show(childFragmentManager, "date_picker")
    }

    private fun onCurrentDateChanged(newDate: Date) {
        binding.title.text = getFormattedDateForHistory(newDate)
        binding.weekCalendar.setCurrentDate(newDate)
    }

    private fun onPeriodDaysLeftChanged(daysLeft: Int) {
        if (daysLeft == 0) {
            binding.subtitle.visibility = View.GONE
        } else {
            binding.subtitle.text = getElapsedTimeString(daysLeft)
            binding.subtitle.visibility = View.VISIBLE
        }
    }

    private fun getElapsedTimeString(daysLeft: Int): String {
        val builder = StringBuilder(getString(R.string.periodDaysLeft))
        builder.append(" $daysLeft ")
        builder.append(when(getWordEndingType(daysLeft)) {
            WordEnding.TYPE_1 -> getString(R.string.days1)
            WordEnding.TYPE_2 -> getString(R.string.days2)
            WordEnding.TYPE_3 -> getString(R.string.days3)
        })
        return builder.toString()
    }

    class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 1

        override fun createFragment(position: Int): Fragment {
            return OperationsFragment()
        }

    }

}