package digital.fact.saver.presentation.adapters.recycler

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.LayoutPlanBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat

class PlansAdapter(
    private val click: (Long) -> Unit = {}
) : ListAdapter<Plan, PlansAdapter.PlansViewHolder>(PlansDiffUtilCallback()) {

    var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    fun getPlanById(id:Long):Plan?{
        return currentList.firstOrNull { plan -> plan.id == id }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlansViewHolder {
        return PlansViewHolder(
            LayoutPlanBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holderCurrent: PlansViewHolder, position: Int) {
        selectionTracker?.let {
            val selection = selectionTracker?.selection?.contains(currentList[position].id)
            selection?.let {
                holderCurrent.setSelected(it)
            }
        }
        holderCurrent.bind(currentList[position])
    }

    override fun getItemId(position: Int): Long = position.toLong()


    class PlansDiffUtilCallback : DiffUtil.ItemCallback<Plan>() {

        override fun areItemsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Plan, newItem: Plan): Boolean {
            return oldItem == newItem
        }
    }

    inner class PlansViewHolder(private val binding: LayoutPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan) {
            binding.textViewDate.text =
                plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = plan.name

            var spendLogo = ""
            var imageStatus: Drawable? = null
            when(plan.type){
                Plan.PlanType.SPENDING.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up)
                }
                Plan.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() /100)
            val sumText = if(plan.sum.toDouble() % 100 == 0.toDouble()){
                sum.toString() +"0"
            }
            else{
                sum.toString()
            }
            binding.textViewSpendLogo.text = spendLogo
            binding.textViewSum.text = sumText
            binding.constraintPlan.setOnClickListener {
                click.invoke(plan.id)
            }
        }

        fun setSelected(b: Boolean) {
            binding.constraintPlan.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(adapterPosition).id
                }
            }
    }

    class MyItemKeyProvider(private val adapter: PlansAdapter) :
        ItemKeyProvider<Long>(SCOPE_CACHED) {
        override fun getKey(position: Int): Long {
            return adapter.currentList[position].id
        }

        override fun getPosition(key: Long): Int {
            return adapter.currentList.indexOfFirst { it.id == key }
        }
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as PlansAdapter.PlansViewHolder).getItemDetails()
            }
            return null
        }
    }
}