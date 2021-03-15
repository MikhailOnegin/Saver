package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.*
import digital.fact.saver.domain.models.*
import digital.fact.saver.models.*
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.WalletsViewModel
import java.lang.IllegalArgumentException

class WalletsAdapter(
    private val onWalletClick: (Int) -> Unit,
    private val viewModel: WalletsViewModel,
    private val operationsViewModel: OperationsViewModel
) :
    ListAdapter<SourceItem, RecyclerView.ViewHolder>(
        SourceDiffUtilCallback()
    ) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SourcesActiveCount -> Sources.TYPE_COUNT_ACTIVE
            is Sources -> Sources.TYPE_SOURCE_ACTIVE
            is SourcesShowHidedWallets -> Sources.TYPE_BUTTON_SHOW
            is SourcesInactiveCount -> Sources.TYPE_COUNT_INACTIVE
            is SourcesInactive -> Sources.TYPE_SOURCE_INACTIVE
            is SourcesAddNewWallet -> Sources.TYPE_BUTTON_ADD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Sources.TYPE_COUNT_ACTIVE -> CountActiveViewHolder.getCountActiveVH(parent)
            Sources.TYPE_SOURCE_ACTIVE -> SourceActiveViewHolder.getSourceActiveVH(
                parent,
                onWalletClick
            )
            Sources.TYPE_BUTTON_SHOW -> ButtonShowViewHolder.getButtonShowVH(parent)
            Sources.TYPE_COUNT_INACTIVE -> CountInactiveViewHolder.getCountInactiveVH(parent)
            Sources.TYPE_BUTTON_ADD -> ButtonAddViewHolder.getButtonAddVH(parent)
            Sources.TYPE_SOURCE_INACTIVE -> SourceInactiveViewHolder.getSourceInactiveVH(
                parent,
                onWalletClick
            )
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    private class CountActiveViewHolder(
        private val binding: RvWalletActiveCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesActiveCount) {
            binding.summary.text = item.activeWalletsSum.toString()
        }

        companion object {
            fun getCountActiveVH(
                parent: ViewGroup
            ): CountActiveViewHolder {
                val binding = RvWalletActiveCountBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CountActiveViewHolder(binding)
            }
        }
    }

    private class CountInactiveViewHolder(
        private val binding: RvWalletInactiveCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesInactiveCount) {
            binding.summary.text = item.inactiveWalletsSum.toString()
        }

        companion object {
            fun getCountInactiveVH(
                parent: ViewGroup
            ): CountInactiveViewHolder {
                val binding = RvWalletInactiveCountBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return CountInactiveViewHolder(binding)
            }
        }
    }

    class ButtonAddViewHolder(
        private val binding: RvWalletAddBinding,
        private val parent: ViewGroup,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesAddNewWallet) {
            binding.root.setOnClickListener {
                parent.findNavController().navigate(R.id.walletAddFragment)
            }
        }

        companion object {
            fun getButtonAddVH(
                parent: ViewGroup
            ): ButtonAddViewHolder {
                val binding = RvWalletAddBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ButtonAddViewHolder(binding, parent)
            }
        }
    }

    class ButtonShowViewHolder(
        private val binding: RvShowInactiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SourcesShowHidedWallets,
            adapter: WalletsAdapter,
            viewModel: WalletsViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            binding.root.setOnClickListener {
                operationsViewModel.operations.value?.toOperations()?.let { //yunusov: переработать условие срабатывания
                    adapter.submitList(viewModel.sources.value?.toSources(it, item.isHidedShowed))
                }
                if (item.isHidedShowed) {
                    binding.title.setText(R.string.hideInactive)
                } else {
                    binding.title.setText(R.string.showInactive)
                }
                item.isHidedShowed = !item.isHidedShowed
            }
        }

        companion object {
            fun getButtonShowVH(
                parent: ViewGroup
            ): ButtonShowViewHolder {
                val binding = RvShowInactiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ButtonShowViewHolder(binding)
            }
        }
    }

    class SourceActiveViewHolder(
        private val binding: RvWalletActiveBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sources) {
            binding.title.text = item.name
            binding.subTitle.text = item.start_sum.toString()
            binding.root.setOnClickListener { onClick.invoke(item._id) }
        }

        companion object {
            fun getSourceActiveVH(
                parent: ViewGroup,
                onClick: (Int) -> Unit
            ): SourceActiveViewHolder {
                val binding = RvWalletActiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return SourceActiveViewHolder(binding, onClick)
            }
        }
    }

    class SourceInactiveViewHolder(
        private val binding: RvWalletInactiveBinding,
        private val onClick: (Int) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesInactive) {
            binding.title.text = item.name
            binding.subTitle.text = item.start_sum.toString()
            binding.root.setOnClickListener { onClick.invoke(item._id) }
        }

        companion object {
            fun getSourceInactiveVH(
                parent: ViewGroup,
                onClick: (Int) -> Unit
            ): SourceInactiveViewHolder {
                val binding = RvWalletInactiveBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return SourceInactiveViewHolder(binding, onClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItem(position)) {
            is SourcesActiveCount -> (holder as CountActiveViewHolder).bind(
                getItem(position) as SourcesActiveCount
            )
            is Sources -> (holder as SourceActiveViewHolder).bind(
                getItem(position) as Sources
            )
            is SourcesShowHidedWallets -> (holder as ButtonShowViewHolder).bind(
                getItem(position) as SourcesShowHidedWallets,
                this,
                viewModel,
                operationsViewModel
            )
            is SourcesInactive -> (holder as SourceInactiveViewHolder).bind(
                getItem(position) as SourcesInactive
            )
            is SourcesInactiveCount -> (holder as CountInactiveViewHolder).bind(
                getItem(position) as SourcesInactiveCount
            )
            is SourcesAddNewWallet -> (holder as ButtonAddViewHolder).bind(
                getItem(position) as SourcesAddNewWallet
            )
        }
    }

    class SourceDiffUtilCallback : DiffUtil.ItemCallback<SourceItem>() {
        override fun areItemsTheSame(oldItem: SourceItem, newItem: SourceItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SourceItem, newItem: SourceItem): Boolean {
            return false
        }
    }
}