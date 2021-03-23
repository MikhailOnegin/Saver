package digital.fact.saver.presentation.fragments.plan


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansBinding
import digital.fact.saver.domain.models.Plan
import digital.fact.saver.presentation.adapters.PlansPagerAdapter
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.round
import digital.fact.saver.utils.startCountAnimation
import java.text.ParseException
import java.text.SimpleDateFormat


class PlansFragment : Fragment() {

    private lateinit var binding: FragmentPlansBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navCMain: NavController
    private lateinit var plansPagerAdapter: PlansPagerAdapter

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
        navCMain = findNavController()
        initializedAdapters()
        binding.viewPager2.adapter = plansPagerAdapter
        TabLayoutMediator(binding.tableLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.current)
                1 -> tab.text = resources.getString(R.string.completed)
                2 -> tab.text = resources.getString(R.string.outside)
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
        binding.floatingButtonAddPlan.setOnClickListener {
            navCMain.navigate(R.id.action_plansFragment_to_AddPlanFragment)
        }
    }

    private fun initializedAdapters() {
        plansPagerAdapter = PlansPagerAdapter(activity?.supportFragmentManager!!, this.lifecycle)
    }

    @SuppressLint("SimpleDateFormat")
    fun setObservers(owner: LifecycleOwner) {
        plansVM.period.observe(owner, {
            val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
            val dateFrom = dateFormatter.format(it.dateFrom.time)
            val dateTo = dateFormatter.format(it.dateTo.time)
            val periodRange = "$dateFrom - $dateTo"
            binding.toolbar.subtitle = periodRange

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
                binding.toolbar.title = period

            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        )

        plansVM.getAllPlans().observe(owner, { plans ->

            val textViewSpendingEmpty = binding.textViewSpending.text.isEmpty()
            val textViewIncomeEmpty = binding.textViewIncome.text.isEmpty()

            val spendingDefault: Double = if (textViewSpendingEmpty) 0.00
            else binding.textViewSpending.text.toString().toDouble()
            binding.textViewSpending.text = spendingDefault.toString()

            val incomeDefault: Double = if (textViewIncomeEmpty) 0.00
            else binding.textViewIncome.text.toString().toDouble()
            binding.textViewIncome.text = incomeDefault.toString()

            var spending = round(0.00, 2)
            var income = round(0.00, 2)

            for (i in plans.indices) {
                val plan = plans[i]
                when (plan.type) {
                    Plan.PlanType.SPENDING.value -> spending += plan.sum
                    else -> income += plan.sum
                }
            }

            val roundSpending = round(spending, 2) / 100
            val roundIncome = round(income, 2) / 100

            if (textViewIncomeEmpty) binding.textViewSpending.text = roundSpending.toString()
            else {
                startCountAnimation(
                    binding.textViewSpending,
                    spendingDefault.toFloat(),
                    roundSpending.toFloat(),
                    400,
                    2
                )

            }
            if (textViewIncomeEmpty) binding.textViewIncome.text = roundIncome.toString()
            else {
                startCountAnimation(
                    binding.textViewIncome,
                    incomeDefault.toFloat(),
                    roundIncome.toFloat(),
                    400,
                    2
                )
            }

            //Для дополнительного нуля в случае с нулевой дробной частью
            if(roundSpending  % 10 == 0.toDouble()){
                val textSpending = "${binding.textViewSpending.text}0"
                binding.textViewSpending.text = textSpending
            }
            if(roundIncome % 10 == 0.toDouble()){
                val textIncome = "${binding.textViewIncome.text}0"
                binding.textViewIncome.text = textIncome
            }
        }
        )
    }
}