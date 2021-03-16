package digital.fact.saver.presentation.adapters.recycler

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.LayoutPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat

class PlansCurrentAdapter(
    private val clickPlan: (id: Int) -> Unit = {},
    private val longClickPlan: (selected: Boolean)-> Unit = {}
): ListAdapter<Plan, PlansCurrentAdapter.PlanCurrentViewHolder>(PlansDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanCurrentViewHolder {
        return PlanCurrentViewHolder(LayoutPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holderCurrent: PlanCurrentViewHolder, position: Int) {
        holderCurrent.bind(currentList[position])
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
    inner class PlanCurrentViewHolder(private val binding: LayoutPlanBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan){
            binding.textViewDate.text = plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = plan.name

            val spendLogo = when(plan.category){
                Plan.PlanCategory.SPENDING.value -> itemView.resources.getString(R.string.planned_spend)
                Plan.PlanCategory.INCOME.value -> itemView.resources.getString(R.string.planned_income)
                else -> itemView.resources.getString(R.string.error)
            }
            binding.textViewSpendLogo.text = spendLogo
            binding.textViewSum.text = plan.sum.toString()
            binding.constraintPlan.setOnClickListener {
                clickPlan.invoke(plan._id)
            }
            binding.constraintPlan.setOnLongClickListener {
                binding.constraintPlan.isSelected = !it.isSelected
                longClickPlan.invoke(it.isSelected)
                return@setOnLongClickListener true
            }
        }
    }
}