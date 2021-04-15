package digital.fact.saver.presentation.fragments.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentAboutAppHistoryBinding
import digital.fact.saver.domain.models.AppHistory
import digital.fact.saver.presentation.adapters.recycler.AboutAppHistoryAdapter
import digital.fact.saver.utils.LinearRvItemDecorations

class AboutAppHistoryFragment: Fragment() {

    private lateinit var binding: FragmentAboutAppHistoryBinding
    private lateinit var historyAdapter: AboutAppHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutAppHistoryBinding.inflate(
            inflater, container,false
        )
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        initializedAdapters()
        binding.recyclerHistory.addItemDecoration(
            LinearRvItemDecorations(
            sideMarginsDimension = R.dimen.screenContentPadding,
            marginBetweenElementsDimension = R.dimen.verticalMarginBetweenListElements
        )
        )
        // for test
        val itemsTest = arrayListOf(
            AppHistory("v 1.1.1", "Сделали невероятно много\n" +
                    "Сделали невероятно много\n" +
                    "Сделали невероятно много"),
            AppHistory("v1.2.1", "Сделали ещё больше\n" +
                    "Сделали ещё больше\n" +
                    "Сделали ещё больше"),
            AppHistory("v 1.3.1", "Сделали ещё больше\n" +
                    "Сделали ещё больше\n" +
                    "Сделали ещё больше")
            )
        historyAdapter.submitList(itemsTest)
    }

    private fun initializedAdapters() {
        historyAdapter = AboutAppHistoryAdapter()
        binding.recyclerHistory.adapter = historyAdapter
    }
}