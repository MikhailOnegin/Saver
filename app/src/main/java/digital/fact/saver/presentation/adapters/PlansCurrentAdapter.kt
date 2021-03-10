package digital.fact.saver.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.databinding.LayoutPlanBinding
import digital.fact.saver.domain.models.Plan

class PlansCurrentAdapter: ListAdapter<Plan, PlansCurrentAdapter.PlanViewHolder>(PlansDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(LayoutPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    override fun getItemCount(): Int {
        return currentList.size
    }

    class PlansDiffUtilCallback : DiffUtil.ItemCallback<Plan>() {

        override fun areItemsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem == newItem
        }
    }
    inner class PlanViewHolder(private val binding: LayoutPlanBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(plan: Plan){
            binding.textViewDate.text = plan.planning_date.toString()
            binding.textViewCategory.text = plan.category.toString()
        }
    }
}