package digital.fact.saver.presentation.fragments.plan

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentPlansBinding
import digital.fact.saver.domain.models.Period
import digital.fact.saver.data.database.dto.PlanTable
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.adapters.pagers.PlansPagerAdapter
import digital.fact.saver.presentation.dialogs.SlideToPerformDialog
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.WordEnding
import digital.fact.saver.utils.getWordEndingType
import digital.fact.saver.utils.startCountAnimation
import eightbitlab.com.blurview.RenderScriptBlur
import java.text.ParseException
import java.text.SimpleDateFormat

class PlansFragment : Fragment() {

    private lateinit var binding: FragmentPlansBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navCMain: NavController
    private lateinit var pagerAdapter: PlansPagerAdapter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansBinding.inflate(inflater, container, false)
        setupBlurView()
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
        binding.viewPager2.adapter = pagerAdapter
        setListeners()
        setObservers(this)
    }

    override fun onResume() {
        super.onResume()
        plansVM.updatePlans()
        plansVM.getPeriod()
    }

    private fun setListeners() {
        //binding.floatingButtonAddPlan.setOnClickListener {
        //    navCMain.navigate(R.id.action_plansFragment_to_AddPlanFragment)
        //}
        binding.toolbar.setNavigationOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }

        TabLayoutMediator(binding.tableLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = resources.getString(R.string.current)
                1 -> tab.text = resources.getString(R.string.completed)
                2 -> tab.text = resources.getString(R.string.outside)
            }
        }.attach()

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.add -> {
                    navCMain.navigate(R.id.action_plansFragment_to_AddPlanFragment)
                }
            }
            false
        }
    }

    private fun initializedAdapters() {
        pagerAdapter = PlansPagerAdapter(activity?.supportFragmentManager!!, this.lifecycle)
    }

    @SuppressLint("SimpleDateFormat")
    fun setObservers(owner: LifecycleOwner) {
        plansVM.period.observe(owner, { period ->
            plansVM.getAllPlans().value?.let { plans ->
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
                val dateFrom = dateFormatter.format(period.dateFrom.time)
                val dateTo = dateFormatter.format(period.dateTo.time)
                val periodRange = "$dateFrom - $dateTo"
                binding.toolbar.subtitle = periodRange

                try {
                    val date1 = period.dateTo.time
                    val date2 = period.dateFrom.time
                    val diff: Long = date1.time - date2.time
                    val diffDays = (diff / 86_400_000).toInt()
                    val days = when (getWordEndingType(diffDays.toLong())) {
                        WordEnding.TYPE_1 -> "день"
                        WordEnding.TYPE_2 -> "дня"
                        WordEnding.TYPE_3 -> "дней"
                    }
                    val periodText = "${resources.getString(R.string.period2)} $diffDays $days"
                    binding.toolbar.title = periodText

                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                //setCalculateData(plans, period)
            }
        }
        )

        plansVM.getAllPlans().observe(owner, { plans ->

            plansVM.period.value?.let { period ->
                val dateFormatter = SimpleDateFormat("dd.MM.yyyy")
                val dateFrom = dateFormatter.format(period.dateFrom.time)
                val dateTo = dateFormatter.format(period.dateTo.time)
                val periodRange = "$dateFrom - $dateTo"
                binding.toolbar.subtitle = periodRange

                try {
                    val date1 = period.dateTo.time
                    val date2 = period.dateFrom.time
                    val diff: Long = date1.time - date2.time
                    val diffDays = (diff / 86_400_000).toInt()
                    val days = when (getWordEndingType(diffDays.toLong())) {
                        WordEnding.TYPE_1 -> "день"
                        WordEnding.TYPE_2 -> "дня"
                        WordEnding.TYPE_3 -> "дней"
                    }
                    val periodText = "${resources.getString(R.string.period2)} $diffDays $days"
                    binding.toolbar.title = periodText

                } catch (e: ParseException) {
                    e.printStackTrace()
                }
               // setCalculateData(plans, period)
            }
        }
        )
    }

    private fun setupBlurView() {
        //val radius = 10f
        //binding.blurView.setupWith(binding.root)
        //        .setBlurAlgorithm(RenderScriptBlur(requireActivity()))
        //        .setBlurRadius(radius)
        //        .setBlurAutoUpdate(true)
        //
        //binding.blurView.doOnLayout {
        //    plansVM.setPlansBlurViewWidth(it.height)
        //}
    }

    //private fun setCalculateData(planTables: List<PlanTable>, period: Period) {

        //val unixFrom = period.dateFrom.time.time
        //val unixTo = period.dateTo.time.time
        //val plansCurrent = planTables.filter { it.operation_id == 0L && it.planning_date > unixFrom && it.planning_date < unixTo || it.planning_date == 0L }
        //
        //var s = 0.00
        //var i = 0.00
        //
        //plansCurrent.forEach {plan ->
        //    when(plan.type){
        //        PlanTable.PlanType.EXPENSES.value -> s += plan.sum.toDouble() / 100
        //        PlanTable.PlanType.INCOME.value -> i += plan.sum.toDouble() / 100
        //    }
        //}
        //startCountAnimation(
        //        binding.textViewSpending,
        //        binding.textViewSpending.text.toString().toFloat(),
        //        s.toFloat(),
        //        400,
        //        2
        //)
        //
        //startCountAnimation(
        //        binding.textViewIncome,
        //        binding.textViewIncome.text.toString().toFloat(),
        //        i.toFloat(),
        //        400,
        //        2
        //)
    }