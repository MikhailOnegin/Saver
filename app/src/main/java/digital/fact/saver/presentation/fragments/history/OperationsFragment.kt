package digital.fact.saver.presentation.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentOperationsBinding
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.recycler.OperationsAdapter
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.utils.LinearRvItemDecorations

class OperationsFragment : Fragment() {

    private lateinit var binding: FragmentOperationsBinding
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var historyVM: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOperationsBinding.inflate(inflater, container, false)
        binding.recyclerView.addItemDecoration(LinearRvItemDecorations(
                sideMarginsDimension = R.dimen.screenContentPadding,
                marginBetweenElementsDimension = R.dimen.verticalMarginBetweenListElements
        ))
        binding.recyclerView.adapter = OperationsAdapter(onOperationLongClicked)
        setEmptyView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        val oFactory = OperationsViewModel.OperationsViewModelFactory(mainVM)
        operationsVM = ViewModelProvider(this, oFactory)[OperationsViewModel::class.java]
        val hFactory = HistoryViewModel.HistoryViewModelFactory(mainVM)
        historyVM = ViewModelProvider(requireActivity(), hFactory)[HistoryViewModel::class.java]
        operationsVM.initialize(
                arguments?.getLong(EXTRA_INITIAL_DATE) ?: 0L,
                arguments?.getInt(EXTRA_POSITION) ?: 0
        )
        setObservers()
    }

    private fun setObservers() {
        operationsVM.operations.observe(viewLifecycleOwner) { onOperationsChanged(it) }
        historyVM.historyBlurViewHeight.observe(viewLifecycleOwner) {onBlurViewHeightChanged(it)}
    }

    private val onOperationLongClicked: (Long) -> Boolean = {
        SlideToPerformDialog(
                title = getString(R.string.dialogDeleteOperationTitle),
                message = getString(R.string.dialogDeleteOperationMessage),
                onSliderFinishedListener = { operationsVM.deleteOperation(it) }
        ).show(childFragmentManager, "delete_dialog")
        true
    }

    private fun onOperationsChanged(operations: List<Operation>?) {
        if (!operations.isNullOrEmpty()) {
            binding.emptyView.root.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.GONE
            binding.emptyView.root.visibility = View.VISIBLE
        }
        (binding.recyclerView.adapter as OperationsAdapter).submitList(operations)
    }

    private fun onBlurViewHeightChanged(newHeight: Int) {
        binding.recyclerView.updatePadding(bottom = newHeight)
    }

    private fun setEmptyView() {
        binding.run {
            emptyView.image.setImageResource(R.drawable.image_empty_view_operations)
            emptyView.title.text = getString(R.string.historyEmptyViewTitle)
            emptyView.hint.text = getString(R.string.historyEmptyViewHint)
        }
    }

    companion object {

        const val EXTRA_INITIAL_DATE = "extra_initial_date"
        const val EXTRA_POSITION = "extra_position"

    }

}