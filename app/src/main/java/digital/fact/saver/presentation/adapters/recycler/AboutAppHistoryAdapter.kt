package digital.fact.saver.presentation.adapters.recycler

import android.R.string
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.databinding.LayoutAboutAppHistoryItemBinding
import digital.fact.saver.databinding.RvAboutAppHistoryBinding
import digital.fact.saver.domain.models.AppHistory


class AboutAppHistoryAdapter: ListAdapter<AppHistory, AboutAppHistoryAdapter.AppHistoryViewHolder>(
    AppHistoryDiffUtilCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHistoryViewHolder {
        return AppHistoryViewHolder(
            RvAboutAppHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AppHistoryViewHolder, position: Int) {
        holder.bind(currentList[position])
    }



    inner class AppHistoryViewHolder(private val binding: RvAboutAppHistoryBinding): RecyclerView.ViewHolder(
        binding.root
    ){
        fun bind(history: AppHistory){
            binding.textViewVersion.text = history.version
            val stringArray = history.description.split("\n")
            val container = binding.linearHistory
            stringArray.forEach {

                val itemHistoryBinding = LayoutAboutAppHistoryItemBinding.inflate(
                    LayoutInflater.from(
                        itemView.context,
                    ),
                    container,
                    false
                )
                itemHistoryBinding.textViewDescription.text = it
                container.addView(itemHistoryBinding.root)
            }

        }

    }

    class AppHistoryDiffUtilCallback: DiffUtil.ItemCallback<AppHistory>(){
        override fun areItemsTheSame(oldItem: AppHistory, newItem: AppHistory): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: AppHistory, newItem: AppHistory): Boolean {
            return oldItem == newItem
        }
    }
}