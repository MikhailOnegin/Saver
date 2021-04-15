package digital.fact.saver.presentation.fragments.period

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPeriodBinding
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.utils.*
import eightbitlab.com.blurview.RenderScriptBlur
import java.text.SimpleDateFormat
import java.util.*

class PeriodFragment : Fragment() {

    private lateinit var binding: FragmentPeriodBinding
    private lateinit var mainVM: MainViewModel

    private val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPeriodBinding.inflate(inflater, container, false)
        setupBlurView()
        return binding.root
    }

    private fun setupBlurView() {
        val radius = 10f
        binding.blurView.setupWith(binding.root)
            .setBlurAlgorithm(RenderScriptBlur(requireActivity()))
            .setBlurRadius(radius)
            .setBlurAutoUpdate(true)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setListeners()
        setObservers()
    }

    private fun setObservers() {
        mainVM.conditionsChanged.observe(viewLifecycleOwner) { onPeriodChanged() }
    }

    private fun setListeners() {
        binding.period.setOnClickListener { showDatePicker() }
        binding.toolbar.setNavigationOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTheme(R.style.Calendar)
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener {
            if (resetTimeInMillis(it.first ?: 0L)
                == resetTimeInMillis(it.second ?: 0L)
            ) {
                createSnackBar(
                    binding.root,
                    getString(R.string.errorPeriodDays)
                ).show()
            } else {
                mainVM.updatePeriod(it.first ?: 0L, it.second ?: 0L)
            }
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun onPeriodChanged() {
        val stringBuilder = StringBuilder()
        stringBuilder.append(getString(R.string.period))
        stringBuilder.append(": ")
        stringBuilder.append(sdf.format(mainVM.periodStart.value))
        stringBuilder.append(" - ")
        stringBuilder.append(sdf.format(mainVM.periodEnd.value))
        binding.period.text = stringBuilder.toString()
    }

}