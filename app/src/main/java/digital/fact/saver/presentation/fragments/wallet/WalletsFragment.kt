package digital.fact.saver.presentation.fragments.wallet

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletsBinding
import digital.fact.saver.data.database.dto.Source
import digital.fact.saver.domain.models.Sources
import digital.fact.saver.domain.models.toOperations
import digital.fact.saver.domain.models.toSources
import digital.fact.saver.presentation.adapters.recycler.WalletsAdapter
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class WalletsFragment : Fragment() {

    private lateinit var binding: FragmentWalletsBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsBinding.inflate(inflater, container, false)
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

    private fun setListeners() {
        binding.addWallet.setOnClickListener { findNavController().navigate(R.id.action_walletsFragment_to_walletAddFragment) }
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

    private fun setObservers() {
        sourcesVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it) })
    }

    private fun onSourcesChanged(listUnsorted: List<Source>) {
        val list =
            listUnsorted.toSources(operationsVM.operations.value?.toOperations(), isShowed = false)
        if (list.isNullOrEmpty()) {
            binding.list.visibility = View.GONE
            binding.addWallet.visibility = View.VISIBLE
            setEmptyMessage()
        } else {
            setEmptyMessage(true)
            binding.list.visibility = View.VISIBLE
            binding.addWallet.visibility = View.GONE
        }
        val onActionClicked = { id: Long ->
            val bundle = Bundle()
            bundle.putLong(WALLET_ID, id)
            findNavController().navigate(
                R.id.action_walletsFragment_to_walletFragment,
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
            imageViewIcon.setImageResource(R.drawable.ic_wallet_empty)
            textViewNotFoundData.setText(R.string.hint_empty_wallet_title)
            textViewDescription.setText(R.string.hint_empty_wallet_description)
        }
    }

    companion object {
        const val WALLET_ID = "WALLET_ID"
    }

}

