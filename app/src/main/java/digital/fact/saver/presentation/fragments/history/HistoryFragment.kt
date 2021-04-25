package digital.fact.saver.presentation.fragments.history

import android.animation.*
import android.graphics.Rect
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.App
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbOperation.*
import digital.fact.saver.databinding.FragmentHistoryBinding
import digital.fact.saver.domain.models.DailyFee
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.Template
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.recycler.DailyFeesAdapter
import digital.fact.saver.presentation.adapters.recycler.TemplatesAdapter
import digital.fact.saver.presentation.customviews.CounterDrawable
import digital.fact.saver.presentation.dialogs.CurrentPlansDialog
import digital.fact.saver.presentation.dialogs.TemplateDialog
import digital.fact.saver.presentation.fragments.operation.NewOperationFragment
import digital.fact.saver.utils.*
import digital.fact.saver.utils.events.EventObserver
import eightbitlab.com.blurview.RenderScriptBlur
import java.lang.IllegalArgumentException
import java.util.*

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding
    private lateinit var historyVM: HistoryViewModel
    private var isAnimationRunning = false
    private lateinit var mViewPager: ViewPager2
    private var textColorWhite = 0
    private var textColorRed = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        setupViewPager()
        setupBlurView()
        binding.dailyFees.addItemDecoration(RvItemDecoration())
        binding.templates.addItemDecoration(RvItemDecoration())
        binding.root.doOnLayout { setOperationsLayoutHeight(it.width) }
        return binding.root
    }

    private fun setOperationsLayoutHeight(width: Int) {
        val margin = resources.getDimension(R.dimen.normalMargin).toInt()
        val buttonsMargin = resources.getDimension(R.dimen.smallMargin).toInt()
        val buttonWidth = (width - buttonsMargin * 6) / 5
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            buttonWidth + buttonsMargin * 2
        )
        params.bottomToTop = R.id.add
        params.setMargins(0, 0, 0, margin)
        binding.operationsLayout.layoutParams = params
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = HistoryViewModel.HistoryViewModelFactory(mainVM)
        historyVM = ViewModelProvider(requireActivity(), factory)[HistoryViewModel::class.java]
        setObservers()
        setInfoPanel()
        setListeners()
        textColorWhite = ContextCompat.getColor(requireActivity(), R.color.textColorWhite)
        textColorRed = ContextCompat.getColor(requireActivity(), R.color.textColorRed)
    }

    override fun onStart() {
        super.onStart()
        (requireActivity() as MainActivity).showBottomNavigationView()
    }

    override fun onStop() {
        super.onStop()
        economyAnimator?.end()
        savingsAnimator?.end()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        historyVM.resetSecondLayerState()
    }

    private fun setListeners() {
        binding.run {
            datePicker.setOnClickListener { showDatePicker() }
            toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).openDrawer() }
            weekCalendar.setOnDateChangedListener { historyVM.setCurrentDate(it.time) }
            add.setOnClickListener { onAddButtonClicked() }
            secondLayerBackground.setOnClickListener { onAddButtonClicked() }
            expenses.setOnClickListener { navigateToAddOperation(it) }
            income.setOnClickListener { navigateToAddOperation(it) }
            transfer.setOnClickListener { navigateToAddOperation(it) }
            saverExpenses.setOnClickListener { navigateToAddOperation(it) }
            saverIncome.setOnClickListener { navigateToAddOperation(it) }
            calculations.setOnClickListener { }
            toolbar.setOnMenuItemClickListener(onMenuItemClicked)
            plans.setOnClickListener { onPlansButtonClicked() }
        }
    }

    private fun onPlansButtonClicked() {
        CurrentPlansDialog(
            onPlanClicked
        ).show(childFragmentManager, "current_plans_dialog")
    }

    private val onPlanClicked: (Int, Long, Long, String) -> Unit =
        { operationType, planId, planSum, planName ->
            navigateToAddPlannedOperation(operationType, planId, planSum, planName)
        }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.today -> historyVM.setCurrentDate(Date())
        }
        true
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
            onBackPressedCallback.remove()
        }
    }

    private fun setObservers() {
        historyVM.run {
            currentDate.observe(viewLifecycleOwner) { onCurrentDateChanged(it) }
            periodDaysLeft.observe(viewLifecycleOwner) { onPeriodDaysLeftChanged(it) }
            secondLayerEvent.observe(viewLifecycleOwner, EventObserver(onSecondLayerEvent))
            currentPlans.observe(viewLifecycleOwner) { onCurrentPlansChanged(it) }
            economy.observe(viewLifecycleOwner) { updateEconomy(it) }
            savings.observe(viewLifecycleOwner) { updateSavings(it) }
            periodProgress.observe(viewLifecycleOwner) { updateProgress(it) }
            dailyFees.observe(viewLifecycleOwner) { onDailyFeesChanged(it) }
            templates.observe(viewLifecycleOwner) { onTemplatesChanged(it) }
        }
    }

    private fun onTemplatesChanged(templates: List<Template>?) {
        templates?.run {
            if (binding.templates.adapter == null) {
                binding.templates.adapter = TemplatesAdapter(onTemplateClicked)
            }
            (binding.templates.adapter as TemplatesAdapter).submitList(templates)
        }
    }

    private val onTemplateClicked: (Template) -> Unit = {
        historyVM.onAddOperationButtonClicked()
        TemplateDialog(
            historyVM.currentDate.value?.time ?: Date().time,
            it
        ).show(childFragmentManager, "template_dialog")
    }

    private fun onDailyFeesChanged(dailyFees: List<DailyFee>?) {
        dailyFees?.run {
            if (binding.dailyFees.adapter == null) {
                binding.dailyFees.adapter = DailyFeesAdapter(onDailyFeeClicked)
            }
            (binding.dailyFees.adapter as DailyFeesAdapter).submitList(dailyFees)
        }
    }

    private val onDailyFeeClicked: (Long, Long) -> Unit = { sum, sourceId ->
        historyVM.addDailyFeeOperation(sum, sourceId)
        onAddButtonClicked()
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
        val operationsAnimator = getOperationsAnimator(true)
        val templatesAnimator = getTemplatesAnimator(true)
        val dailyFeesAnimator = getDailyFeesAnimator(true)
        val buttonsSet = AnimatorSet()
        when {
            historyVM.shouldShowDailyFee && historyVM.templates.value?.isNullOrEmpty() == false ->
                buttonsSet.playTogether(operationsAnimator, templatesAnimator, dailyFeesAnimator)
            historyVM.shouldShowDailyFee && historyVM.templates.value?.isNullOrEmpty() == true ->
                buttonsSet.playTogether(operationsAnimator, dailyFeesAnimator)
            !historyVM.shouldShowDailyFee && historyVM.templates.value?.isNullOrEmpty() == false ->
                buttonsSet.playTogether(operationsAnimator, templatesAnimator)
            else -> buttonsSet.playTogether(operationsAnimator)
        }
        val resultSet = AnimatorSet()
        resultSet.playSequentially(layerSet, buttonsSet)
        resultSet.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                isAnimationRunning = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimationRunning = false
            }

        })
        return resultSet
    }

    private fun getSecondLayerHidingAnimatorSet(): AnimatorSet {
        val firstSet = AnimatorSet()
        firstSet.playTogether(
            getAddButtonOpeningAnimator(false),
            getSecondLayerAnimator(false),
            getStatusBarAnimator(false),
            getOperationsAnimator(false)
        )
        firstSet.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                isAnimationRunning = true
            }

            override fun onAnimationEnd(animation: Animator?) {
                isAnimationRunning = false
            }

        })
        val secondSet = AnimatorSet()
        if (historyVM.shouldShowDailyFee) {
            secondSet.playTogether(firstSet, getDailyFeesAnimator(false))
        } else secondSet.playTogether(firstSet)
        val thirdSet = AnimatorSet()
        if (historyVM.templates.value?.isNullOrEmpty() == false) {
            thirdSet.playTogether(secondSet, getTemplatesAnimator(false))
        } else thirdSet.playTogether(secondSet)
        return thirdSet
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

    private fun getOperationsAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        animator.addUpdateListener {
            binding.run {
                val alpha = it.animatedValue as Float
                operationsLayout.alpha = alpha
                operationsHint.alpha = alpha
            }
        }
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) {
                    binding.run {
                        operationsLayout.visibility = View.VISIBLE
                        operationsHint.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) {
                    binding.run {
                        operationsLayout.visibility = View.GONE
                        operationsHint.visibility = View.GONE
                    }
                }
            }

        })
        return animator
    }

    private fun getTemplatesAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        animator.addUpdateListener {
            binding.run {
                val alpha = it.animatedValue as Float
                templates.alpha = alpha
                templatesHint.alpha = alpha
            }
        }
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) {
                    binding.run {
                        templates.visibility = View.VISIBLE
                        templatesHint.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) {
                    binding.run {
                        templates.visibility = View.GONE
                        templatesHint.visibility = View.GONE
                    }
                }
            }

        })
        if (isShowing) animator.startDelay = 40L
        return animator
    }

    private fun getDailyFeesAnimator(isShowing: Boolean): Animator {
        val animator = ValueAnimator.ofFloat(
            if (isShowing) 0f else 1f,
            if (isShowing) 1f else 0f
        )
        animator.addUpdateListener {
            binding.run {
                val alpha = it.animatedValue as Float
                dailyFees.alpha = alpha
                dailyFeesHint.alpha = alpha
            }
        }
        animator.addListener(object : AnimatorListenerAdapter() {

            override fun onAnimationStart(animation: Animator?) {
                if (isShowing) {
                    binding.run {
                        dailyFees.visibility = View.VISIBLE
                        dailyFeesHint.visibility = View.VISIBLE
                    }
                }
            }

            override fun onAnimationEnd(animation: Animator?) {
                if (!isShowing) {
                    binding.run {
                        dailyFees.visibility = View.GONE
                        dailyFeesHint.visibility = View.GONE
                    }
                }
            }

        })
        if (isShowing) animator.startDelay = 80L
        return animator
    }

    private fun navigateToAddPlannedOperation(
        operationType: Int,
        planId: Long,
        planSum: Long,
        planName: String
    ) {
        val bundle = Bundle()
        val date = historyVM.currentDate.value ?: Date()
        bundle.putLong(NewOperationFragment.EXTRA_OPERATION_DATE, date.time)
        bundle.putInt(NewOperationFragment.EXTRA_OPERATION_TYPE, operationType)
        bundle.putLong(NewOperationFragment.EXTRA_PLAN_ID, planId)
        bundle.putLong(NewOperationFragment.EXTRA_PLAN_SUM, planSum)
        bundle.putString(NewOperationFragment.EXTRA_PLAN_NAME, planName)
        findNavController().navigate(R.id.action_historyFragment_to_newOperationFragment, bundle)
    }

    private fun navigateToAddOperation(view: View) {
        val bundle = Bundle()
        val date = historyVM.currentDate.value ?: Date()
        bundle.putLong(NewOperationFragment.EXTRA_OPERATION_DATE, date.time)
        bundle.putInt(
            NewOperationFragment.EXTRA_OPERATION_TYPE, when (view.id) {
                R.id.expenses -> OperationType.EXPENSES.value
                R.id.income -> OperationType.INCOME.value
                R.id.transfer -> OperationType.TRANSFER.value
                R.id.saverExpenses -> OperationType.SAVER_EXPENSES.value
                R.id.saverIncome -> OperationType.SAVER_INCOME.value
                else -> throw IllegalArgumentException("Wrong operation type.")
            }
        )
        findNavController().navigate(R.id.action_historyFragment_to_newOperationFragment, bundle)
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
        builder.setSelection(historyVM.currentDate.value?.time)
        val fragment = builder.build()
        fragment.addOnPositiveButtonClickListener { historyVM.setCurrentDate(Date(it)) }
        fragment.show(childFragmentManager, "date_picker")
    }

    private fun onPeriodDaysLeftChanged(daysLeft: Long) {
        if (daysLeft == 0L) {
            binding.subtitle.text = getString(R.string.hintOutsideOfThePeriod)
        } else {
            binding.subtitle.text = getElapsedTimeString(daysLeft)
            binding.subtitle.visibility = View.VISIBLE
        }
    }

    private fun getElapsedTimeString(daysLeft: Long): String {
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

    private fun onCurrentDateChanged(newDate: Date) {
        setupBottomPanel()
        binding.title.text = getFormattedDateForHistory(newDate)
        binding.weekCalendar.setCurrentDate(newDate)
        if (mViewPager.adapter == null || historyVM.shouldRecreateHistoryAdapter) {
            mViewPager.adapter = ViewPagerAdapter(newDate, this)
            mViewPager.setCurrentItem(Int.MAX_VALUE / 2, false)
        } else {
            if (!shouldHandleDataChange) {
                shouldHandleDataChange = true
                return
            }
            val daysDiff = getDaysDifference(newDate, getCurrentViewPagerDate()).toInt()
            mViewPager.setCurrentItem(mViewPager.currentItem + daysDiff, true)
        }
    }

    private var shouldHandleDataChange = true

    private fun setupViewPager() {
        mViewPager = ViewPager2(requireContext())
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            0
        )
        params.topToBottom = R.id.toolbarContainer
        params.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
        mViewPager.layoutParams = params
        binding.root.addView(mViewPager)
        mViewPager.offscreenPageLimit = 3
        binding.blurView.doOnLayout {
            historyVM.setHistoryBlurViewWidth(it.height)
        }
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                shouldHandleDataChange = false
                historyVM.setCurrentDate(getCurrentViewPagerDate())
            }
        })
    }

    fun getCurrentViewPagerDate(): Date {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.time = (mViewPager.adapter as ViewPagerAdapter).initialDate
        calendar.add(Calendar.DAY_OF_YEAR, mViewPager.currentItem - Int.MAX_VALUE / 2)
        return calendar.time
    }

    class ViewPagerAdapter(
        val initialDate: Date,
        fragment: Fragment
    ) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = Int.MAX_VALUE

        override fun createFragment(position: Int): Fragment {
            val fragment = OperationsFragment()
            val bundle = Bundle()
            bundle.putLong(OperationsFragment.EXTRA_INITIAL_DATE, initialDate.time)
            bundle.putInt(OperationsFragment.EXTRA_POSITION, position)
            fragment.arguments = bundle
            return fragment
        }

    }

    private var isInfoPanelShown = true

    private fun hideInfoPanel() {
        if (!isInfoPanelShown) return
        val animator = ObjectAnimator.ofFloat(
            binding.blurView,
            View.ALPHA,
            1f, 0f
        )
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                binding.blurView.visibility = View.INVISIBLE
                isInfoPanelShown = false
            }
        })
        animator.start()
    }

    private fun showInfoPanel() {
        if (isInfoPanelShown) return
        val animator = ObjectAnimator.ofFloat(
            binding.blurView,
            View.ALPHA,
            0f, 1f
        )
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.blurView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                isInfoPanelShown = true
            }
        })
        animator.start()
    }

    private fun setupBottomPanel() {
        if (shouldPlansButtonBeVisible()) binding.plans.show()
        else binding.plans.hide()
        if (historyVM.isInsideCurrentPeriod()) showInfoPanel()
        else hideInfoPanel()
    }

    private fun onCurrentPlansChanged(plans: List<Plan>?) {
        if (shouldPlansButtonBeVisible())
            binding.plans.show()
        else binding.plans.hide()
        setPlansCount(plans?.size ?: 0)
    }

    private fun setPlansCount(count: Int) {
        val icon = binding.plans.drawable as LayerDrawable
        val counterDrawable: CounterDrawable
        val reuse = icon.findDrawableByLayerId(R.id.counter)
        counterDrawable = if (reuse != null && reuse is CounterDrawable) reuse
        else CounterDrawable(requireActivity())
        counterDrawable.setCount(count)
        icon.mutate()
        icon.setDrawableByLayerId(R.id.counter, counterDrawable)
        binding.plans.invalidate()
    }

    private fun shouldPlansButtonBeVisible(): Boolean {
        return (!historyVM.currentPlans.value.isNullOrEmpty() && historyVM.isInsideCurrentPeriod())
    }

    private fun setInfoPanel() {
        binding.economy.text = (historyVM.economy.value ?: 0L).formatToMoney()
        binding.savings.text = (historyVM.savings.value ?: 0L).formatToMoney()
    }

    private var economyAnimator: ValueAnimator? = null

    private fun updateEconomy(newValue: Long) {
        economyAnimator?.cancel()
        val currentValue = getLongSumFromString(binding.economy.text.toString(), true)
        economyAnimator = ValueAnimator.ofInt(currentValue.toInt(), newValue.toInt())
        (economyAnimator as ValueAnimator).addUpdateListener {
            val value = (it.animatedValue as Int).toLong()
            binding.economy.text = value.formatToMoney()
            if (value >= 0L) binding.economy.setTextColor(textColorWhite)
            else binding.economy.setTextColor(textColorRed)
        }
        val duration = resources.getInteger(R.integer.valuesAnimationTime)
        (economyAnimator as ValueAnimator).duration = duration.toLong()
        (economyAnimator as ValueAnimator).start()
    }

    private var savingsAnimator: ValueAnimator? = null

    private fun updateSavings(newValue: Long) {
        savingsAnimator?.cancel()
        val currentValue = getLongSumFromString(binding.savings.text.toString(), true)
        savingsAnimator = ValueAnimator.ofInt(currentValue.toInt(), newValue.toInt())
        (savingsAnimator as ValueAnimator).addUpdateListener {
            val value = (it.animatedValue as Int).toLong()
            binding.savings.text = value.formatToMoney()
            if (value >= 0L) binding.savings.setTextColor(textColorWhite)
            else binding.savings.setTextColor(textColorRed)
        }
        val duration = resources.getInteger(R.integer.valuesAnimationTime)
        (savingsAnimator as ValueAnimator).duration = duration.toLong()
        (savingsAnimator as ValueAnimator).start()
    }

    private var progressAnimator: ValueAnimator? = null

    private fun updateProgress(progress: Int) {
        progressAnimator?.cancel()
        val currentValue = binding.periodProgress.progress
        if (currentValue == 0) {
            binding.periodProgress.progress = progress
            return
        }
        progressAnimator = ValueAnimator.ofInt(currentValue, progress)
        (progressAnimator as ValueAnimator).addUpdateListener {
            binding.periodProgress.progress = it.animatedValue as Int
        }
        val duration = resources.getInteger(R.integer.progressAnimationTime)
        (progressAnimator as ValueAnimator).duration = duration.toLong()
        (progressAnimator as ValueAnimator).start()
    }

    private class RvItemDecoration : RecyclerView.ItemDecoration() {

        val margin = App.getInstance().resources.getDimension(R.dimen.smallMargin).toInt()

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            outRect.set(
                if (position == 0) margin else 0,
                0,
                margin,
                0
            )
        }
    }

}