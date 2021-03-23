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
import digital.fact.saver.models.Operation
import digital.fact.saver.presentation.activity.MainViewModel
import digital.fact.saver.presentation.adapters.recycler.OperationsAdapter
import digital.fact.saver.utils.LinearRvItemDecorations

class OperationsFragment : Fragment() {

    private lateinit var binding: FragmentOperationsBinding
    private lateinit var operationsVM: OperationsViewModel
    private lateinit var mainVM: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOperationsBinding.inflate(inflater, container, false)
        binding.recyclerView.addItemDecoration(LinearRvItemDecorations(
                sideMarginsDimension = R.dimen.smallMargin,
                marginBetweenElementsDimension = R.dimen.smallMargin
        ))
        binding.recyclerView.adapter = OperationsAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        operationsVM = ViewModelProvider(this)[OperationsViewModel::class.java]
        mainVM = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        setObservers()
    }

    private fun setObservers() {
        operationsVM.operations.observe(viewLifecycleOwner) { onOperationsChanged(it) }
        mainVM.historyBlurViewHeight.observe(viewLifecycleOwner) {onBlurViewHeightChanged(it)}
    }

    private fun onOperationsChanged(operations: List<Operation>?) {
        (binding.recyclerView.adapter as OperationsAdapter).submitList(operations)
    }

    private fun onBlurViewHeightChanged(newHeight: Int) {
        binding.recyclerView.updatePadding(bottom = newHeight)
    }

}