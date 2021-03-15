package digital.fact.saver.presentation.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.LayoutPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat

class PlansCurrentAdapter(
    private val clickPlan: (id: Int) -> Unit
): ListAdapter<Plan, PlansCurrentAdapter.PlanViewHolder>(PlansDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        return PlanViewHolder(LayoutPlanBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ))
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        holder.bind(currentList[position], position)
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
        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan, position: Int){
            //val paddingTop = if(position == 0) R.dimen._32dp
            //else 0
            //binding.rootConstraint.setPadding(0,paddingTop, 0, 64)
            binding.textViewDate.text = plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = plan.name
            binding.textViewSum.text = plan.sum.toString()
            binding.rootConstraint.setOnClickListener {
                clickPlan.invoke(plan._id)
            }
        }
    }
}