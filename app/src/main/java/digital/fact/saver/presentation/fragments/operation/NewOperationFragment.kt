package digital.fact.saver.presentation.fragments.operation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Operation.OperationType
import digital.fact.saver.databinding.FragmentOperationBinding
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.spinner.SpinnerSourcesAdapter
import digital.fact.saver.presentation.dialogs.ConfirmationDialog
import digital.fact.saver.utils.*
import java.lang.IllegalArgumentException
import java.util.*

class NewOperationFragment : Fragment() {

    private lateinit var binding: FragmentOperationBinding
    private lateinit var operationVM: OperationViewModel
    private var isKeyboardShown = true

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentOperationBinding.inflate(inflater, container, false)
        initializeViews()
        return binding.root
    }

    private fun initializeViews() {
        setKeyboard()
        adjustToOperationType()
    }

    private fun adjustToOperationType() {
        arguments?.getInt(EXTRA_OPERATION_TYPE)?.let {
            when (it) {
                OperationType.EXPENSES.value -> prepareLayoutForExpenses()
                OperationType.INCOME.value -> prepareLayoutForIncome()
                OperationType.TRANSFER.value -> prepareLayoutForTransfer()
                OperationType.SAVER_EXPENSES.value -> prepareLayoutForSaverExpenses()
                OperationType.SAVER_INCOME.value -> prepareLayoutForSaverIncome()
                OperationType.PLANNED_EXPENSES.value -> prepareLayoutForPlannedExpenses()
                OperationType.PLANNED_INCOME.value -> prepareLayoutForPlannedIncome()
            }
        }
    }

    private fun prepareLayoutForExpenses() {
        binding.run {
            toolbar.title = getString(R.string.hintFabExpenses)
            transferHint.visibility = View.GONE
            toTitle.visibility = View.GONE
            to.visibility = View.GONE
        }
    }

    private fun prepareLayoutForPlannedExpenses() {
        binding.run {
            toolbar.title = getString(R.string.hintFabPlannedExpenses)
            planSumHint.text = getString(R.string.planSumHintExpenses)
            factSumHint.text = getString(R.string.factSumHintExpenses)
            planName.text = arguments?.getString(EXTRA_PLAN_NAME)
            planContainer.visibility = View.VISIBLE
            buttonCreate.setText(R.string.buttonCreatePlannedOperation)
            buttonCreatePartOfPlan.visibility = View.VISIBLE
            transferHint.visibility = View.GONE
            toTitle.visibility = View.GONE
            to.visibility = View.GONE
            nameTitle.visibility = View.GONE
            name.visibility = View.GONE
            (binding.sumContainer.layoutParams as ConstraintLayout.LayoutParams).setMargins(0,0,0,0)
        }
    }

    private fun prepareLayoutForIncome() {
        binding.run {
            toolbar.title = getString(R.string.hintFabIncome)
            transferHint.visibility = View.GONE
            fromTitle.visibility = View.GONE
            from.visibility = View.GONE
        }
    }

    private fun prepareLayoutForPlannedIncome() {
        binding.run {
            toolbar.title = getString(R.string.hintFabPlannedIncome)
            planSumHint.text = getString(R.string.planSumHintIncome)
            factSumHint.text = getString(R.string.factSumHintIncome)
            planName.text = arguments?.getString(EXTRA_PLAN_NAME)
            planContainer.visibility = View.VISIBLE
            buttonCreate.setText(R.string.buttonCreatePlannedOperation)
            buttonCreatePartOfPlan.visibility = View.VISIBLE
            transferHint.visibility = View.GONE
            fromTitle.visibility = View.GONE
            from.visibility = View.GONE
            nameTitle.visibility = View.GONE
            name.visibility = View.GONE
            (binding.sumContainer.layoutParams as ConstraintLayout.LayoutParams).setMargins(0,0,0,0)
        }
    }

    private fun prepareLayoutForTransfer() {
        binding.run {
            toolbar.title = getString(R.string.hintFabTransfer)
            nameTitle.visibility = View.GONE
            name.visibility = View.GONE
            (toTitle.layoutParams as LinearLayout.LayoutParams)
                    .setMargins(
                            resources.getDimension(R.dimen.normalMargin).toInt(),
                            0,
                            resources.getDimension(R.dimen.normalMargin).toInt(),
                            0
                    )
        }
    }

    private fun prepareLayoutForSaverExpenses() {
        binding.run {
            toolbar.title = getString(R.string.hintFabSaverExpensesCut)
            transferHint.visibility = View.GONE
            nameTitle.visibility = View.GONE
            name.visibility = View.GONE
            toTitle.visibility = View.GONE
            to.visibility = View.GONE
        }
    }

    private fun prepareLayoutForSaverIncome() {
        binding.run {
            toolbar.title = getString(R.string.hintFabSaverIncomeCut)
            transferHint.visibility = View.GONE
            nameTitle.visibility = View.GONE
            name.visibility = View.GONE
            fromTitle.visibility = View.GONE
            from.visibility = View.GONE
        }
    }

    private fun setKeyboard() {
        binding.run {
            if (isKeyboardShown) {
                gridLayout.visibility = View.VISIBLE
                container.visibility = View.GONE
                buttonProceed.visibility = View.VISIBLE
                buttonsContainer.visibility = View.GONE
            } else {
                gridLayout.visibility = View.GONE
                container.visibility = View.VISIBLE
                buttonProceed.visibility = View.VISIBLE
                buttonsContainer.visibility = View.GONE
            }
        }
    }

    private fun initializeSpinners(sources: List<Sources>) {
        val fromAdapter = SpinnerSourcesAdapter(
                requireActivity(), R.layout.spinner_source_dropdown)
        fromAdapter.addAll(sources)
        binding.from.adapter = fromAdapter
        binding.to.adapter = fromAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = OperationViewModel.OperationViewModelFactory(mainVM)
        operationVM = ViewModelProvider(this, factory)[OperationViewModel::class.java]
        operationVM.initializeSources(arguments?.getInt(EXTRA_OPERATION_TYPE)
                ?: throw IllegalArgumentException("Wrong operation type."))
        initializeDate()
        setObservers()
        setListeners()
        val extraSum = arguments?.getLong(EXTRA_PLAN_SUM)
        extraSum?.run {
            if (this != 0L) {
                operationVM.setSumFromExtra(getSumStringFromLong(this))
            }
            binding.planSum.text = extraSum.formatToMoney(true)
        }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().run {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }
    }

    override fun onStop() {
        super.onStop()
        hideSoftKeyboard()
    }

    private fun hideSoftKeyboard() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun setObservers() {
        operationVM.run {
            date.observe(viewLifecycleOwner) { onOperationDateChanged(it) }
            sum.observe(viewLifecycleOwner) { onSumChanged(it) }
            sources.observe(viewLifecycleOwner) { initializeSpinners(it) }
            operationCreatedEvent.observe(viewLifecycleOwner) { findNavController().popBackStack() }
        }
    }

    private fun setListeners() {
        binding.run {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            key0.setOnClickListener(onKeyPressed)
            key1.setOnClickListener(onKeyPressed)
            key2.setOnClickListener(onKeyPressed)
            key3.setOnClickListener(onKeyPressed)
            key4.setOnClickListener(onKeyPressed)
            key5.setOnClickListener(onKeyPressed)
            key6.setOnClickListener(onKeyPressed)
            key7.setOnClickListener(onKeyPressed)
            key8.setOnClickListener(onKeyPressed)
            key9.setOnClickListener(onKeyPressed)
            keyComma.setOnClickListener(onKeyPressed)
            keyBackspace.setOnClickListener(onKeyPressed)
            buttonProceed.setOnClickListener { hideKeyboard() }
            sum.setOnClickListener { showKeyBoard() }
            buttonCreate.setOnClickListener { onButtonCreateClicked() }
            buttonCreatePartOfPlan.setOnClickListener { onButtonCreatePartOfPlanClicked() }
        }
    }

    private fun onButtonCreatePartOfPlanClicked() {
        if (hasProblemsWithPartSum()) return
        ConfirmationDialog(
                title = getString(R.string.dialogPerformPartOfPlanTitle),
                message = getString(R.string.dialogPerformPartOfPlanMessage),
                positiveButtonText = getString(R.string.buttonPerform),
                onPositiveButtonClicked = { onButtonCreateClicked(true) }
        ).show(childFragmentManager, "perform_part_of_plan_dialog")
    }

    private fun onButtonCreateClicked(isPartOfPlan: Boolean = false) {
        if (hasProblemsWithSum()) return
        if (hasProblemsWithTransfer()) return
        operationVM.createNewOperation(
                operationType = arguments?.getInt(EXTRA_OPERATION_TYPE) ?: throw IllegalArgumentException(),
                operationName = arguments?.getString(EXTRA_PLAN_NAME) ?: binding.name.text.toString(),
                fromSourceId = getFromSourceId(),
                toSourceId = getToSourceId(),
                planId = arguments?.getLong(EXTRA_PLAN_ID) ?: 0L,
                comment = "",
                isPartOfPlan = isPartOfPlan
        )
    }

    private fun hasProblemsWithPartSum(): Boolean {
        val planSum = arguments?.getLong(EXTRA_PLAN_SUM) ?: 0L
        if (getLongSumFromString(operationVM.sum.value ?: "") >= planSum) {
            createSnackBar(
                    binding.root,
                    getString(R.string.errorPartOfPlanSum)
            ).show()
            return true
        }
        return false
    }

    private fun hasProblemsWithTransfer(): Boolean {
        if (arguments?.getInt(EXTRA_OPERATION_TYPE) == OperationType.TRANSFER.value) {
            val fromSourceId = (binding.from.selectedItem as Sources).id
            val toSourceId = (binding.to.selectedItem as Sources).id
            if (fromSourceId == toSourceId) {
                createSnackBar(
                        binding.root,
                        getString(R.string.errorSameWalletForTransfer)
                ).show()
                return true
            }
        }
        return false
    }

    private fun getFromSourceId(): Long {
        return when (arguments?.getInt(EXTRA_OPERATION_TYPE)) {
            OperationType.EXPENSES.value,
            OperationType.PLANNED_EXPENSES.value,
            OperationType.SAVER_EXPENSES.value,
            OperationType.TRANSFER.value -> (binding.from.selectedItem as Sources).id
            else -> 0L
        }
    }

    private fun getToSourceId(): Long {
        return when (arguments?.getInt(EXTRA_OPERATION_TYPE)) {
            OperationType.INCOME.value,
            OperationType.PLANNED_INCOME.value,
            OperationType.SAVER_INCOME.value,
            OperationType.TRANSFER.value -> (binding.to.selectedItem as Sources).id
            else -> 0L
        }
    }

    private fun hasProblemsWithSum(): Boolean {
        if (operationVM.sum.value.isNullOrBlank() || operationVM.hasZeroSum()) {
            createSnackBar(
                    binding.root,
                    getString(R.string.errorEnterOperationSum)
            ).show()
            return true
        }
        return false
    }

    private fun onSumChanged(sum: String) {
        if (sum.isEmpty()) {
            binding.sum.visibility = View.GONE
            binding.sumHint.visibility = View.VISIBLE
            binding.buttonProceed.isEnabled = false
        } else {
            binding.sum.text = sum.insertGroupSeparators()
            binding.sum.visibility = View.VISIBLE
            binding.sumHint.visibility = View.GONE
            binding.buttonProceed.isEnabled = !operationVM.hasZeroSum()
        }
    }

    private fun showKeyBoard() {
        if (!isKeyboardShown) {
            isKeyboardShown = true
            getKeyboardShowingAnimatorSet().start()
            binding.buttonProceed.visibility = View.VISIBLE
            binding.buttonsContainer.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        if (isKeyboardShown) {
            isKeyboardShown = false
            getKeyboardHidingAnimatorSet().start()
            binding.buttonProceed.visibility = View.GONE
            binding.buttonsContainer.visibility = View.VISIBLE
        }
    }

    private fun getKeyboardHidingAnimatorSet(): AnimatorSet {
        val alphaSet = AnimatorSet()
        val gridHidingAnimator = ValueAnimator.ofFloat(1f, 0f)
        gridHidingAnimator.addUpdateListener {
            binding.gridLayout.alpha = it.animatedValue as Float
        }
        val containerShowingAnimator = ValueAnimator.ofFloat(0f, 1f)
        containerShowingAnimator.addUpdateListener {
            binding.container.alpha = it.animatedValue as Float
        }
        containerShowingAnimator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.container.alpha = 0f
                binding.container.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                binding.name.requestFocus()
            }
        })
        val heightAnimator = ValueAnimator.ofInt(binding.gridLayout.height, 0)
        heightAnimator.addUpdateListener {
            val params = ConstraintLayout.LayoutParams(
                binding.gridLayout.width,
                it.animatedValue as Int
            )
            params.topToBottom = R.id.sumContainer
            binding.gridLayout.layoutParams = params
        }
        heightAnimator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                binding.gridLayout.visibility = View.GONE
                (binding.container.layoutParams as ConstraintLayout.LayoutParams).topToBottom = R.id.sumContainer
            }
        })
        alphaSet.playTogether(gridHidingAnimator, containerShowingAnimator)
        val resultSet = AnimatorSet()
        resultSet.playSequentially(alphaSet, heightAnimator)
        return resultSet
    }

    private fun getKeyboardShowingAnimatorSet(): AnimatorSet {
        binding.gridLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val measuredHeight = binding.gridLayout.measuredHeight
        val heightAnimator = ValueAnimator.ofInt(1, measuredHeight)
        heightAnimator.addUpdateListener {
            val params = ConstraintLayout.LayoutParams(
                binding.gridLayout.width,
                it.animatedValue as Int
            )
            params.topToBottom = R.id.sumContainer
            binding.gridLayout.layoutParams = params
        }
        heightAnimator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                val params = ConstraintLayout.LayoutParams(
                    binding.gridLayout.width,
                    1
                )
                params.topToBottom = R.id.sumContainer
                binding.gridLayout.layoutParams = params
                (binding.container.layoutParams as ConstraintLayout.LayoutParams).topToBottom = R.id.gridLayout
                binding.gridLayout.visibility = View.VISIBLE
            }
        })
        val containerHidingAnimator = ValueAnimator.ofFloat(1f, 0f)
        containerHidingAnimator.addUpdateListener {
            binding.container.alpha = it.animatedValue as Float
        }
        val gridLayoutShowingAnimator = ValueAnimator.ofFloat(0f, 1f)
        gridLayoutShowingAnimator.addUpdateListener {
            binding.gridLayout.alpha = it.animatedValue as Float
        }
        gridLayoutShowingAnimator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                hideSoftKeyboard()
                binding.gridLayout.alpha = 0f
                binding.gridLayout.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                binding.container.visibility = View.GONE
            }
        })
        val alphaSet = AnimatorSet()
        alphaSet.playTogether(containerHidingAnimator, gridLayoutShowingAnimator)
        val resultSet = AnimatorSet()
        resultSet.playSequentially(heightAnimator, alphaSet)
        return resultSet
    }

    private val onKeyPressed: (View) -> Unit = {
        when (it.id) {
            R.id.key_0 -> operationVM.onKeyboardButtonClicked("0")
            R.id.key_1 -> operationVM.onKeyboardButtonClicked("1")
            R.id.key_2 -> operationVM.onKeyboardButtonClicked("2")
            R.id.key_3 -> operationVM.onKeyboardButtonClicked("3")
            R.id.key_4 -> operationVM.onKeyboardButtonClicked("4")
            R.id.key_5 -> operationVM.onKeyboardButtonClicked("5")
            R.id.key_6 -> operationVM.onKeyboardButtonClicked("6")
            R.id.key_7 -> operationVM.onKeyboardButtonClicked("7")
            R.id.key_8 -> operationVM.onKeyboardButtonClicked("8")
            R.id.key_9 -> operationVM.onKeyboardButtonClicked("9")
            R.id.key_comma -> operationVM.onKeyboardButtonClicked(",")
            R.id.key_backspace -> operationVM.onBackspaceClicked()
        }

    }

    private fun onOperationDateChanged(date: Date) {
        binding.toolbar.subtitle = getFullFormattedDate(date)
    }

    private fun initializeDate() {
        val dateMillis = arguments?.getLong(EXTRA_OPERATION_DATE) ?: 0L
        if (dateMillis != 0L) operationVM.setOperationDate(dateMillis)
    }

    companion object {

        const val EXTRA_OPERATION_DATE = "extra_operation_date"
        const val EXTRA_OPERATION_TYPE = "extra_operation_type"
        const val EXTRA_PLAN_ID = "extra_plan_id"
        const val EXTRA_PLAN_SUM = "extra_plan_sum"
        const val EXTRA_PLAN_NAME = "extra_plan_name"

    }

}