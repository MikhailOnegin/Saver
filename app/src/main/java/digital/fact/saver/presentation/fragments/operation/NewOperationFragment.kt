package digital.fact.saver.presentation.fragments.operation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentOperationBinding
import digital.fact.saver.utils.getFullFormattedDate
import java.util.*

class NewOperationFragment : Fragment() {

    private lateinit var binding: FragmentOperationBinding
    private lateinit var operationVM: OperationViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentOperationBinding.inflate(inflater, container, false)
        binding.toolbar.title = getString(R.string.newOperationFragmentTitle)
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

    private fun setObservers() {
        operationVM.date.observe(viewLifecycleOwner) { onOperationDateChanged(it) }
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
        }
    }

    private val onKeyPressed: (View) -> Unit = {

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