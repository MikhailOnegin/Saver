package digital.fact.saver.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.databinding.DialogTemplateBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.Template
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.spinner.SpinnerSourcesAdapter
import digital.fact.saver.presentation.fragments.operation.OperationViewModel
import digital.fact.saver.utils.SumInputFilter
import digital.fact.saver.utils.getLongSumFromString
import digital.fact.saver.utils.getSumStringFromLong
import java.lang.IllegalArgumentException

class TemplateDialog(
    private val operationDate: Long,
    private val template: Template
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogTemplateBinding
    private lateinit var operationVM: OperationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogTemplateBinding.inflate(inflater, container, false)
        binding.name.text = template.operationName
        binding.sum.filters = arrayOf(SumInputFilter())
        binding.sum.setText(getSumStringFromLong(template.operationSum))
        binding.sourceTitle.text = when (template.operationType) {
            DbOperation.OperationType.EXPENSES.value -> getString(R.string.operationFromTitle)
            DbOperation.OperationType.INCOME.value -> getString(R.string.operationToTitle)
            else -> throw IllegalArgumentException("Illegal operation type for template.")
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val factory = OperationViewModel.OperationViewModelFactory(mainVM)
        operationVM = ViewModelProvider(this, factory)[OperationViewModel::class.java]
        operationVM.initializeSources(template.operationType)
        setObservers()
        setListeners()
        updateCreateButtonState()
    }

    private fun setObservers() {
        operationVM.run {
            sources.observe(viewLifecycleOwner) { initializeSpinners(it) }
            operationCreatedEvent.observe(viewLifecycleOwner) { dismiss() }
        }
    }

    private fun setListeners() {
        binding.run {
            buttonCancel.setOnClickListener { dismiss() }
            sum.doOnTextChanged { _, _, _, _ -> updateCreateButtonState() }
            buttonCreate.setOnClickListener { onCreateButtonClicked() }
        }
    }

    private fun onCreateButtonClicked() {
        val fromSourceId: Long
        val toSourceId: Long
        when (template.operationType) {
            DbOperation.OperationType.EXPENSES.value ->{
                fromSourceId = (binding.source.selectedItem as Source).id
                toSourceId = 0L
            }
            DbOperation.OperationType.INCOME.value ->{
                toSourceId = (binding.source.selectedItem as Source).id
                fromSourceId = 0L
            }
            else -> throw IllegalArgumentException("Wrong operation type for template.")
        }
        operationVM.createNewOperationFromTemplate(
            operationDate = operationDate,
            operationSum = getLongSumFromString(binding.sum.text.toString()),
            operationType = template.operationType,
            operationName = template.operationName,
            fromSourceId = fromSourceId,
            toSourceId = toSourceId
        )
    }

    private fun initializeSpinners(sources: List<Source>) {
        val adapter = SpinnerSourcesAdapter(requireActivity(), R.layout.spinner_source_dropdown)
        adapter.addAll(sources)
        binding.source.adapter = adapter
        val source = sources.firstOrNull { it.id == template.sourceId }
        source?.run {
            binding.source.setSelection(adapter.getPosition(this))
        }
    }

    private fun updateCreateButtonState() {
        binding.buttonCreate.isEnabled =
            !binding.sum.text.isNullOrBlank() && operationVM.sources.value?.isNullOrEmpty() != true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}