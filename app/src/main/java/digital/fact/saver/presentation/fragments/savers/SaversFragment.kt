package digital.fact.saver.presentation.fragments.savers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.databinding.FragmentSaversBinding
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSavers
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.adapters.recycler.SourcesAdapter
import digital.fact.saver.presentation.fragments.savers.saver.SaverFragment
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class SaversFragment : Fragment() {

    private lateinit var binding: FragmentSaversBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSaversBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        sourcesVM.getAllSources()
        setListeners()
        setObservers()
        //setDecoration()
    }

    /*private fun setDecoration() {
        binding.list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val sideMargins = App.getInstance()
                    .resources.getDimension(R.dimen.screenContentPadding).toInt()
                val verticalMargins = App.getInstance()
                    .resources.getDimension(R.dimen.verticalMarginBetweenListElements).toInt()
                val position = parent.getChildAdapterPosition(view)
                val size = (parent.adapter as SourcesAdapter).itemCount
                if (position < 0) return
                outRect.set(
                    sideMargins,
                    if (position == 0) sideMargins else 0,
                    sideMargins,
                    if (position == size - 1) sideMargins else verticalMargins
                )
                *//*
                when ((parent.adapter as SourcesAdapter).currentList[position].itemType) {
                    Sources.TYPE_SAVER -> {
                        outRect.set(
                            sideMargins,
                            if (position == 0) sideMargins else 0,
                            sideMargins,
                            if (position == size - 1) sideMargins else verticalMargins
                        )
                    }
                    *//**//*Sources.TYPE_BUTTON_SHOW -> {
                        outRect.set(
                            sideMargins,
                            if (position == 0) sideMargins else 0,
                            sideMargins,
                            if (position == size - 1) sideMargins else verticalMargins
                        )
                    }*//**//*
                }*//*
            }
        })
    }*/

    private fun setListeners() {
        binding.toolbar.setOnMenuItemClickListener(onMenuItemClicked)
        binding.toolbar.setNavigationOnClickListener { (requireActivity() as MainActivity).openDrawer() }
    }

    private val onMenuItemClicked: (MenuItem) -> Boolean = {
        when (it.itemId) {
            R.id.add -> findNavController().navigate(R.id.action_banksFragment_to_bankAddFragment)
        }
        true
    }

    private fun setObservers() {
        sourcesVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(listUnsorted: List<Source>) {
        val list = listUnsorted.toSavers(operationsVM.operations.value?.toOperations())
        if (list.isNullOrEmpty()) {
            binding.list.visibility = View.GONE
            setEmptyMessage()
        } else {
            setEmptyMessage(true)
            binding.list.visibility = View.VISIBLE
        }
        val onActionClicked = { id: Long ->
            val bundle = Bundle()
            bundle.putLong(SaverFragment.EXTRA_SAVER_ID, id)
            findNavController().navigate(R.id.action_banksFragment_to_bankFragment, bundle)
        }
        val adapter = SourcesAdapter(onWalletClick = onActionClicked, sourcesVM, operationsVM)
        binding.list.adapter = adapter
        adapter.submitList(list)
    }

    private fun setEmptyMessage(hideAll: Boolean = false) {
        if (hideAll) {
            binding.empty.root.visibility = View.GONE
            return
        }
        binding.empty.apply {
            root.visibility = View.VISIBLE
            image.setImageResource(R.drawable.ic_empty_bank)
            title.setText(R.string.bankEmptyTitle)
            hint.setText(R.string.bankEmptyDescription)
        }
    }

}