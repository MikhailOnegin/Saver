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
import digital.fact.saver.domain.models.PlanStatus
import digital.fact.saver.domain.models.SeparatorPlans
import digital.fact.saver.utils.sumToString
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
        return if (planItem is Plan && planItem.status == PlanStatus.CURRENT) {
            PLAN_CURRENT
        } else if (planItem is Plan && planItem.status == PlanStatus.DONE) {
            PLAN_DONE_IN_PERIOD
        } else if (planItem is SeparatorPlans) {
            SEPARATOR
        } else if (planItem is Plan && planItem.status == PlanStatus.DONE_OUTSIDE) {
            PLAN_DONE_OUTSIDE
        } else if (planItem is Plan && planItem.status == PlanStatus.OUTSIDE) {
            PLAN_OUTSIDE
        } else throw IllegalArgumentException("Wrong plan view holder type.")
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            SEPARATOR -> {
                SeparatorPlansViewHolder(
                    RvPlansSeparatorBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else ->
                PlanViewHolder(
                    RvPlanBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is PlanViewHolder -> {
                selectionTracker?.let {
                    val selection =
                        selectionTracker?.selection?.contains(currentList[position].itemId)
                    selection?.let {
                        holder.setSelected(it)
                    }
                }
                val plan = currentList[position]
                if (plan is Plan)
                    holder.bind(plan, getItemViewType(position))
            }
            is SeparatorPlansViewHolder -> {
            }
            else -> throw IllegalArgumentException("Wrong plan view holder type.")
        }
    }

    private inner class SeparatorPlansViewHolder(binding: RvPlansSeparatorBinding) :
        RecyclerView.ViewHolder(binding.root)


    inner class PlanViewHolder(private val binding: RvPlanBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SimpleDateFormat")
        fun bind(plan: Plan, typeHolder: Int) {
            setStyleViewHolder(itemView.context, typeHolder, binding)
            if (plan.planning_date == 0L)
                binding.textViewDate.visibility = View.GONE
            else {
                binding.textViewDate.visibility = View.VISIBLE
                binding.textViewDate.text =
                    plan.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            }

            var sumLogo1 = ""
            var sumLogo2 = ""

            var sum1 = 0L
            var sum2 = 0L

            val imageType: Drawable? = getImageStatus(itemView.context, plan.type, typeHolder)

            when (typeHolder) {
                PLAN_CURRENT -> {
                    sumLogo1 = when (plan.type) {
                        DbPlan.PlanType.EXPENSES.value -> {
                            itemView.resources.getString(R.string.plan_spend)
                        }
                        DbPlan.PlanType.INCOME.value -> {
                            itemView.resources.getString(R.string.plan_income)
                        }
                        else -> ""
                    }
                    sum1 = plan.sum
                    setOnClickListener(plan)
                }

                PLAN_DONE_IN_PERIOD -> {
                    when (plan.type) {
                        DbPlan.PlanType.EXPENSES.value -> {
                            sumLogo1 = itemView.resources.getString(R.string.factSumHintExpenses)
                            sumLogo2 = itemView.resources.getString(R.string.planned_spend)

                        }
                        DbPlan.PlanType.INCOME.value -> {
                            sumLogo1 = itemView.resources.getString(R.string.factSumHintIncome)
                            sumLogo2 = itemView.resources.getString(R.string.planned_income)
                        }
                    }
                    sum1 = plan.sum_fact
                    sum2 = plan.sum
                    binding.rootConstraint.setOnClickListener {
                        clickPlanDone?.invoke(plan.id)
                    }
                }
                PLAN_DONE_OUTSIDE -> {
                    when (plan.type) {
                        DbPlan.PlanType.EXPENSES.value -> {
                            sumLogo1 = itemView.resources.getString(R.string.factSumHintExpenses)
                            sumLogo2 = itemView.resources.getString(R.string.planned_spend)
                        }
                        DbPlan.PlanType.INCOME.value -> {
                            sumLogo1 = itemView.resources.getString(R.string.factSumHintIncome)
                            sumLogo2 = itemView.resources.getString(R.string.planned_income)
                        }
                    }
                    sum1 = plan.sum_fact
                    sum2 = plan.sum
                    binding.rootConstraint.setOnClickListener {
                        clickPlanDoneOutside?.invoke(plan.id)
                    }
                }

                PLAN_OUTSIDE -> {
                    sumLogo1 = when (plan.type) {
                        DbPlan.PlanType.EXPENSES.value -> {
                            itemView.resources.getString(R.string.planned_spend)
                        }
                        DbPlan.PlanType.INCOME.value -> {
                            itemView.resources.getString(R.string.planned_income)
                        }
                        else -> ""
                    }
                    sum1 = plan.sum
                    binding.rootConstraint.setOnClickListener {
                        clickPlanOutside?.invoke(plan.id)
                    }
                }
            }

            imageType?.let {
                binding.imageViewType.setImageDrawable(it)
            }

            binding.textViewTitle.text = plan.name
            binding.imageViewType.setImageDrawable(imageType)

            binding.textViewSumLogo1.text = sumLogo1
            binding.textViewSum1.text = sumToString(sum1)

            binding.textViewSumLogo2.text = sumLogo2
            binding.textViewSum2.text = sumToString(sum2)
        }

        fun setSelected(b: Boolean) {
            binding.rootConstraint.isSelected = b
        }

        fun getItemDetails(): ItemDetailsLookup.ItemDetails<Long> =
            object : ItemDetailsLookup.ItemDetails<Long>() {
                override fun getPosition(): Int {
                    return bindingAdapterPosition
                }

                override fun getSelectionKey(): Long {
                    return getItem(bindingAdapterPosition).itemId
                }
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
                return (recyclerView.getChildViewHolder(view) as PlansAdapter.PlanViewHolder).getItemDetails()
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

    private fun setStyleViewHolder(
        context: Context,
        typeHolder: Int,
        binding: RvPlanBinding
    ) {
        when (typeHolder) {
            PLAN_CURRENT -> {
                binding.rootConstraint.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_plan_in_period)
                binding.imageViewType.layoutParams.width =
                    context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.imageViewType.layoutParams.height =
                    context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.constraintSumPlanned.visibility = View.GONE
                binding.textViewDate.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.background_gradient_pink_light_pink
                )
            }
            PLAN_DONE_IN_PERIOD -> {
                binding.rootConstraint.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_plan_in_period)
                binding.imageViewType.layoutParams.width =
                    context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.imageViewType.layoutParams.height =
                    context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.constraintSumPlanned.visibility = View.VISIBLE
                binding.textViewDate.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.background_gradient_pink_light_pink
                )
            }
            PLAN_DONE_OUTSIDE -> {
                binding.rootConstraint.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_plan_outside)
                binding.imageViewType.layoutParams.width =
                    context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.imageViewType.layoutParams.height =
                    context.resources.getDimension(R.dimen.image_plan_done_outside_size).toInt()
                binding.constraintSumPlanned.visibility = View.VISIBLE
                binding.textViewDate.background =
                    ContextCompat.getDrawable(context, R.drawable.background_purple)

            }
            PLAN_OUTSIDE -> {
                binding.rootConstraint.background =
                    ContextCompat.getDrawable(context, R.drawable.selector_plan_outside)
                binding.imageViewType.layoutParams.width =
                    context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.imageViewType.layoutParams.height =
                    context.resources.getDimension(R.dimen.planImageSize).toInt()
                binding.constraintSumPlanned.visibility = View.GONE
                binding.textViewDate.background =
                    ContextCompat.getDrawable(context, R.drawable.background_purple)
            }
        }
    }

    private fun getImageStatus(context: Context, type: Int, typeHolder: Int): Drawable? {
        val result: Drawable?
        when (typeHolder) {
            PLAN_DONE_IN_PERIOD -> {
                result = when (type) {
                    DbPlan.PlanType.INCOME.value -> {
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_arrow_up_completed
                        )
                    }
                    DbPlan.PlanType.EXPENSES.value -> {
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_arrow_down_completed
                        )
                    }
                    else -> null
                }
            }
            PLAN_DONE_OUTSIDE -> {
                result = when (type) {
                    DbPlan.PlanType.INCOME.value -> {
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_arrow_up_completed_2
                        )
                    }
                    DbPlan.PlanType.EXPENSES.value -> {
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_arrow_up_completed_2
                        )
                    }
                    else -> null
                }
            }
            else -> {
                result = when (type) {
                    DbPlan.PlanType.INCOME.value -> {
                        ContextCompat.getDrawable(context, R.drawable.ic_arrow_down)
                    }
                    DbPlan.PlanType.EXPENSES.value -> {
                        ContextCompat.getDrawable(context, R.drawable.ic_arrow_up)
                    }
                    else -> null
                }
            }
        }
        return result
    }
}