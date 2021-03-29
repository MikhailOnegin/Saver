package digital.fact.saver.presentation.fragments.bank

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentBanksBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSavers
import digital.fact.saver.presentation.adapters.recycler.WalletsAdapter
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class BanksFragment : Fragment() {

    private lateinit var binding: FragmentBanksBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBanksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
        sourcesVM.updateSources()
        setListeners()
        setObservers()
        setDecoration()
    }

    private fun setDecoration() {
        binding.list.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)
                if (position < 0) return
                when ((parent.adapter as WalletsAdapter).currentList[position].itemType) {
                    Sources.TYPE_SAVER -> {
                        if (position == 0) {
                            outRect.top =
                                view.context?.resources?.getDimension(R.dimen._6dp)?.toInt() ?: 0
                        }
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._14dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_BUTTON_SHOW -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                    }
                }
            }
        })
    }

    private fun setListeners() {
        binding.addBank.setOnClickListener { findNavController().navigate(R.id.action_banksFragment_to_bankAddFragment) }
    }

    private fun setObservers() {
        sourcesVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(listUnsorted: List<Source>) {
        val list =
            listUnsorted.toSavers(
                operationsVM.operations.value?.toOperations()
            )
        if (list.isNullOrEmpty()) {
            binding.list.visibility = View.GONE
            binding.addBank.visibility = View.VISIBLE
            setEmptyMessage()
        } else {
            setEmptyMessage(true)
            binding.list.visibility = View.VISIBLE
            binding.addBank.visibility = View.GONE
        }
        val onActionClicked = { id: Long ->
            val bundle = Bundle()
            bundle.putLong(BANK_ID, id)
            findNavController().navigate(
                R.id.action_banksFragment_to_bankFragment,
                bundle
            )
        }
        val adapter = WalletsAdapter(onWalletClick = onActionClicked, sourcesVM, operationsVM)
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
            imageViewIcon.setImageResource(R.drawable.ic_empty_bank)
            textViewNotFoundData.setText(R.string.hint_empty_bank_title)
            textViewDescription.setText(R.string.hint_empty_bank_description)
        }
    }

    companion object {
        const val BANK_ID = "BANK_ID"
    }

}