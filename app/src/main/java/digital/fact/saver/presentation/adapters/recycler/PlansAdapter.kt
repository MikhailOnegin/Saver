package digital.fact.saver.presentation.adapters.recycler

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.data.database.dto.DbPlan
import digital.fact.saver.databinding.*
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.domain.models.PlanItem
import digital.fact.saver.domain.models.SeparatorPlans
import digital.fact.saver.utils.toDateString
import java.lang.IllegalArgumentException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat

class PlansAdapter(
    private val clickPlanCurrent: ((Long) -> Unit)? = null,
    private val onCurrentPlanClickedInHistory:
    ((operationType: Int, planId: Long, planSum: Long, planName: String) -> Unit)? = null,
    private val currentPlansDialog: BottomSheetDialogFragment? = null,
    private val clickPlanDone: ((Long) -> Unit)? = null,
    private val clickPlanDoneOutside: ((Long) -> Unit)? = null,
    private val clickPlanOutside: ((Long) -> Unit)? = null

) : ListAdapter<PlanItem, RecyclerView.ViewHolder>(PlansDiffUtilCallback()) {

    var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    fun getPlanById(id: Long): PlanItem? {
        return currentList.firstOrNull { planItem ->
            planItem.itemId == id
        }
    }

    override fun getItemViewType(position: Int): Int {
        val planItem = currentList[position]
        return if (planItem is Plan && planItem.operation_id == 0L && planItem.inPeriod){
            PLAN_CURRENT
        }
        else if (planItem is Plan && planItem.operation_id != 0L && planItem.inPeriod){
            PLAN_DONE_IN_PERIOD
        }
        else if (planItem is SeparatorPlans) {
            SEPARATOR
        }
        else if (planItem is Plan && planItem.operation_id != 0L && !planItem.inPeriod) {
            PLAN_DONE_OUTSIDE
        }
        else if (planItem is Plan && !planItem.inPeriod) {
            PLAN_OUTSIDE
        }
        else throw IllegalArgumentException("Wrong plan view holder type.")
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            PLAN_CURRENT -> {
                PlanCurrentViewHolder(
                    RvPlanBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            PLAN_DONE_IN_PERIOD -> {
                PlanDoneViewHolder(
                    RvPlanBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            SEPARATOR -> {
                SeparatorPlansViewHolder(
                    RvPlansSeparatorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            PLAN_DONE_OUTSIDE -> {
                PlanDoneOutsideHolder(
                    RvPlanBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            PLAN_OUTSIDE -> {
                PlanOutsideViewHolder(
                    RvPlanBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Wrong plan view holder type.")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PlanCurrentViewHolder -> {
                selectionTracker?.let {
                    val selection =
                        selectionTracker?.selection?.contains(currentList[position].itemId)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                val plan = currentList[position]
                if (plan is Plan)
                    holder.bind(plan)
            }
            is PlanDoneViewHolder -> {
                selectionTracker?.let {
                    val selection =
                        selectionTracker?.selection?.contains(currentList[position].itemId)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                val plan = currentList[position]
                if (plan is Plan)
                    holder.bind(plan)
            }
            is SeparatorPlansViewHolder -> {
            }
            is PlanDoneOutsideHolder -> {
                selectionTracker?.let {
                    val selection =
                        selectionTracker?.selection?.contains(currentList[position].itemId)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                val plan = currentList[position]
                if (plan is Plan)
                    holder.bind(plan)
            }
            is PlanOutsideViewHolder -> {
                selectionTracker?.let {
                    val selection =
                        selectionTracker?.selection?.contains(currentList[position].itemId)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                val plan = currentList[position]
                if (plan is Plan)
                    holder.bind(plan)
            }
            else -> throw IllegalArgumentException("Wrong plan view holder type.")
        }

    }

    inner class PlanCurrentViewHolder(private val binding: RvPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan) {
            setStyleViewHolder(itemView.context, this, binding)
            if (plan.planning_date == 0L)
                binding.textViewDate.visibility = View.GONE
            else {
                binding.textViewDate.visibility = View.VISIBLE
                binding.textViewDate.text =
                    plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            }

            binding.textViewTitle.text = plan.name

            var spendLogo = ""
            var imageStatus: Drawable? = null
            when (plan.type) {
                DbPlan.PlanType.EXPENSES.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus =
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up)
                }
                DbPlan.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus =
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() / 100)
            val bd = BigDecimal(sum)
            val sumText = bd.setScale(2, RoundingMode.HALF_UP).toString()
            binding.textViewSumLogo1.text = spendLogo
            binding.textViewSum1.text = sumText
            setOnClickListener(plan)
        }

        private fun setOnClickListener(plan: Plan) {
            binding.rootConstraint.setOnClickListener {
                clickPlanCurrent?.invoke(plan.id)
                when (plan.type) {
                    DbPlan.PlanType.EXPENSES.value -> {
                        onCurrentPlanClickedInHistory?.invoke(
                            DbOperation.OperationType.PLANNED_EXPENSES.value,
                            plan.id,
                            plan.sum,
                            plan.name
                        )
                    }
                    DbPlan.PlanType.INCOME.value -> {
                        onCurrentPlanClickedInHistory?.invoke(
                            DbOperation.OperationType.PLANNED_INCOME.value,
                            plan.id,
                            plan.sum,
                            plan.name
                        )
                    }
                }
                currentPlansDialog?.dismiss()
            }
        }

        fun setSelected(b: Boolean) {
            binding.rootConstraint.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(adapterPosition).itemId
                }
            }
    }


    private inner class PlanDoneViewHolder(private val binding: RvPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan) {
            setStyleViewHolder(itemView.context, this, binding)
            binding.textViewDate.text =
                plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewTitle.text = plan.name
            var sumPlanLogo = ""
            var sumPlannedLogo = ""
            var imageStatus: Drawable? = null
            when (plan.type) {
                DbPlan.PlanType.EXPENSES.value -> {
                    sumPlanLogo = itemView.resources.getString(R.string.factSumHintExpenses)
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_arrow_down_completed
                    )
                }
                DbPlan.PlanType.INCOME.value -> {
                    sumPlanLogo = itemView.resources.getString(R.string.factSumHintIncome)
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_arrow_up_completed
                    )
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() / 100)
            val bd = BigDecimal(sum)
            val sumTextPlanned = bd.setScale(2, RoundingMode.HALF_UP).toString()

            val sumPlanned = (plan.sum_fact.toDouble() / 100)
            val bdPlanned = BigDecimal(sumPlanned)
            val sumTextPlan = bdPlanned.setScale(2, RoundingMode.HALF_UP).toString()

            binding.textViewSumLogo1.text = sumPlanLogo
            binding.textViewSumLogo2.text = sumPlannedLogo
            binding.textViewSum1.text = sumTextPlan
            binding.textViewSum2.text = sumTextPlanned
            binding.rootConstraint.setOnClickListener {
                clickPlanDone?.invoke(plan.id)
            }
        }

        fun setSelected(b: Boolean) {
            binding.rootConstraint.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(adapterPosition).itemId
                }
            }
    }

    private inner class SeparatorPlansViewHolder(binding: RvPlansSeparatorBinding) :
        RecyclerView.ViewHolder(binding.root)

    private inner class PlanDoneOutsideHolder(private val binding: RvPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan) {
            setStyleViewHolder(itemView.context, this, binding)
            binding.textViewDate.text =
                plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewTitle.text = plan.name
            var sumPlanLogo = ""
            var sumPlannedLogo = ""
            var imageStatus: Drawable? = null
            when (plan.type) {
                DbPlan.PlanType.EXPENSES.value -> {
                    sumPlanLogo = itemView.resources.getString(R.string.factSumHintExpenses)
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_arrow_down_completed_2
                    )
                }
                DbPlan.PlanType.INCOME.value -> {
                    sumPlanLogo = itemView.resources.getString(R.string.factSumHintIncome)
                    sumPlannedLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.ic_arrow_up_completed_2
                    )
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (plan.sum.toDouble() / 100)
            val bd = BigDecimal(sum)
            val sumTextPlaned = bd.setScale(2, RoundingMode.HALF_UP).toString()

            val sumPlanned = (plan.sum_fact.toDouble() / 100)
            val bdPlanned = BigDecimal(sumPlanned)
            val sumTextPlan = bdPlanned.setScale(2, RoundingMode.HALF_UP).toString()

            binding.textViewSumLogo1.text = sumPlanLogo
            binding.textViewSumLogo2.text = sumPlannedLogo
            binding.textViewSum1.text = sumTextPlan
            binding.textViewSum2.text = sumTextPlaned
            binding.rootConstraint.setOnClickListener {
                clickPlanDoneOutside?.invoke(plan.id)
            }
        }

        fun setSelected(b: Boolean) {
            binding.rootConstraint.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(adapterPosition).itemId
                }
            }
    }

    inner class PlanOutsideViewHolder(private val binding: RvPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(planTable: Plan) {
            setStyleViewHolder(itemView.context, this, binding)
            binding.textViewDate.text =
                planTable.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            binding.textViewTitle.text = planTable.name

            var spendLogo = ""
            var imageStatus: Drawable? = null
            when (planTable.type) {
                DbPlan.PlanType.EXPENSES.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus =
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up)
                }
                DbPlan.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus =
                        ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down)
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
            binding.textViewSumLogo1.text = spendLogo
            binding.textViewSum1.text = sumText
            binding.rootConstraint.setOnClickListener {
                clickPlanOutside?.invoke(planTable.id)
            }
        }

        fun setSelected(b: Boolean) {
            binding.rootConstraint.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return adapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(adapterPosition).itemId
                }
            }
    }

    class PlansDiffUtilCallback : DiffUtil.ItemCallback<PlanItem>() {

        override fun areItemsTheSame(oldItem: PlanItem, newItem: PlanItem): Boolean {
            return oldItem.itemId == newItem.itemId
        }

        override fun areContentsTheSame(oldItem: PlanItem, newItem: PlanItem): Boolean {
            return oldItem == newItem
        }
    }

    class MyItemKeyProvider(private val plansAdapter: PlansAdapter) :
        ItemKeyProvider<Long>(SCOPE_CACHED) {
        override fun getKey(position: Int): Long {
            return plansAdapter.currentList[position].itemId
        }

        override fun getPosition(key: Long): Int {
            return plansAdapter.currentList.indexOfFirst { it.itemId == key }
        }
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return when (recyclerView.getChildViewHolder(view)) {
                    is PlansAdapter.PlanCurrentViewHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansAdapter.PlanCurrentViewHolder).getItemDetails()
                    is PlansAdapter.PlanDoneViewHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansAdapter.PlanDoneViewHolder).getItemDetails()
                    is PlansAdapter.PlanDoneOutsideHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansAdapter.PlanDoneOutsideHolder).getItemDetails()
                    is PlansAdapter.PlanOutsideViewHolder ->
                        (recyclerView.getChildViewHolder(view) as PlansAdapter.PlanOutsideViewHolder).getItemDetails()

                    else -> null
                }
            }
            return null
        }
    }

    private companion object {
        const val PLAN_CURRENT = 0
        const val PLAN_DONE_IN_PERIOD = 1
        const val SEPARATOR = 2
        const val PLAN_DONE_OUTSIDE = 3
        const val PLAN_OUTSIDE = 4
    }

    private fun setStyleViewHolder(context: Context, holder: RecyclerView.ViewHolder, binding: RvPlanBinding){
        when (holder){
            is PlanCurrentViewHolder -> {
                binding.rootConstraint.background = ContextCompat.getDrawable(context, R.drawable.selector_plan_in_period)
                binding.imageViewStatus.layoutParams.width = context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.imageViewStatus.layoutParams.height = context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.constraintSumPlanned.visibility = View.GONE
                binding.textViewDate.background = ContextCompat.getDrawable(context, R.drawable.background_gradient_pink_light_pink)
            }
            is PlanDoneViewHolder -> {
                binding.rootConstraint.background = ContextCompat.getDrawable(context, R.drawable.selector_plan_in_period)
                binding.imageViewStatus.layoutParams.width = context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.imageViewStatus.layoutParams.height = context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.constraintSumPlanned.visibility = View.VISIBLE
                binding.textViewDate.background = ContextCompat.getDrawable(context, R.drawable.background_gradient_pink_light_pink)
            }
            is PlanDoneOutsideHolder -> {
                binding.rootConstraint.background = ContextCompat.getDrawable(context, R.drawable.selector_plan_outside)
                binding.imageViewStatus.layoutParams.width = context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.imageViewStatus.layoutParams.height = context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.constraintSumPlanned.visibility = View.VISIBLE
                binding.textViewDate.background = ContextCompat.getDrawable(context, R.drawable.background_purple)

            }
            is PlanOutsideViewHolder ->{
                binding.rootConstraint.background = ContextCompat.getDrawable(context, R.drawable.selector_plan_outside)
                binding.imageViewStatus.layoutParams.width = context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.imageViewStatus.layoutParams.height = context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.constraintSumPlanned.visibility = View.GONE
                binding.textViewDate.background = ContextCompat.getDrawable(context, R.drawable.background_purple)
            }
        }
    }
}