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
import digital.fact.saver.domain.models.Source
import digital.fact.saver.models.Sources
import digital.fact.saver.models.toOperations
import digital.fact.saver.models.toSources
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

    private fun setDecoration() { //yunusov: изменить под копилки
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
                    Sources.TYPE_SOURCE_ACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._14dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_BUTTON_ADD -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_COUNT_ACTIVE -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._6dp)?.toInt() ?: 0
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._23dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_COUNT_INACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._23dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_BUTTON_SHOW -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                    }
                    Sources.TYPE_SOURCE_INACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._14dp)?.toInt() ?: 0
                    }
                }
            }
        })
    }

    private fun setListeners() {
        binding.addBank.setOnClickListener { findNavController().navigate(R.id.walletAddFragment) }
    }

    private fun setObservers() {
        sourcesVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(listUnsorted: List<Source>) {
        val list =
            listUnsorted.toSources(operationsVM.operations.value?.toOperations(), isShowed = false, onlySavers = true)
        if (list.isNullOrEmpty()) {
            binding.list.visibility = View.GONE
            binding.addBank.visibility = View.VISIBLE
        } else {
            binding.list.visibility = View.VISIBLE
            binding.addBank.visibility = View.GONE
        }
        val onActionClicked = { id: Int ->
            val bundle = Bundle()
            bundle.putInt(BANK_ID, id)
            findNavController().navigate(
                R.id.action_walletsFragment_to_walletFragment,
                bundle
            )
        }
        val adapter = WalletsAdapter(onWalletClick = onActionClicked, sourcesVM, operationsVM)
        binding.list.adapter = adapter
        adapter.submitList(list)
    }

    companion object {
        const val BANK_ID = "BANK_ID"
    }

}