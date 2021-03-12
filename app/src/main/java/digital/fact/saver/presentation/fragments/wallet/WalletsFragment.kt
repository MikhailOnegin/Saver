package digital.fact.saver.presentation.fragments.wallet

import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentWalletsBinding
import digital.fact.saver.databinding.RvWalletInactiveCountBinding
import digital.fact.saver.domain.models.Source
import digital.fact.saver.domain.models.SourceActiveCount
import digital.fact.saver.domain.models.SourceItem
import digital.fact.saver.domain.models.toSources
import digital.fact.saver.presentation.adapters.recycler.WalletsAdapter
import digital.fact.saver.presentation.viewmodels.WalletsViewModel

class WalletsFragment : Fragment() {

    private lateinit var binding: FragmentWalletsBinding
    private lateinit var walletsVM: WalletsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWalletsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        walletsVM = ViewModelProvider(requireActivity())[WalletsViewModel::class.java]
        walletsVM.updateSources()
        setObservers()
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
                when ((parent.adapter as WalletsAdapter).currentList[position].type) {
                    Source.TYPE_SOURCE_ACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._14dp)?.toInt() ?: 0
                    }
                    Source.TYPE_BUTTON_ADD -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._13dp)?.toInt() ?: 0
                    }
                    Source.TYPE_COUNT_ACTIVE -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._6dp)?.toInt() ?: 0
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._23dp)?.toInt() ?: 0
                    }
                    Source.TYPE_COUNT_INACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._23dp)?.toInt() ?: 0
                    }
                    Source.TYPE_BUTTON_SHOW -> {
                        outRect.top =
                            view.context?.resources?.getDimension(R.dimen._27dp)?.toInt() ?: 0
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._27dp)?.toInt() ?: 0
                    }
                    Source.TYPE_SOURCE_INACTIVE -> {
                        outRect.bottom =
                            view.context?.resources?.getDimension(R.dimen._14dp)?.toInt() ?: 0
                    }
                }
            }
        })
    }

    private fun setObservers() {
        walletsVM.sources.observe(viewLifecycleOwner, { onSourcesChanged(it.toSources()) })
    }

    private fun onSourcesChanged(list: List<SourceItem>) {
        val onActionClicked = { id: Int ->
//            val bundle = Bundle()
//            bundle.putInt(WALLET_ID, id)
//            findNavController().navigate(
//                R.id.action_walletsFragment_to_detailWalletFragment,
//                bundle
//            )
        }
        val adapter = WalletsAdapter(onWalletClick = onActionClicked, walletsVM)
        binding.list.adapter = adapter
        adapter.submitList(list)
        setDecoration()
    }

    companion object {
        const val WALLET_ID = "WALLET_ID"
    }

}

