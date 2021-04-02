package digital.fact.saver.presentation.fragments.history

import android.animation.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Operation.*
import digital.fact.saver.databinding.FragmentHistoryBinding
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.fragments.operation.NewOperationFragment
import digital.fact.saver.utils.*
import digital.fact.saver.utils.events.EventObserver
import eightbitlab.com.blurview.RenderScriptBlur
import java.lang.IllegalArgumentException
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var mainVM: MainViewModel
    private lateinit var historyVM: HistoryViewModel
    private var isAnimationRunning = false

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

    override fun onDestroyView() {
        super.onDestroyView()
        historyVM.resetSecondLayerState()
    }

    private fun setListeners() {
        binding.run {
            datePicker.setOnClickListener { showDatePicker() }
            toolbar.setNavigationOnClickListener { showNotReadyToast(requireContext()) }
            weekCalendar.setOnDateChangedListener { mainVM.setCurrentDate(it.time) }
            add.setOnClickListener { onAddButtonClicked() }
            toolbar.setOnMenuItemClickListener(onMenuItemClickListener)
            secondLayerBackground.setOnClickListener { onAddButtonClicked() }
            fabExpenses.setOnClickListener { navigateToAddOperation(it) }
            fabExpensesHint.setOnClickListener { navigateToAddOperation(it) }
            fabIncome.setOnClickListener { navigateToAddOperation(it) }
            fabIncomeHint.setOnClickListener { navigateToAddOperation(it) }
            fabTransfer.setOnClickListener { navigateToAddOperation(it) }
            fabTransferHint.setOnClickListener { navigateToAddOperation(it) }
            fabSaverExpenses.setOnClickListener { navigateToAddOperation(it) }
            fabSaverExpensesHint.setOnClickListener { navigateToAddOperation(it) }
            fabSaverIncome.setOnClickListener { navigateToAddOperation(it) }
            fabSaverIncomeHint.setOnClickListener { navigateToAddOperation(it) }
            calculations.setOnClickListener { }
        }
    }

    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private fun onAddButtonClicked() {
        if (isAnimationRunning) return
        historyVM.onAddOperationButtonClicked()
        if (historyVM.secondLayerEvent.value?.peekContent() == true) {
            requireActivity().run {
                onBackPressedCallback = onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                    if (isAnimationRunning) return@addCallback
                    historyVM.collapseSecondLayout()
                    onBackPressedCallback.remove()
                }
            }
        } else {
            onBackPressedCallback.remove()
        }
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

    private val onSecondLayerEvent: (Boolean) -> Unit = { isShowing ->
        val set = if (isShowing) getSecondLayerShowingAnimatorSet()
        else getSecondLayerHidingAnimatorSet()
        set.start()
    }

    private fun getSecondLayerShowingAnimatorSet(): AnimatorSet {
        val layerSet = AnimatorSet()
        layerSet.playTogether(
            getAddButtonOpeningAnimator(true),
            getSecondLayerAnimator(true),
            getStatusBarAnimator(true)
        )
        val buttonsSet = AnimatorSet()
        buttonsSet.playTogether(
            getMiniFabAnimator(true, binding.fabExpenses, 0L),
            getMiniFabAnimator(true, binding.fabIncome, 60L),
            getMiniFabAnimator(true, binding.fabTransfer, 120L),
            getMiniFabAnimator(true, binding.fabSaverExpenses, 180L),
            getMiniFabAnimator(true, binding.fabSaverIncome, 240L)
        )
        val resultSet = AnimatorSet()
        resultSet.playSequentially(layerSet, buttonsSet, getFabHintsAnimator(true))
        resultSet.addListener(object: AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) { isAnimationRunning = true }

            override fun onAnimationEnd(animation: Animator?) { isAnimationRunning = false }

        })
        return resultSet
    }

    private fun getSecondLayerHidingAnimatorSet(): AnimatorSet {
        val resultSet = AnimatorSet()
        resultSet.playTogether(
            getAddButtonOpeningAnimator(false),
            getSecondLayerAnimator(false),
            getStatusBarAnimator(false),
            getMiniFabAnimator(false, binding.fabExpenses, 0L),
            getMiniFabAnimator(false, binding.fabIncome, 0L),
            getMiniFabAnimator(false, binding.fabTransfer, 0L),
            getMiniFabAnimator(false, binding.fabSaverExpenses, 0L),
            getMiniFabAnimator(false, binding.fabSaverIncome, 0L),
            getFabHintsAnimator(false)
        )
        resultSet.addListener(object: AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) { isAnimationRunning = true }

            override fun onAnimationEnd(animation: Animator?) { isAnimationRunning = false }

        })
        return resultSet
    }

    private fun getMiniFabAnimator(
        isShowing: Boolean,
        fab: FloatingActionButton,
        startDelay: Long
    ): Animator {
        val overScale = 1.4f
        val showingTime = 400L
        val hidingTime = 300L
        val showingValues = floatArrayOf(0f, overScale, 1f)
        val hidingValues = floatArrayOf(1f, 0f)
        val scaleAnimator = if (isShowing) ValueAnimator.ofFloat(*showingValues)
        else ValueAnimator.ofFloat(*hidingValues)
        scaleAnimator.run {
            interpolator = AccelerateInterpolator()
            duration = if (isShowing) showingTime else hidingTime
            setStartDelay(if (isShowing) startDelay else 0L)
        }
        scaleAnimator.addUpdateListener {
            fab.scaleX = it.animatedValue as Float
            fab.scaleY = it.animatedValue as Float
        }
        scaleAnimator.addListener(object: AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) fab.visibility = View.VISIBLE
                binding.run {
                    fabExpensesHint.visibility = View.GONE
                    fabIncomeHint.visibility = View.GONE
                    fabTransferHint.visibility = View.GONE
                    fabSaverExpensesHint.visibility = View.GONE
                    fabSaverIncomeHint.visibility = View.GONE
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) fab.visibility = View.GONE
            }

        })
        return scaleAnimator
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

    private fun getFabHintsAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        animator.addUpdateListener {
            binding.run {
                val alpha = it.animatedValue as Float
                fabExpensesHint.alpha = alpha
                fabIncomeHint.alpha = alpha
                fabTransferHint.alpha = alpha
                fabSaverExpensesHint.alpha = alpha
                fabSaverIncomeHint.alpha = alpha
            }
        }
        animator.addListener(object: AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) {
                    binding.run {
                        fabExpensesHint.visibility = View.VISIBLE
                        fabIncomeHint.visibility = View.VISIBLE
                        fabTransferHint.visibility = View.VISIBLE
                        fabSaverExpensesHint.visibility = View.VISIBLE
                        fabSaverIncomeHint.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) {
                    binding.run {
                        fabExpensesHint.visibility = View.GONE
                        fabIncomeHint.visibility = View.GONE
                        fabTransferHint.visibility = View.GONE
                        fabSaverExpensesHint.visibility = View.GONE
                        fabSaverIncomeHint.visibility = View.GONE
                    }
                }
            }

        })
        return animator
    }

    private fun navigateToAddOperation(view: View) {
        val bundle = Bundle()
        val date = mainVM.currentDate.value ?: Date()
        bundle.putLong(NewOperationFragment.EXTRA_OPERATION_DATE, date.time)
        bundle.putInt(NewOperationFragment.EXTRA_OPERATION_TYPE, when (view.id) {
            R.id.fabExpenses, R.id.fabExpensesHint -> OperationType.EXPENSES.value
            R.id.fabIncome, R.id.fabIncomeHint -> OperationType.EXPENSES.value
            R.id.fabTransfer, R.id.fabTransferHint -> OperationType.EXPENSES.value
            R.id.fabSaverExpenses, R.id.fabSaverExpensesHint -> OperationType.EXPENSES.value
            R.id.fabSaverIncome, R.id.fabSaverIncomeHint -> OperationType.EXPENSES.value
            else -> throw IllegalArgumentException("Wrong operation type.")
        })
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