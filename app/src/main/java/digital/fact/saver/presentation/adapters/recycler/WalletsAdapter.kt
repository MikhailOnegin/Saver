package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.*
import digital.fact.saver.domain.models.Source
import digital.fact.saver.models.*
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.toStringFormatter
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
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    private class CountActiveViewHolder(
        private val binding: RvWalletActiveCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesActiveCount) {
            binding.summary.text = item.activeWalletsSum.toStringFormatter()
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
            binding.summary.text = item.inactiveWalletsSum.toStringFormatter()
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

    class ButtonShowViewHolder(
        private val binding: RvShowInactiveBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: SourcesShowHidedWallets,
            adapter: WalletsAdapter,
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            binding.root.setOnClickListener {
                when (item.destinationSource) {
                    Sources.Companion.Destination.WALLETS_ACTIVE -> workOnActive(
                        item,
                        adapter,
                        viewModel,
                        operationsViewModel
                    )
                    Sources.Companion.Destination.WALLETS_INACTIVE -> workOnInactive(
                        item,
                        adapter,
                        viewModel,
                        operationsViewModel
                    )
                    Sources.Companion.Destination.SAVERS -> workOnSavers(
                        item,
                        adapter,
                        viewModel,
                        operationsViewModel
                    )
                }
            }
        }

        private fun workOnActive(
            item: SourcesShowHidedWallets,
            adapter: WalletsAdapter,
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            if (item.isHidedShowed) {
                binding.title.setText(R.string.hideInactive)
                adapter.submitList(
                    viewModel.sources.value?.toActiveSources(
                        operationsViewModel.operations.value?.toOperations(),
                        false
                    )
                )
            } else {
                binding.title.setText(R.string.showInactive)
                adapter.submitList(
                    viewModel.sources.value?.toActiveSources(
                        operationsViewModel.operations.value?.toOperations(),
                        true
                    )
                )
            }
        }

        private fun workOnInactive(
            item: SourcesShowHidedWallets,
            adapter: WalletsAdapter,
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            if (item.isHidedShowed) {
                binding.title.setText(R.string.hideInactive)
                adapter.submitList(
                    viewModel.sources.value?.toInactiveSources(
                        operationsViewModel.operations.value?.toOperations(),
                        false
                    )
                )
            } else {
                binding.title.setText(R.string.showInactive)
                adapter.submitList(
                    viewModel.sources.value?.toInactiveSources(
                        operationsViewModel.operations.value?.toOperations(),
                        true
                    )
                )
            }
        }

        private fun workOnSavers(
            item: SourcesShowHidedWallets,
            adapter: WalletsAdapter,
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            if (item.isHidedShowed) {
                binding.title.setText(R.string.hideInactive)
                adapter.submitList(
                    viewModel.sources.value?.toSavers(
                        operationsViewModel.operations.value?.toOperations(),
                        false
                    )
                )
            } else {
                binding.title.setText(R.string.showInactive)
                adapter.submitList(
                    viewModel.sources.value?.toSavers(
                        operationsViewModel.operations.value?.toOperations(),
                        true
                    )
                )
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
            if (item.visibility == Source.SourceVisibility.INVISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hided)
            binding.title.text = item.name
            binding.subTitle.text = getCurrentSum(item)
            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        private fun getCurrentSum(item: Sources): CharSequence {
            return if (item.currentSum == 0L && item.startSum != 0L) {
                item.startSum
            } else {
                item.currentSum
            }.toStringFormatter()
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
            if (item.visibility == Source.SourceVisibility.INVISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hided)
            binding.title.text = item.name
            binding.subTitle.text = getCurrentSum(item)
            if (item.aimSum != 0L) setProgressVariable(item)
            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        private fun getCurrentSum(item: Sources): CharSequence {
            return if (item.currentSum == 0L && item.startSum != 0L) {
                item.startSum
            } else {
                item.currentSum
            }.toStringFormatter()
        }

        private fun setProgressVariable(item: Sources) {
            binding.indicator.root.visibility = View.VISIBLE
            binding.blur.visibility = View.VISIBLE
            binding.intent.text = item.aimSum.toStringFormatter()
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
                if (getItem(position).itemType == Sources.TYPE_SAVER || getItem(position).itemType == Sources.TYPE_SAVER_HIDED)
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