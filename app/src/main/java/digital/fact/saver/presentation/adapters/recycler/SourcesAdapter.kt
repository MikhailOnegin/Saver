package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.App
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbSource
import digital.fact.saver.databinding.*
import digital.fact.saver.domain.models.*
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel
import digital.fact.saver.utils.formatToMoney
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class SourcesAdapter(
    private val onWalletClick: (Long) -> Unit,
    private val viewModel: SourcesViewModel,
    private val operationsViewModel: OperationsViewModel
) :
    ListAdapter<SourceItem, RecyclerView.ViewHolder>(
        SourceDiffUtilCallback()
    ) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SourcesActiveCount -> Source.TYPE_COUNT_ACTIVE
            is Source -> {
                if (getItem(position).itemType == Source.TYPE_SAVER || getItem(position).itemType == Source.TYPE_SAVER_HIDED) Source.TYPE_SAVER
                else Source.TYPE_SOURCE_ACTIVE
            }
            is SourcesShowHidedWallets -> Source.TYPE_BUTTON_SHOW
            is SourcesInactiveCount -> Source.TYPE_COUNT_INACTIVE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Source.TYPE_COUNT_ACTIVE -> CountActiveViewHolder.getCountActiveVH(parent)
            Source.TYPE_SOURCE_ACTIVE -> SourceActiveViewHolder.getSourceActiveVH(
                parent,
                onWalletClick
            )
            Source.TYPE_SAVER -> SourceSaverViewHolder.getSaverActiveVH(
                parent,
                onWalletClick
            )
            Source.TYPE_BUTTON_SHOW -> ButtonShowViewHolder.getButtonShowVH(parent)
            Source.TYPE_COUNT_INACTIVE -> CountInactiveViewHolder.getCountInactiveVH(parent)
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    private class CountActiveViewHolder(
        private val binding: RvWalletActiveCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourcesActiveCount) {
            binding.summary.text = item.activeWalletsSum.formatToMoney()
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
            binding.summary.text = item.inactiveWalletsSum.formatToMoney()
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
            adapter: SourcesAdapter,
            viewModel: SourcesViewModel,
            operationsViewModel: OperationsViewModel
        ) {
            binding.root.setOnClickListener {
                when (item.destinationSource) {
                    Source.Companion.Destination.WALLETS_ACTIVE -> workOnActive(
                        item,
                        adapter,
                        viewModel,
                        operationsViewModel
                    )
                    Source.Companion.Destination.WALLETS_INACTIVE -> workOnInactive(
                        item,
                        adapter,
                        viewModel,
                        operationsViewModel
                    )
                    Source.Companion.Destination.SAVERS -> workOnSavers(
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
            adapter: SourcesAdapter,
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
            adapter: SourcesAdapter,
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
            adapter: SourcesAdapter,
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
        fun bind(item: Source) {
            if (item.visibility == DbSource.Visibility.INVISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hided)
            binding.title.text = item.name
            binding.subTitle.text = getCurrentSum(item)
            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        private fun getCurrentSum(item: Source): CharSequence {
            return if (item.currentSum == 0L && item.startSum != 0L) {
                item.startSum
            } else {
                item.currentSum
            }.formatToMoney()
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
        private val binding: RvSaverBinding,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val builder = StringBuilder()
        private val fullDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val screenContentPadding = App.getInstance().resources.getDimension(R.dimen.screenContentPadding).toInt()

        fun bind(item: Source) {
            if (item.visibility == DbSource.Visibility.VISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item)
            if (item.visibility == DbSource.Visibility.INVISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hided)

            binding.title.text = item.name
            binding.subTitle.text = item.currentSum.formatToMoney()

            binding.intent.text = getAimText(item)
            if (item.aimSum > 0L) {
                binding.indicator.root.visibility = View.VISIBLE
                binding.intent.visibility = View.VISIBLE
            } else {
                binding.indicator.root.visibility = View.INVISIBLE
                binding.intent.visibility = View.GONE
            }

            setProgressBarValue(item)

            binding.root.setOnClickListener { onClick.invoke(item.id) }
        }

        private fun getAimText(saver: Source): String {
            builder.clear()
            builder.append(App.getInstance().getString(R.string.rvSaverSaveHint1))
            builder.append(" ")
            builder.append(saver.aimSum.formatToMoney())
            builder.append('\n')
            builder.append(App.getInstance().getString(R.string.rvSaverSaveHint2))
            builder.append(" ")
            builder.append(fullDateFormat.format(Date(saver.aimDate)))
            return builder.toString()
        }

        private fun setProgressBarValue(item: Source) {
            if (item.aimSum <= 0L) binding.intentProgress.visibility = View.INVISIBLE
            else {
                if (item.currentSum > 0L) {
                    val progress = ((item.currentSum.toFloat() / item.aimSum) * 100).toInt()
                    binding.intentProgress.progress = progress
                } else {
                    binding.intentProgress.progress = 0
                }
                binding.intentProgress.visibility = View.VISIBLE
            }
            val progressDrawable = ResourcesCompat.getDrawable(
                App.getInstance().resources,
                R.drawable.background_progress_bar,
                null)
            val progressHiddenDrawable = ResourcesCompat.getDrawable(
                App.getInstance().resources,
                R.drawable.background_progress_bar_hidden,
                null)
            if (item.visibility == DbSource.Visibility.VISIBLE.value)
                binding.intentProgress.progressDrawable = progressDrawable
            else binding.intentProgress.progressDrawable = progressHiddenDrawable
        }

        companion object {
            fun getSaverActiveVH(
                parent: ViewGroup,
                onClick: (Long) -> Unit
            ): SourceSaverViewHolder {
                val binding = RvSaverBinding.inflate(
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
            is Source -> {
                if (getItem(position).itemType == Source.TYPE_SAVER || getItem(position).itemType == Source.TYPE_SAVER_HIDED)
                    (holder as SourceSaverViewHolder).bind(getItem(position) as Source)
                else (holder as SourceActiveViewHolder).bind(getItem(position) as Source)
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