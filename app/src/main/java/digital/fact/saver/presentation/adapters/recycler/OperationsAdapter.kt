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
import digital.fact.saver.data.database.dto.Operation.OperationType
import digital.fact.saver.domain.models.Operation
import digital.fact.saver.presentation.adapters.recycler.OperationsAdapter.OperationVH
import digital.fact.saver.utils.formatToMoney
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
                setSourcesInfo(operation)
                setDescription(operation)
                val sign = getOperationSign(operation)
                setSumTextStyle(operation)
                sum.text = "$sign${operation.sum.formatToMoney(true)}"
                root.setOnClickListener { }
            }
        }

        @SuppressLint("SetTextI18n")
        private fun setSourcesInfo(operation: Operation) {
            binding.run {
                operation.let {
                    when (operation.type) {
                        OperationType.TRANSFER.value -> {
                            source.text = it.fromSourceName
                            sourceSum.text = "(${it.fromSourceSum.formatToMoney(true)})"
                            sourceTo.text = it.toSourceName
                            sourceToSum.text = "(${it.toSourceSum.formatToMoney(true)})"
                        }
                        OperationType.EXPENSES.value,
                        OperationType.PLANNED_EXPENSES.value,
                        OperationType.SAVER_EXPENSES.value -> {
                            source.text = it.fromSourceName
                            sourceSum.text = "(${it.fromSourceSum.formatToMoney(true)})"
                        }
                        OperationType.INCOME.value,
                        OperationType.PLANNED_INCOME.value,
                        OperationType.SAVER_INCOME.value -> {
                            source.text = it.toSourceName
                            sourceSum.text = "(${it.toSourceSum.formatToMoney(true)})"
                        }
                    }
                }
            }
        }

        private fun setSecondSourceVisibility(operation: Operation) {
            val smallMargin = itemView.context.resources.getDimension(R.dimen.smallMargin).toInt()
            val normalMargin = itemView.context.resources.getDimension(R.dimen.normalMargin).toInt()
            val sourcesParams = (binding.source.layoutParams as ConstraintLayout.LayoutParams)
            when (operation.type) {
                OperationType.TRANSFER.value -> {
                    binding.sourceTo.visibility = View.VISIBLE
                    binding.sourceToSum.visibility = View.VISIBLE
                    binding.fromTitle.visibility = View.VISIBLE
                    binding.toTitle.visibility = View.VISIBLE
                    sourcesParams.setMargins(smallMargin, normalMargin, 0, 0)
                }
                else -> {
                    binding.sourceTo.visibility = View.GONE
                    binding.sourceToSum.visibility = View.GONE
                    binding.fromTitle.visibility = View.GONE
                    binding.toTitle.visibility = View.GONE
                    sourcesParams.setMargins(normalMargin, normalMargin, 0, 0)
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
                        planSum.text = operation.planSum.formatToMoney(true)
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