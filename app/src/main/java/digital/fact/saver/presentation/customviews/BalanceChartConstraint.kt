package digital.fact.saver.presentation.customviews

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import digital.fact.saver.R
import digital.fact.saver.databinding.LayoutBalanceChartBinding
import digital.fact.saver.domain.models.BalanceChartItem
import digital.fact.saver.utils.round
import digital.fact.saver.utils.toDateString
import java.text.SimpleDateFormat

class BalanceChartConstraint(context: Context) : ConstraintLayout(context) {

    private lateinit var binding: LayoutBalanceChartBinding

    init {
        binding = LayoutBalanceChartBinding.inflate(LayoutInflater.from(
            context
        ))
        inflate(context, R.layout.layout_balance_chart, binding.root)
    }

    @SuppressLint("SimpleDateFormat")
    fun setData(balanceChart: BalanceChartItem){
        binding.textViewDate.text = balanceChart.sum.toDateString(SimpleDateFormat("dd.MM.yy"))
        binding.textViewSum.text = round(balanceChart.sum.toDouble(), 2).toString()
    }
}