package digital.fact.saver.presentation.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.databinding.DialogCurrentPlansBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.recycler.PlansCurrentAdapter
import digital.fact.saver.presentation.fragments.history.HistoryViewModel
import digital.fact.saver.utils.LinearRvItemDecorations

class CurrentPlansDialog(
        private val onPlanClicked: (Int, Long, Long, String) -> Unit
): BottomSheetDialogFragment() {

    private lateinit var binding: DialogCurrentPlansBinding
    private lateinit var historyVM: HistoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCurrentPlansBinding.inflate(inflater, container, false)
        binding.recyclerView.addItemDecoration(LinearRvItemDecorations(
                sideMarginsDimension = R.dimen.screenContentPadding,
                marginBetweenElementsDimension = R.dimen.verticalMarginBetweenListElements
        ))
        binding.recyclerView.adapter = PlansCurrentAdapter(
                onCurrentPlanClickedInHistory = onPlanClicked,
                currentPlansDialog = this
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        historyVM = ViewModelProvider(requireActivity())[HistoryViewModel::class.java]
        historyVM.currentPlans.observe(viewLifecycleOwner) { onCurrentPlansChanged(it) }
    }

    private fun onCurrentPlansChanged(plans: List<Plan>) {
        (binding.recyclerView.adapter as PlansCurrentAdapter).submitList(plans)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

}