package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.databinding.LayoutAboutAppHistoryItemBinding
import digital.fact.saver.databinding.RvAboutAppHistoryBinding

class AboutAppHistoryAdapter :
    ListAdapter<String, AboutAppHistoryAdapter.AppHistoryViewHolder>(AppHistoryDiffUtilCallback()) {

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

    inner class AppHistoryViewHolder(private val binding: RvAboutAppHistoryBinding) :
        RecyclerView.ViewHolder(
            binding.root
        ) {
        fun bind(history: String) {
            val stringArray = history.split("\n")
            val container = binding.linearHistory
            stringArray.forEach {
                if (it == stringArray.first()) binding.textViewVersion.text = it.trim()
                else {
                    val itemHistoryBinding = LayoutAboutAppHistoryItemBinding.inflate(
                        LayoutInflater.from(itemView.context),
                        container,
                        false
                    )
                    itemHistoryBinding.textViewDescription.text = it.trim()
                    container.addView(itemHistoryBinding.root)
                }

            }

        }

    }

    class AppHistoryDiffUtilCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

}