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
import java.lang.IllegalArgumentException

class WalletsAdapter(private val onWalletClick: (Int) -> Unit) :
    ListAdapter<SourceItem, RecyclerView.ViewHolder>(
        SourceDiffUtilCallback()
    ) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is SourceActiveCount -> Source.TYPE_COUNT_ACTIVE
            is SourceActive -> Source.TYPE_SOURCE_ACTIVE
            is SourceShowInactiveWallets -> Source.TYPE_BUTTON_SHOW
            is SourceInactiveCount -> Source.TYPE_COUNT_INACTIVE
            is SourceInactive -> Source.TYPE_SOURCE_INACTIVE
            is SourceAddNewWallet -> Source.TYPE_BUTTON_ADD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Source.TYPE_COUNT_ACTIVE -> CountActiveViewHolder.getCountActiveVH(parent)
            Source.TYPE_SOURCE_ACTIVE -> SourceActiveViewHolder.getSourceActiveVH(
                parent,
                onWalletClick
            )
            Source.TYPE_BUTTON_SHOW -> ButtonShowViewHolder.getButtonShowVH(parent)
            Source.TYPE_COUNT_INACTIVE -> CountInactiveViewHolder.getCountInactiveVH(parent)
            Source.TYPE_SOURCE_INACTIVE -> SourceInactiveViewHolder.getSourceInactiveVH(
                parent,
                onWalletClick
            )
            Source.TYPE_BUTTON_ADD -> ButtonAddViewHolder.getButtonAddVH(parent)
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    private class CountActiveViewHolder(
        private val binding: RvWalletActiveCountBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SourceActiveCount) {
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
        fun bind(item: SourceInactiveCount) {
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
        fun bind(item: SourceAddNewWallet) {
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
        fun bind(item: SourceShowInactiveWallets) {}

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
        fun bind(item: SourceActive) {
            binding.title.text = item.name
            binding.subTitle.text = item.start_sum.toString()
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
        fun bind(item: SourceInactive) {
            binding.title.text = item.name
            binding.subTitle.text = item.start_sum.toString()
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
            is SourceActiveCount -> (holder as CountActiveViewHolder).bind(
                getItem(position) as SourceActiveCount
            )
            is SourceActive -> (holder as SourceActiveViewHolder).bind(
                getItem(position) as SourceActive
            )
            is SourceShowInactiveWallets -> (holder as ButtonShowViewHolder).bind(
                getItem(position) as SourceShowInactiveWallets
            )
            is SourceInactiveCount -> (holder as CountInactiveViewHolder).bind(
                getItem(position) as SourceInactiveCount
            )
            is SourceInactive -> (holder as SourceInactiveViewHolder).bind(
                getItem(position) as SourceInactive
            )
            is SourceAddNewWallet -> (holder as ButtonAddViewHolder).bind(
                getItem(position) as SourceAddNewWallet
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