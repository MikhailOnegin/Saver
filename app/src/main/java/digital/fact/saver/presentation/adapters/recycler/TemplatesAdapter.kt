package digital.fact.saver.presentation.adapters.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import digital.fact.saver.R
import digital.fact.saver.data.database.dto.DbOperation
import digital.fact.saver.databinding.RvTemplateBinding
import digital.fact.saver.domain.models.Template
import digital.fact.saver.utils.formatToMoney

class TemplatesAdapter(
    private val onTemplateClicked: (Template) -> Unit
) : ListAdapter<Template, TemplatesAdapter.TemplateVH>(TemplatesDiffUtilCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateVH {
        return TemplateVH.getViewHolder(parent)
    }

    override fun onBindViewHolder(holder: TemplateVH, position: Int) {
        holder.bind(getItem(position), onTemplateClicked)
    }

    class TemplateVH(
        private val binding: RvTemplateBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            template: Template,
            onTemplateClicked: (Template) -> Unit
        ) {
            binding.run {
                name.text = template.operationName
                sum.text = template.operationSum.formatToMoney(true)
                val iconResource = when (template.operationType) {
                    DbOperation.OperationType.EXPENSES.value -> R.drawable.ic_operation_expenses
                    DbOperation.OperationType.INCOME.value -> R.drawable.ic_operation_income
                    else -> 0
                }
                if (iconResource != 0) icon.setImageResource(iconResource)
                root.setOnClickListener { onTemplateClicked.invoke(template) }
            }
        }

        companion object {

            fun getViewHolder(parent: ViewGroup): TemplateVH {
                val binding = RvTemplateBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return TemplateVH(binding)
            }

        }

    }

    class TemplatesDiffUtilCallback : DiffUtil.ItemCallback<Template>() {

        override fun areItemsTheSame(oldItem: Template, newItem: Template): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Template, newItem: Template): Boolean {
            return oldItem == newItem
        }

    }

}