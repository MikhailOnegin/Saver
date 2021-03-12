package digital.fact.saver.presentation.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.datepicker.MaterialDatePicker
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private lateinit var binding: FragmentHistoryBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setListeners()
    }

    private fun setListeners() {
        binding.datePicker.setOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        builder.setTheme(R.style.Calendar)
        builder.build().show(childFragmentManager, "date_picker")
    }

}