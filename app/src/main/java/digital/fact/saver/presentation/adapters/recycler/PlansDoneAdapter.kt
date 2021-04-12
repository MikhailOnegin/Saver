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
import digital.fact.saver.databinding.LayoutPlanDoneBinding
import digital.fact.saver.databinding.LayoutPlanDoneOutsideBinding
import digital.fact.saver.databinding.LayoutSeparatorPlansBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.PlanDoneOutside
import digital.fact.saver.domain.models.PlanItem
import digital.fact.saver.domain.models.SeparatorPlans
import digital.fact.saver.utils.toDateString
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat

class PlansDoneAdapter(
        private val click: (Long) -> Unit = {}
) : ListAdapter<PlanItem, RecyclerView.ViewHolder>(PlansDiffUtilCallback()) {

    var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }


    fun getPlanById(id:Long): PlanItem?{
        return currentList.firstOrNull { plan -> plan._id == id }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is Plan -> PLAN_DONE_IN_PERIOD
            is SeparatorPlans -> SEPARATOR
            else -> PLAN_DONE_OUTSIDE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PLAN_DONE_IN_PERIOD -> {
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
                selectionTracker?.let {
                    val selection = selectionTracker?.selection?.contains(currentList[position]._id)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
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
        fun bind(plan: Plan) {
            binding.textViewDate.text =
                    plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = plan.name
            var sumPlanLogo = ""
            var sumPlannedLogo = ""
            var imageStatus: Drawable? = null
            when (plan.type) {
                PlanTable.PlanType.EXPENSES.value -> {
                    sumPlanLogo = "Фактически потрачено"
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down_completed_2)
                }
                PlanTable.PlanType.INCOME.value -> {
                    sumPlanLogo = "Фактически получено"
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up_completed_2)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() /100)
            val bd = BigDecimal(sum)
            val sumTextPlanned = bd.setScale(2, RoundingMode.HALF_UP).toString()

            val sumPlanned = (plan.sum_fact.toDouble() /100)
            val bdPlanned = BigDecimal(sumPlanned)
            val sumTextPlan = bdPlanned.setScale(2, RoundingMode.HALF_UP).toString()

            binding.textViewSumPlanLogo.text = sumPlanLogo
            binding.textViewSumPlannedLogo.text = sumPlannedLogo
            binding.textViewSumPlan.text = sumTextPlan
            binding.textViewSumPlanned.text = sumTextPlanned
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
                        return getItem(adapterPosition)._id
                    }
                }

    }

    private inner class PlansDoneOutsideHolder(private val binding: LayoutPlanDoneOutsideBinding) :
            RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(plan: PlanDoneOutside) {
            binding.textViewDate.text =
                    plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewCategory.text = plan.name
            var sumPlanLogo = ""
            var sumPlannedLogo = ""
            var imageStatus: Drawable? = null
            when (plan.type) {
                PlanTable.PlanType.EXPENSES.value -> {
                    sumPlanLogo = "Фактически потрачено"
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down_completed_2)
                }
                PlanTable.PlanType.INCOME.value -> {
                    sumPlanLogo = "Фактически получено"
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up_completed_2)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() /100)
            val bd = BigDecimal(sum)
            val sumTextPlaned = bd.setScale(2, RoundingMode.HALF_UP).toString()

            val sumPlanned = (plan.sum_fact.toDouble() /100)
            val bdPlanned = BigDecimal(sumPlanned)
            val sumTextPlan = bdPlanned.setScale(2, RoundingMode.HALF_UP).toString()

            binding.textViewSumPlanLogo.text = sumPlanLogo
            binding.textViewSumPlannedLogo.text = sumPlannedLogo
            binding.textViewSum.text = sumTextPlan
            binding.textViewSumPlanned.text = sumTextPlaned
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
                        return getItem(adapterPosition)._id
                    }
                }
    }

    private inner class SeparatorPlansViewHolder(binding: LayoutSeparatorPlansBinding) :
            RecyclerView.ViewHolder(binding.root)

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
                    is PlansDoneOutsideHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansDoneAdapter.PlansDoneOutsideHolder).getItemDetails()
                    is PlansDoneViewHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansDoneAdapter.PlansDoneViewHolder).getItemDetails()
                    else -> null
                }
            }
            return null
        }
    }

    private companion object {
        const val PLAN_DONE_IN_PERIOD = 0
        const val SEPARATOR = 1
        const val PLAN_DONE_OUTSIDE = 2
    }
}