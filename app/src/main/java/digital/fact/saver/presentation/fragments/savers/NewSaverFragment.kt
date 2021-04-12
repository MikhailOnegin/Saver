package digital.fact.saver.presentation.fragments.savers

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.databinding.FragmentNewSaverBinding
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.resetTimeInMillis
import digital.fact.saver.utils.toLongFormatter
import java.text.SimpleDateFormat
import java.util.*

class NewSaverFragment : Fragment() {

    private lateinit var binding: FragmentNewSaverBinding
    private lateinit var sourcesVM: SourcesViewModel
    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private var previousPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNewSaverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStop() {
        super.onStop()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        setDecoration()
        setViewPager()
        setDefaultDateToButton()
        setListeners()
    }

    private fun setDecoration() {
        binding.viewPager.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.right =
                    view.context?.resources?.getDimension(R.dimen._16dp)?.toInt() ?: 0
                outRect.left =
                    view.context?.resources?.getDimension(R.dimen._16dp)?.toInt() ?: 0
            }
        })
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDefaultDateToButton() {
        val sdf = SimpleDateFormat("dd.MM.yyyy")
        binding.walletCreateDate.text = sdf.format(Date()).toString()
    }

    private fun setListeners() {
        binding.walletCreateDate.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
        binding.createSaver.setOnClickListener { addSaver() }
        binding.walletName.doOnTextChanged { text, _, _, _ ->
            binding.createSaver.isEnabled = !text.isNullOrEmpty() && text.length > 2
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setTheme(R.style.Calendar)
        builder.setSelection(Date().time)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            binding.walletCreateDate.text = sdf.format(it).toString()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.accept -> addSaver()
        }
        true
    }

    private fun addSaver() {
        sourcesVM.insertSource(
            Source(
                name = binding.walletName.text.toString(),
                type = Source.Type.SAVER.value,
                aim_sum = binding.aimMoney.text.toString().toLongFormatter(),
                adding_date = resetTimeInMillis(
                    sdf.parse(binding.walletCreateDate.text.toString())?.time ?: 0L
                ),
                sort_order = 0,
                visibility = Source.Visibility.VISIBLE.value,
            )
        )
        findNavController().popBackStack()
    }

    private fun setViewPager() {
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
        val minWidth = resources.getDimension(R.dimen._8dp)
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
            params.marginStart = resources.getDimension(R.dimen._4dp).toInt()
            params.marginEnd = resources.getDimension(R.dimen._4dp).toInt()
            binding.customTabLayout.getChildAt(previousPosition).layoutParams = params
        }
        animationSelectedExpand.addUpdateListener {
            val params = LinearLayout.LayoutParams(
                (it.animatedValue as Float).toInt(),
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            params.marginStart = resources.getDimension(R.dimen._4dp).toInt()
            params.marginEnd = resources.getDimension(R.dimen._4dp).toInt()
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

    companion object {
        const val HINT = "HINT"
        const val HINT_VIRTUAL = "HINT_VIRTUAL"
        const val HINT_INCOME = "HINT_INCOME"
        const val HINT_OUTCOME = "HINT_OUTCOME"
    }

}