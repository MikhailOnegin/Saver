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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentOperationBinding
import digital.fact.saver.utils.getFullFormattedDate
import digital.fact.saver.utils.insertGroupSeparators
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
        binding.toolbar.title = getString(R.string.newOperationFragmentTitle)
        binding.buttonProceed.visibility = View.VISIBLE
        binding.buttonCreate.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        operationVM = ViewModelProvider(this)[OperationViewModel::class.java]
        initializeDate()
        setObservers()
        setListeners()
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
        }
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
            binding.buttonProceed.isEnabled = true
        }
    }

    private fun showKeyBoard() {
        if (!isKeyboardShown) {
            isKeyboardShown = true
            getKeyboardShowingAnimatorSet().start()
            binding.buttonProceed.visibility = View.VISIBLE
            binding.buttonCreate.visibility = View.GONE
        }
    }

    private fun hideKeyboard() {
        if (isKeyboardShown) {
            isKeyboardShown = false
            getKeyboardHidingAnimatorSet().start()
            binding.buttonProceed.visibility = View.GONE
            binding.buttonCreate.visibility = View.VISIBLE
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

    }

}