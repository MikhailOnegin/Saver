package digital.fact.saver.presentation.fragments.plan


import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.PlansPagerAdapter
import digital.fact.saver.presentation.dialogs.AddPlanDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat


class PlansFragment : Fragment() {

    private lateinit var binding: FragmentPlansBinding
    private lateinit var plansVM: PlansViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
                .get(PlansViewModel::class.java)
        initializedAdapters()
        TabLayoutMediator(binding.tableLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Текущие"
                1 -> tab.text = "Выполненные"
                2 -> tab.text = "Вне периода"
            }
        }.attach()
        setListeners()
        setObservers(this)
    }

    override fun onStart() {
        super.onStart()
        plansVM.getPeriod()
    }

    private fun setListeners() {
        binding.imageButtonAddPlan.setOnClickListener {
            AddPlanDialog().show(
                childFragmentManager,
                "add Plan"
            )
        }
    }

    private fun initializedAdapters() {
        binding.viewPager2.adapter =
            PlansPagerAdapter(activity?.supportFragmentManager!!, this.lifecycle)
    }

    @SuppressLint("SimpleDateFormat")
    fun setObservers(owner: LifecycleOwner){
        plansVM.period.observe(owner, {
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
            val dateFrom = dateFormatter.format(it.dateFrom.time)
            val dateTo = dateFormatter.format(it.dateTo.time)
            val periodRange = "$dateFrom - $dateTo"
            binding.textViewPeriodRange.text = periodRange

            try {
                val date1 = it.dateTo.time
                val date2 = it.dateFrom.time
                val diff: Long = date1.time - date2.time
                val diffDays = (diff / 86_400_000).toInt()
                val period = "${resources.getString(R.string.period2)} $diffDays ${
                    resources.getString(
                        R.string.days
                    )
                } "
                binding.textViewPeriodDays.text = period

            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        )
        plansVM.getAllPlans().observe(owner, { plans ->
            val spendingDefault: Double = if(binding.textViewSpending.text.isEmpty()) 0.00
            else binding.textViewSpending.text.toString().toDouble()
            binding.textViewSpending.text = spendingDefault.toString()

            val incomeDefault: Double = if(binding.textViewIncome.text.isEmpty()) 0.00
            else binding.textViewIncome.text.toString().toDouble()
            binding.textViewIncome.text = incomeDefault.toString()

            var spending = 0.00f
            var income = 0.00f

            for (i in plans.indices) {
                val plan = plans[i]
                when (plan.category) {
                    Plan.PlanCategory.SPENDING.value -> spending += plan.sum
                    else -> income += plan.sum
                }
            }

            val roundSpending = round(spending, 2)
            val roundIncome = round(income, 2)

            startCountAnimation( binding.textViewSpending,spendingDefault.toFloat(), roundSpending.toFloat())
            startCountAnimation( binding.textViewIncome, incomeDefault.toFloat(), roundIncome.toFloat())
        })
    }

    private fun startCountAnimation(view: TextView,  fromNumber: Float, toNumber :Float) {
        val animator = ValueAnimator.ofFloat(fromNumber, toNumber)
        animator.duration = 340
        animator.addUpdateListener { animation -> view.text = animation.animatedValue.toString() }
        animator.start()
    }
}