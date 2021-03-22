package digital.fact.saver.presentation.adapters.recycler

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.databinding.RvOperationBinding
import digital.fact.saver.domain.models.Operation.OperationType
import digital.fact.saver.models.Operation
import digital.fact.saver.presentation.adapters.recycler.OperationsAdapter.OperationVH
import digital.fact.saver.utils.toStringFormatter
import java.lang.IllegalArgumentException

class OperationsAdapter : ListAdapter<Operation, OperationVH>(OperationsDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OperationVH {
        return OperationVH.getViewHolder(parent)
    }

    override fun onBindViewHolder(holder: OperationVH, position: Int) {
        holder.bind(getItem(position))
    }

    class OperationVH(
            private val binding: RvOperationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(operation: Operation) {
            setIcon(operation)
            setPlanInfo(operation)
            setSecondSourceVisibility(operation)
            binding.run {
                source.text = "Имя кошелька"
                sourceSum.text = "(${operation.fromSourceSum.toStringFormatter(true)})"
                sourceTo.text = "Имя другого кошелька"
                sourceToSum.text = "(${operation.toSourceSum.toStringFormatter(true)})"
                setDescription(operation)
                val sign = getOperationSign(operation)
                setSumTextStyle(operation)
                sum.text = "$sign${operation.sum.toStringFormatter(true)}"
            }
        }

        private fun setSecondSourceVisibility(operation: Operation) {
            when (operation.type) {
                OperationType.TRANSFER.value -> {
                    binding.sourceTo.visibility = View.VISIBLE
                    binding.sourceToSum.visibility = View.VISIBLE
                }
                else -> {
                    binding.sourceTo.visibility = View.GONE
                    binding.sourceToSum.visibility = View.GONE
                }
            }
        }

        private fun setDescription(operation: Operation) {
            val res = itemView.context.resources
            val text = when (operation.type) {
                OperationType.SAVER_EXPENSES.value -> res.getString(R.string.rvOperationSaverExpenses)
                OperationType.SAVER_INCOME.value -> res.getString(R.string.rvOperationSaverIncome)
                OperationType.TRANSFER.value -> res.getString(R.string.rvOperationTransfer)
                else -> operation.name
            }
            binding.description.text = text
        }

        private fun setSumTextStyle(operation: Operation) {
            val whiteColor = ContextCompat.getColor(itemView.context, R.color.textColorWhite)
            val greenColor = ContextCompat.getColor(itemView.context, R.color.textColorGreen)
            when (operation.type) {
                OperationType.INCOME.value,
                OperationType.PLANNED_INCOME.value -> {
                    binding.sum.setTextColor(greenColor)
                    binding.sum.typeface = Typeface.DEFAULT_BOLD
                }
                else -> {
                    binding.sum.setTextColor(whiteColor)
                    binding.sum.typeface = Typeface.DEFAULT
                }
            }
        }

        private fun getOperationSign(operation: Operation): String {
            return when (operation.type) {
                OperationType.EXPENSES.value,
                OperationType.PLANNED_EXPENSES.value,
                OperationType.SAVER_EXPENSES.value -> "- "
                OperationType.INCOME.value,
                OperationType.PLANNED_INCOME.value,
                OperationType.SAVER_INCOME.value -> "+ "
                OperationType.TRANSFER.value -> ""
                else -> throw IllegalArgumentException()
            }
        }

        private fun setPlanInfo(operation: Operation) {
            val context = itemView.context
            binding.run {
                when (operation.type) {
                    OperationType.PLANNED_EXPENSES.value,
                    OperationType.PLANNED_INCOME.value -> {
                        planSum.text = operation.planSum.toStringFormatter(true)
                        planIcon.visibility = View.VISIBLE
                        planSumHint.visibility = View.VISIBLE
                        planSum.visibility = View.VISIBLE
                        setBottomMarginToDescription(false)
                    }
                    else -> {
                        planIcon.visibility = View.GONE
                        planSumHint.visibility = View.GONE
                        planSum.visibility = View.GONE
                        setBottomMarginToDescription(true)
                    }
                }
                if (operation.type == OperationType.PLANNED_EXPENSES.value) {
                    planSumHint.text = context.getString(R.string.rvOperationPlanExpensesHint)
                } else {
                    planSumHint.text = context.getString(R.string.rvOperationPlanIncomeHint)
                }
            }
        }

        private fun setBottomMarginToDescription(addMargin: Boolean) {
            val res = itemView.context.resources
            val smallMargin = res.getDimension(R.dimen.smallMargin).toInt()
            val normalMargin = res.getDimension(R.dimen.normalMargin).toInt()
            (binding.description.layoutParams as ConstraintLayout.LayoutParams).setMargins(
                    normalMargin, smallMargin, 0, if (addMargin) normalMargin else 0
            )
        }

        private fun setIcon(operation: Operation) {
            val resource = when (operation.type) {
                OperationType.EXPENSES.value,
                OperationType.PLANNED_EXPENSES.value -> R.drawable.ic_operation_expenses
                OperationType.INCOME.value,
                OperationType.PLANNED_INCOME.value -> R.drawable.ic_operation_income
                OperationType.TRANSFER.value -> R.drawable.ic_operation_transfer
                OperationType.SAVER_EXPENSES.value -> R.drawable.ic_operation_saver_expenses
                OperationType.SAVER_INCOME.value -> R.drawable.ic_operation_saver_income
                else -> throw IllegalArgumentException()
            }
            binding.icon.setImageResource(resource)
        }

        companion object {

            fun getViewHolder(parent: ViewGroup): OperationVH {
                val binding = RvOperationBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false)
                return OperationVH(binding)
            }

        }

    }

    class OperationsDiffUtilCallback : DiffUtil.ItemCallback<Operation>() {

        override fun areItemsTheSame(oldItem: Operation, newItem: Operation): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Operation, newItem: Operation): Boolean {
            return oldItem == newItem
        }

    }

}