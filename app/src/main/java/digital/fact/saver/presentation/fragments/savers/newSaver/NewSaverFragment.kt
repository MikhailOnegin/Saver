package digital.fact.saver.presentation.fragments.savers.newSaver

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.App
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentSaverNewBinding
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.SumInputFilter
import digital.fact.saver.utils.formatToMoney
import digital.fact.saver.utils.getLongSumFromString
import java.text.SimpleDateFormat
import java.util.*

class NewSaverFragment : Fragment() {

    private lateinit var binding: FragmentSaverNewBinding
    private lateinit var newSaverVM: NewSaverViewModel

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var previousPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaverNewBinding.inflate(inflater, container, false)
        setupViewPager()
        binding.aimMoney.filters = arrayOf(SumInputFilter())
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = NewSaverViewModel.SaverVMFactory(mainVM)
        newSaverVM = ViewModelProvider(this, factory)[NewSaverViewModel::class.java]
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        newSaverVM.run {
            creationDate.observe(viewLifecycleOwner) { onCreationDateChanged(it) }
            aimDate.observe(viewLifecycleOwner) { onAimDateChanged(it) }
            saverCreatedEvent.observe(viewLifecycleOwner) { onSaverCreatedEvent() }
            dailyFee.observe(viewLifecycleOwner) { onDailyFeeChanged(it, binding.dailyFeeHint) }
        }
    }

    private fun onSaverCreatedEvent() {
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
        (imm  as InputMethodManager).hideSoftInputFromWindow(binding.root.windowToken, 0)
        findNavController().popBackStack()
    }

    private fun onCreationDateChanged(date: Date?) {
        date?.run {
            binding.creationDate.text = dateFormat.format(this)
        }
    }

    private fun onAimDateChanged(date: Date?) {
        date?.run {
            binding.aimDate.text = dateFormat.format(this)
        }
    }

    private fun setViewPagerItemDecoration() {
        binding.viewPager.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.right =
                    view.context?.resources?.getDimension(R.dimen.normalMargin)?.toInt() ?: 0
                outRect.left =
                    view.context?.resources?.getDimension(R.dimen.normalMargin)?.toInt() ?: 0
            }
        })
    }

    private fun setListeners() {
        binding.run {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
            creationDate.setOnClickListener { showDatePicker(PickerMode.CREATION) }
            aimDate.setOnClickListener { showDatePicker(PickerMode.AIM) }
            name.doOnTextChanged { text, _, _, _ ->
                createSaver.isEnabled = !text.isNullOrEmpty()
            }
            aimMoney.doOnTextChanged { text, _, _, _ ->
                newSaverVM.setAimSum(getLongSumFromString(text.toString()))
            }
            createSaver.setOnClickListener { createSaver() }
        }
    }

    private fun showDatePicker(mode: PickerMode) {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection (when (mode) {
            PickerMode.CREATION -> newSaverVM.creationDate.value?.time ?: Date().time
            PickerMode.AIM -> newSaverVM.aimDate.value?.time ?: Date().time
        })
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            when (mode) {
                PickerMode.CREATION -> newSaverVM.setCreationDate(Date(it))
                PickerMode.AIM -> newSaverVM.setAimDate(Date(it))
            }
        }
        picker.show(childFragmentManager, "creation_date_picker")
    }

    private fun createSaver() {
        newSaverVM.createSaver(binding.name.text.toString())
    }

    private fun setupViewPager() {
        setViewPagerItemDecoration()
        val pagerAdapter = ScreenSlidePagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                animateSelection(position)
            }
        })
    }

    private fun animateSelection(position: Int) {
        val animator = AnimatorSet()
        val maxWidth = resources.getDimension(R.dimen.maxTabIndicatorWidth)
        val minWidth = resources.getDimension(R.dimen.smallMargin)
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.lightPurple)
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.gray)

        val animationDefaultCollapse = ValueAnimator.ofFloat(maxWidth, minWidth)
        val animationSelectedExpand = ValueAnimator.ofFloat(minWidth, maxWidth)
        val animationDefaultColor = ValueAnimator.ofArgb(selectedColor, defaultColor)
        val animationSelectedColor = ValueAnimator.ofArgb(defaultColor, selectedColor)

        animationDefaultCollapse.addUpdateListener {
            val params = LinearLayout.LayoutParams(
                (it.animatedValue as Float).toInt(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.marginStart = resources.getDimension(R.dimen.halfSmallMargin).toInt()
            params.marginEnd = resources.getDimension(R.dimen.halfSmallMargin).toInt()
            binding.customTabLayout.getChildAt(previousPosition).layoutParams = params
        }
        animationSelectedExpand.addUpdateListener {
            val params = LinearLayout.LayoutParams(
                (it.animatedValue as Float).toInt(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.marginStart = resources.getDimension(R.dimen.halfSmallMargin).toInt()
            params.marginEnd = resources.getDimension(R.dimen.halfSmallMargin).toInt()
            binding.customTabLayout.getChildAt(position).layoutParams = params
        }
        animationDefaultColor.addUpdateListener {
            binding.customTabLayout.getChildAt(previousPosition)
                .background.setTint(it.animatedValue as Int)
        }
        animationSelectedColor.addUpdateListener {
            binding.customTabLayout.getChildAt(position)
                .background.setTint(it.animatedValue as Int)
        }
        animator.playTogether(
            animationDefaultCollapse,
            animationSelectedExpand,
            animationDefaultColor,
            animationSelectedColor
        )
        animator.doOnEnd { previousPosition = position }
        animator.duration = 300L
        animator.start()
    }

    private inner class ScreenSlidePagerAdapter(fragment: Fragment) :
        FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putString(
                HINT, when (position) {
                    0 -> HINT_VIRTUAL
                    1 -> HINT_INCOME
                    2 -> HINT_OUTCOME
                    else -> HINT_VIRTUAL
                }
            )
            val fragment = HintFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    enum class PickerMode { CREATION, AIM }

    companion object {
        const val HINT = "HINT"
        const val HINT_VIRTUAL = "HINT_VIRTUAL"
        const val HINT_INCOME = "HINT_INCOME"
        const val HINT_OUTCOME = "HINT_OUTCOME"

        val builder = StringBuilder()
        fun onDailyFeeChanged(dailyFee: Long, hintView: TextView) {
            builder.clear()
            builder.append(App.getInstance().getString(R.string.dailyFeeHint1))
            builder.append(" ")
            builder.append(dailyFee.formatToMoney(true))
            builder.append(" ")
            builder.append(App.getInstance().getString(R.string.dailyFeeHint2))
            hintView.text = builder.toString()
            if (dailyFee <= 0L) {
                hintView.visibility = View.GONE
            } else hintView.visibility = View.VISIBLE
        }

    }

}