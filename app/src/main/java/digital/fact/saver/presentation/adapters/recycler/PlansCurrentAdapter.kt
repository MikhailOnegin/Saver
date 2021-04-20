package digital.fact.saver.presentation.adapters.recycler

import android.annotation.SuppressLint
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
import digital.fact.saver.databinding.RvPlanCurrentBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.utils.toDateString
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat

class PlansCurrentAdapter(
        private val click: ((Long) -> Unit)? = null,
        private val onCurrentPlanClickedInHistory:
            ((operationType:Int, planId: Long, planSum: Long, planName: String) -> Unit)? = null,
        private val currentPlansDialog: BottomSheetDialogFragment? = null
) : ListAdapter<Plan, PlansCurrentAdapter.PlansViewHolder>(PlansDiffUtilCallback()) {

    var selectionTracker: SelectionTracker<Long>? = null

    init {
        setHasStableIds(true)
    }

    fun getPlanById(id:Long): Plan?{
        return currentList.firstOrNull { plan -> plan.id == id }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlansViewHolder {
        return PlansViewHolder(
            RvPlanCurrentBinding.inflate(
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

    inner class PlansViewHolder(private val binding: RvPlanCurrentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SimpleDateFormat")
        fun bind(planTable: Plan) {

            if (planTable.planning_date == 0L)
                binding.textViewDate.visibility = View.GONE
            else {
                binding.textViewDate.visibility = View.VISIBLE
                binding.textViewDate.text = planTable.planning_date.toDateString(SimpleDateFormat("dd.MM.yyyy"))
            }

            binding.textViewCategory.text = planTable.name

            var spendLogo = ""
            var imageStatus: Drawable? = null
            when (planTable.type) {
                DbPlan.PlanType.EXPENSES.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_spend)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_up)
                }
                DbPlan.PlanType.INCOME.value -> {
                    spendLogo = itemView.resources.getString(R.string.planned_income)
                    imageStatus = ContextCompat.getDrawable(itemView.context, R.drawable.ic_arrow_down)
                }
            }
            imageStatus?.let {
                binding.imageViewStatus.setImageDrawable(it)
            }
            val sum = (planTable.sum.toDouble() /100)
            val bd = BigDecimal(sum)
            val sumText = bd.setScale(2, RoundingMode.HALF_UP).toString()
            binding.textViewSpendLogo.text = spendLogo
            binding.textViewSum.text = sumText
            setOnClickListener(planTable)
        }

        private fun setOnClickListener(plan: Plan) {
            binding.constraintPlan.setOnClickListener {
                click?.invoke(plan.id)
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

    class MyItemKeyProvider(private val currentAdapter: PlansCurrentAdapter) :
        ItemKeyProvider<Long>(SCOPE_CACHED) {
        override fun getKey(position: Int): Long {
            return currentAdapter.currentList[position].id
        }

        override fun getPosition(key: Long): Int {
            return currentAdapter.currentList.indexOfFirst { it.id == key }
        }
    }

    class MyItemDetailsLookup(private val recyclerView: RecyclerView) :
        ItemDetailsLookup<Long>() {
        override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
            val view = recyclerView.findChildViewUnder(event.x, event.y)
            if (view != null) {
                return (recyclerView.getChildViewHolder(view) as PlansCurrentAdapter.PlansViewHolder).getItemDetails()
            }
            return null
        }
    }
}