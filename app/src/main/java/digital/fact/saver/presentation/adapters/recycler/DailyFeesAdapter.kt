package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.App
import digital.fact.saver.R
import digital.fact.saver.databinding.RvDailyFeeBinding
import digital.fact.saver.domain.models.DailyFee
import digital.fact.saver.utils.WordEnding
import digital.fact.saver.utils.formatToMoney
import digital.fact.saver.utils.getWordEndingType

class DailyFeesAdapter :
    ListAdapter<DailyFee, DailyFeesAdapter.DailyFeeVH>(DailyFeesDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyFeeVH {
        return DailyFeeVH.getViewHolder(parent)
    }

    override fun onBindViewHolder(holder: DailyFeeVH, position: Int) {
        holder.bind(getItem(position))
    }

    class DailyFeeVH(
        private val binding: RvDailyFeeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dailyFee: DailyFee) {
            binding.run {
                savername.text = dailyFee.saverName
                fee.text = dailyFee.fee.formatToMoney(true)
                val daysLeft1 = App.getInstance().getString(R.string.dailyFeeHintDays1)
                val daysLeft2 = when (getWordEndingType(dailyFee.daysLeft)) {
                    WordEnding.TYPE_1 -> App.getInstance().getString(R.string.days1)
                    WordEnding.TYPE_2 -> App.getInstance().getString(R.string.days2)
                    WordEnding.TYPE_3 -> App.getInstance().getString(R.string.days3)
                }
                val daysLeftText = "$daysLeft1 ${dailyFee.daysLeft} $daysLeft2)"
                days.text = daysLeftText
                root.setOnClickListener {  }
            }
        }

        companion object {

            fun getViewHolder(parent: ViewGroup): DailyFeeVH {
                val binding = RvDailyFeeBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return DailyFeeVH(binding)
            }

        }

    }

    class DailyFeesDiffUtilCallback : DiffUtil.ItemCallback<DailyFee>() {

        override fun areItemsTheSame(oldItem: DailyFee, newItem: DailyFee): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: DailyFee, newItem: DailyFee): Boolean {
            return oldItem == newItem
        }
    }

}