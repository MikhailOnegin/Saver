package digital.fact.saver.presentation.fragments.history

import android.animation.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import digital.fact.saver.utils.*
import digital.fact.saver.utils.events.EventObserver
import eightbitlab.com.blurview.RenderScriptBlur
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var mainVM: MainViewModel
    private lateinit var historyVM: HistoryViewModel

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
        historyVM = ViewModelProvider(requireActivity())[HistoryViewModel::class.java]
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
        binding.add.setOnClickListener { historyVM.onAddOperationButtonClicked() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
    }

    private val onMenuItemClickListener: (MenuItem) -> Boolean = {
        if (it.itemId == R.id.about) {
            findNavController().navigate(R.id.action_historyFragment_to_aboutFragment)
        }
        true
    }

    private fun setObservers() {
        mainVM.currentDate.observe(viewLifecycleOwner) { onCurrentDateChanged(it) }
        mainVM.periodDaysLeft.observe(viewLifecycleOwner) { onPeriodDaysLeftChanged(it) }
        historyVM.secondLayerEvent.observe(viewLifecycleOwner, EventObserver(onSecondLayerEvent))
    }

    private val onSecondLayerEvent: (Boolean) -> Unit = {
        val layerSet = AnimatorSet()
        layerSet.playTogether(
            getAddButtonOpeningAnimator(it),
            getSecondLayerAnimator(it),
            getStatusBarAnimator(it)
        )

        val buttonsSet = AnimatorSet()
        buttonsSet.playTogether(
            getFabSaverIncomeAnimator(it)
        )

        val resultSet = AnimatorSet()
        resultSet.playSequentially(layerSet, buttonsSet)

        resultSet.start()
    }

    private fun getFabSaverIncomeAnimator(isShowing: Boolean): AnimatorSet {
        val set = AnimatorSet()
        val fabHeight = resources.getDimension(R.dimen.fabSize)
        val miniFabHeight = resources.getDimension(R.dimen.miniFabSize)
        val smallMargin = resources.getDimension(R.dimen.smallMargin)
        val normalMargin = resources.getDimension(R.dimen.normalMargin)
        val translation = fabHeight/2 + normalMargin + 4 * (smallMargin + miniFabHeight)
        val translationAnimator = ValueAnimator.ofFloat(
            if (isShowing) 0f else -translation,
            if (isShowing) -translation else 0f
        )
        translationAnimator.addUpdateListener {
            binding.fabSaverIncome.translationY = it.animatedValue as Float
        }
        translationAnimator.addListener(object: AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) binding.fabSaverIncome.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) binding.fabSaverIncome.visibility = View.GONE
            }

        })

        val scaleAnimator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        scaleAnimator.addUpdateListener {
            binding.fabSaverIncome.scaleX = it.animatedValue as Float
            binding.fabSaverIncome.scaleY = it.animatedValue as Float
        }
        set.playTogether(translationAnimator, scaleAnimator)
        return set
    }

    private fun getAddButtonOpeningAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 45f,
            if (isShowing) 45f else 0f
        )
        animator.addUpdateListener {
            binding.add.rotation = it.animatedValue as Float
        }
        return animator
    }

    private fun getSecondLayerAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        animator.addUpdateListener {
            binding.secondLayerBackground.alpha = it.animatedValue as Float
        }
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) binding.secondLayerBackground.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) binding.secondLayerBackground.visibility = View.GONE
            }

        })
        return animator
    }

    private fun getStatusBarAnimator(isShowing: Boolean): Animator {
        val startColor = ContextCompat.getColor(
            requireActivity(),
            if (isShowing) R.color.colorPrimary else R.color.historySecondLayer
        )
        val finishColor = ContextCompat.getColor(
            requireActivity(),
            if (isShowing) R.color.historySecondLayer else R.color.colorPrimary
        )
        val animator = ValueAnimator.ofArgb(startColor, finishColor)
        animator.addUpdateListener {
            requireActivity().window.statusBarColor = it.animatedValue as Int
        }
        return animator
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
        builder.append(
            when (getWordEndingType(daysLeft)) {
                WordEnding.TYPE_1 -> getString(R.string.days1)
                WordEnding.TYPE_2 -> getString(R.string.days2)
                WordEnding.TYPE_3 -> getString(R.string.days3)
            }
        )
        return builder.toString()
    }

    class ViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 1

        override fun createFragment(position: Int): Fragment {
            return OperationsFragment()
        }

    }

}