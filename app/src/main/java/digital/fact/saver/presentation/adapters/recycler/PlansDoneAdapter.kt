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
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.databinding.LayoutPlanCurrentBinding
import digital.fact.saver.databinding.LayoutPlanDoneBinding
import digital.fact.saver.databinding.LayoutPlanDoneOutsideBinding
import digital.fact.saver.databinding.LayoutSeparatorPlansBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.PlanDoneOutside
import digital.fact.saver.domain.models.PlanItem
import digital.fact.saver.domain.models.SeparatorPlans
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat

class PlansDoneAdapter(
        private val click: (Long) -> Unit = {}
) : ListAdapter<PlanItem, RecyclerView.ViewHolder>(PlansDiffUtilCallback()) {

    var selectionTracker: SelectionTracker<Long>? = null


    init {
        setHasStableIds(true)
    }

    fun getPlanById(id:Long): PlanItem?{
        val g = currentList
        return currentList.firstOrNull { plan -> plan._id == id }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is Plan -> PLAN_DEFAULT
            is SeparatorPlans -> SEPARATOR
            else -> PLAN_DONE_OUTSIDE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PLAN_DEFAULT -> {
                PlansDoneViewHolder(
                        LayoutPlanDoneBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                        )
                )
            }
            SEPARATOR -> {
                SeparatorPlansViewHolder(
                   LayoutSeparatorPlansBinding.inflate(
                           LayoutInflater.from(parent.context),
                           parent,
                           false
                   )
                )
            }
            else -> {
                PlansDoneOutsideHolder(
                        LayoutPlanDoneOutsideBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                        )
                )
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is PlansDoneViewHolder -> {

                holder.bind(currentList[position] as Plan)
            }
            is PlansDoneOutsideHolder -> {
                selectionTracker?.let {
                    val selection = selectionTracker?.selection?.contains(currentList[position]._id)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                holder.bind(currentList[position] as PlanDoneOutside)
            }
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()


    class PlansDiffUtilCallback : DiffUtil.ItemCallback<PlanItem>() {

        override fun areItemsTheSame(oldItem: PlanItem, newItem: PlanItem): Boolean {
            return oldItem._id == newItem._id
        }

        override fun areContentsTheSame(oldItem: PlanItem, newItem: PlanItem): Boolean {
            return oldItem == newItem
        }
    }

    private inner class PlansDoneViewHolder(private val binding: LayoutPlanDoneBinding) :
            RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(planTable: Plan) {
            binding.textViewDate.text =
                    planTable.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = planTable.name
            var spendLogo = ""
            var imageStatus: Drawable? = null
            when (planTable.type) {
                PlanTable.PlanType.SPENDING.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down_completed)
                }
                PlanTable.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up_completed)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (planTable.sum.toDouble() / 100)
            val sumText = if (planTable.sum.toDouble() % 100 == 0.toDouble()) {
                sum.toString() + "0"
            } else {
                sum.toString()
            }
            binding.textViewSpendLogo.text = spendLogo
            binding.textViewSumPlanIncome.text = sumText
            binding.constraintPlan.setOnClickListener {
                click.invoke(planTable.id)
            }
        }

        //fun setSelected(b: Boolean) {
        //    binding.constraintPlan.isSelected = b
                //}
        //fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
        //        object : ItemDetailsLookup.ItemDetails<Long>() {
        //            override fun getPosition(): Int {
        //                return adapterPosition
                    //            }
        //            override fun getSelectionKey(): Long {
        //                return getItem(adapterPosition)._id
                    //            }
        //        }
    }

    private inner class PlansDoneOutsideHolder(private val binding: LayoutPlanDoneOutsideBinding) :
            RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(planTable: PlanDoneOutside) {
            binding.textViewDate.text =
                    planTable.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = planTable.name
            var spendLogo = ""
            var imageStatus: Drawable? = null
            when (planTable.type) {
                PlanTable.PlanType.SPENDING.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down_completed_2)
                }
                PlanTable.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up_completed_2)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (planTable.sum.toDouble() / 100)
            val sumText = if (planTable.sum.toDouble() % 100 == 0.toDouble()) {
                sum.toString() + "0"
            } else {
                sum.toString()
            }
            binding.textViewSpendLogo.text = spendLogo
            binding.textViewSumPlanIncome.text = sumText
            binding.constraintPlan.setOnClickListener {
                click.invoke(planTable.id)
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
                        return getItem(adapterPosition)._id
                    }
                }
    }

    private inner class SeparatorPlansViewHolder(binding: LayoutSeparatorPlansBinding) :
            RecyclerView.ViewHolder(binding.root) {

    }

    class MyItemKeyProvider(private val currentAdapter: PlansDoneAdapter) :
            ItemKeyProvider<Long>(SCOPE_CACHED) {
        override fun getKey(position: Int): Long {
            return currentAdapter.currentList[position]._id
        }

        override fun getPosition(key: Long): Int {
            return currentAdapter.currentList.indexOfFirst { it._id == key }
        }
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
            ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return when(recyclerView.getChildViewHolder(view)){
                    //is PlansViewHolder -> { (recyclerView.getChildViewHolder(view) as PlansDoneAdapter.PlansViewHolder).getItemDetails() }
                    is PlansDoneOutsideHolder ->{
                        (recyclerView.getChildViewHolder(view) as PlansDoneAdapter.PlansDoneOutsideHolder).getItemDetails()
                    }
                    else -> null
                }
            }
            return null
        }
    }

    private companion object {
        const val PLAN_DEFAULT = 0
        const val SEPARATOR = 1
        const val PLAN_DONE_OUTSIDE = 2
    }
}