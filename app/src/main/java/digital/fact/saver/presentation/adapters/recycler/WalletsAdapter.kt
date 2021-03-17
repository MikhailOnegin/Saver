package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.*
import digital.fact.saver.models.*
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import java.lang.IllegalArgumentException

class WalletsAdapter(
    private val onWalletClick: (Long) -> Unit,
    private val viewModel: SourcesViewModel,
    private val operationsViewModel: OperationsViewModel
) :
    ListAdapter<SourceItem, RecyclerView.ViewHolder>(
        SourceDiffUtilCallback()
    ) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SourcesActiveCount -> Sources.TYPE_COUNT_ACTIVE
            is Sources -> {
                if (getItem(position).itemType == Sources.TYPE_SAVER) Sources.TYPE_SAVER
                else Sources.TYPE_SOURCE_ACTIVE
            }
            is SourcesShowHidedWallets -> Sources.TYPE_BUTTON_SHOW
            is SourcesInactiveCount -> Sources.TYPE_COUNT_INACTIVE
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
            Sources.TYPE_SAVER -> SourceSaverViewHolder.getSaverActiveVH(
                parent,
                onWalletClick
            )
            Sources.TYPE_BUTTON_SHOW -> ButtonShowViewHolder.getButtonShowVH(parent)
            Sources.TYPE_COUNT_INACTIVE -> CountInactiveViewHolder.getCountInactiveVH(parent)
            Sources.TYPE_BUTTON_ADD -> ButtonAddViewHolder.getButtonAddVH(parent)
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
            when (item.destinationSource) {
                Sources.Companion.Destination.ADD_SAVER -> binding.addWallet.setText(R.string.add_bank)
                else -> binding.addWallet.setText(R.string.add_wallet)
            }
            binding.root.setOnClickListener {
                parent.findNavController().navigate(
                    when (item.destinationSource) {
                        Sources.Companion.Destination.ADD_SAVER -> R.id.action_banksFragment_to_bankAddFragment
                        else -> R.id.action_walletsFragment_to_walletAddFragment
                    }
                )
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
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            val onlySavers = item.destinationSource == Sources.Companion.Destination.ADD_SAVER
            binding.root.setOnClickListener {
                if (item.isHidedShowed) {
                    binding.title.setText(R.string.hideInactive)
                    adapter.submitList(
                        viewModel.sources.value?.toSources(
                            operationsViewModel.operations.value?.toOperations(),
                            false,
                            onlySavers = onlySavers
                        )
                    )
                } else {
                    binding.title.setText(R.string.showInactive)
                    adapter.submitList(
                        viewModel.sources.value?.toSources(
                            operationsViewModel.operations.value?.toOperations(),
                            true,
                            onlySavers = onlySavers
                        )
                    )
                }
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
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sources) {
            binding.title.text = item.name
            binding.subTitle.text = item.startSum.toString()
            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        companion object {
            fun getSourceActiveVH(
                parent: ViewGroup,
                onClick: (Long) -> Unit
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

    class SourceSaverViewHolder(
        private val binding: RvBankItemBinding,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Sources) {
            binding.title.text = item.name
            binding.subTitle.text = item.currentSum.toString()
            if (item.aimSum != 0L) setProgressVariable(item)
            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        private fun setProgressVariable(item: Sources) {
            binding.indicator.root.visibility = View.VISIBLE
            binding.blur.visibility = View.VISIBLE
            binding.intent.text = item.aimSum.toString()
            if (item.currentSum > 0L) {
                val currentProgress = (item.currentSum.toFloat() / item.aimSum) * 100
                binding.intentProgress.progress = if (currentProgress >= 100) {
                    100
                } else if (currentProgress <= 0) {
                    0
                } else {
                    currentProgress
                }.toInt()
            } else {
                binding.intentProgress.progress = 0
            }
        }

        companion object {
            fun getSaverActiveVH(
                parent: ViewGroup,
                onClick: (Long) -> Unit
            ): SourceSaverViewHolder {
                val binding = RvBankItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return SourceSaverViewHolder(binding, onClick)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItem(position)) {
            is SourcesActiveCount -> (holder as CountActiveViewHolder).bind(
                getItem(position) as SourcesActiveCount
            )
            is Sources -> {
                if (getItem(position).itemType == Sources.TYPE_SAVER)
                    (holder as SourceSaverViewHolder).bind(getItem(position) as Sources)
                else (holder as SourceActiveViewHolder).bind(getItem(position) as Sources)
            }
            is SourcesShowHidedWallets -> (holder as ButtonShowViewHolder).bind(
                getItem(position) as SourcesShowHidedWallets,
                this,
                viewModel,
                operationsViewModel
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
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: SourceItem, newItem: SourceItem): Boolean {
            return oldItem == newItem
        }
    }
}