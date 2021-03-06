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
import digital.fact.saver.presentation.viewmodels.AbstractSourcesViewModel
import digital.fact.saver.utils.formatToMoney
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*

class SourcesAdapter(
    private val sourcesVM: AbstractSourcesViewModel,
    private val onSourceClicked: (Long) -> Unit
) : ListAdapter<SourceItem, RecyclerView.ViewHolder>(SourceItemDiffUtilCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (val sourceItem = getItem(position)) {
            is SourceHeader -> TYPE_HEADER
            is Source -> {
                when (sourceItem.type) {
                    DbSource.Type.SAVER.value -> TYPE_SAVER
                    DbSource.Type.ACTIVE.value, DbSource.Type.INACTIVE.value -> TYPE_WALLET
                    else -> throw IllegalArgumentException("Illegal source type.")
                }
            }
            is SourcesVisibilitySwitcher -> TYPE_VISIBILITY_SWITCHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> HeaderVH.getViewHolder(parent)
            TYPE_WALLET -> WalletVH.getViewHolder(parent, onSourceClicked)
            TYPE_SAVER -> SaverVH.getViewHolder(parent, onSourceClicked)
            TYPE_VISIBILITY_SWITCHER -> VisibilitySwitcherVH.getButtonShowVH(parent)
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sourceItem = getItem(position)
        when (getItemViewType(position)) {
            TYPE_HEADER -> (holder as HeaderVH).bind(sourceItem as SourceHeader)
            TYPE_WALLET -> (holder as WalletVH).bind(sourceItem as Source)
            TYPE_SAVER -> (holder as SaverVH).bind(sourceItem as Source)
            TYPE_VISIBILITY_SWITCHER ->
                (holder as VisibilitySwitcherVH)
                    .bind(sourceItem as SourcesVisibilitySwitcher, sourcesVM)
            else -> throw IllegalArgumentException("Wrong source view holder type.")
        }
    }

    private class HeaderVH(
        private val binding: RvSourceHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(header: SourceHeader) {
            binding.run {
                val context = App.getInstance()
                when (header.type) {
                    DbSource.Type.ACTIVE.value -> {
                        title.text = context.getString(R.string.headerWalletsActive)
                        title.setTextColor(ContextCompat.getColor(context, R.color.textColorBlue))
                        image.setImageResource(R.drawable.ic_wallet_active)
                    }
                    DbSource.Type.INACTIVE.value -> {
                        title.text = context.getString(R.string.headerWalletsInactive)
                        title.setTextColor(ContextCompat.getColor(context, R.color.textColorHint))
                        image.setImageResource(R.drawable.ic_wallet_inactive)
                    }
                    DbSource.Type.SAVER.value -> {
                        title.text = context.getString(R.string.headerSavers)
                        title.setTextColor(ContextCompat.getColor(context, R.color.textColorGold))
                        image.setImageResource(R.drawable.ic_saver)
                    }
                }
                sum.text = header.sum.formatToMoney()
            }
        }

        companion object {
            fun getViewHolder(
                parent: ViewGroup
            ): HeaderVH {
                val binding = RvSourceHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return HeaderVH(binding)
            }
        }

    }

    class VisibilitySwitcherVH(
        private val binding: RvVisibilitySwitcherBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(switcher: SourcesVisibilitySwitcher, sourcesVM: AbstractSourcesViewModel) {
            if (switcher.invisibleAreShown) binding.title.setText(R.string.hideInvisible)
            else binding.title.setText(R.string.showInvisible)
            binding.root.setOnClickListener { sourcesVM.switchVisibilityMode() }
        }

        companion object {

            fun getButtonShowVH(parent: ViewGroup): VisibilitySwitcherVH {
                val binding = RvVisibilitySwitcherBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return VisibilitySwitcherVH(binding)
            }

        }

    }

    class WalletVH(
        private val binding: RvSourceBinding,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wallet: Source) {
            binding.run {
                indicator.root.visibility = View.INVISIBLE
                aim.visibility = View.INVISIBLE
                aimProgress.visibility = View.INVISIBLE
                if (wallet.visibility == DbSource.Visibility.VISIBLE.value)
                    mainContainer.background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.background_item)
                if (wallet.visibility == DbSource.Visibility.INVISIBLE.value)
                    mainContainer.background =
                        ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hidden)
                title.text = wallet.name
                subTitle.text = wallet.currentSum.formatToMoney(true)
                root.setOnClickListener { onClick.invoke(wallet.id) }
            }
        }

        companion object {

            fun getViewHolder(
                parent: ViewGroup,
                onClick: (Long) -> Unit
            ): WalletVH {
                val binding = RvSourceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return WalletVH(binding, onClick)
            }

        }

    }

    class SaverVH(
        private val binding: RvSourceBinding,
        private val onClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val builder = StringBuilder()
        private val fullDateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        val screenContentPadding =
            App.getInstance().resources.getDimension(R.dimen.screenContentPadding).toInt()

        fun bind(saver: Source) {
            if (saver.visibility == DbSource.Visibility.VISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item)
            if (saver.visibility == DbSource.Visibility.INVISIBLE.value)
                binding.mainContainer.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.background_item_hidden)

            binding.title.text = saver.name
            binding.subTitle.text = saver.currentSum.formatToMoney()

            binding.aim.text = getAimText(saver)
            if (saver.aimSum > 0L) {
                binding.indicator.root.visibility = View.VISIBLE
                binding.aim.visibility = View.VISIBLE
            } else {
                binding.indicator.root.visibility = View.INVISIBLE
                binding.aim.visibility = View.GONE
            }

            setProgressBarValue(saver)

            binding.root.setOnClickListener { onClick.invoke(saver.id) }
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
            if (item.aimSum <= 0L) binding.aimProgress.visibility = View.INVISIBLE
            else {
                if (item.currentSum > 0L) {
                    val progress = ((item.currentSum.toFloat() / item.aimSum) * 100).toInt()
                    binding.aimProgress.progress = progress
                } else {
                    binding.aimProgress.progress = 0
                }
                binding.aimProgress.visibility = View.VISIBLE
            }
            val progressDrawable = ResourcesCompat.getDrawable(
                App.getInstance().resources,
                R.drawable.background_progress_bar,
                null
            )
            val progressHiddenDrawable = ResourcesCompat.getDrawable(
                App.getInstance().resources,
                R.drawable.background_progress_bar_hidden,
                null
            )
            if (item.visibility == DbSource.Visibility.VISIBLE.value)
                binding.aimProgress.progressDrawable = progressDrawable
            else binding.aimProgress.progressDrawable = progressHiddenDrawable
        }

        companion object {

            fun getViewHolder(parent: ViewGroup, onClick: (Long) -> Unit): SaverVH {
                val binding = RvSourceBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return SaverVH(binding, onClick)
            }

        }

    }

    class SourceItemDiffUtilCallback : DiffUtil.ItemCallback<SourceItem>() {

        override fun areItemsTheSame(oldItem: SourceItem, newItem: SourceItem): Boolean {
            if (oldItem is SourceHeader && newItem is SourceHeader) return true
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: SourceItem, newItem: SourceItem): Boolean {
            return oldItem == newItem
        }

    }

    companion object {

        const val TYPE_HEADER = 1
        const val TYPE_WALLET = 2
        const val TYPE_SAVER = 3
        const val TYPE_VISIBILITY_SWITCHER = 4

    }

}