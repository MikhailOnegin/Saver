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
import digital.fact.saver.databinding.FragmentPlansHostBinding
import digital.fact.saver.domain.models.Period
import digital.fact.saver.presentation.activity.MainActivity
import digital.fact.saver.presentation.adapters.pagers.PlansPagerAdapter
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.PlansViewModel
import digital.fact.saver.utils.WordEnding
import digital.fact.saver.utils.getWordEndingType
import java.text.ParseException
import java.text.SimpleDateFormat

class PlansHostFragment : Fragment() {
    private lateinit var binding: FragmentPlansHostBinding
    private lateinit var plansVM: PlansViewModel
    private lateinit var navCMain: NavController
    private lateinit var pagerAdapter: PlansPagerAdapter
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        plansVM = ViewModelProvider(
                requireActivity(),
                ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        )
                .get(PlansViewModel::class.java)
        operationsVM = ViewModelProvider(
            requireActivity(),
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(
            OperationsViewModel::class.java
        )
        navCMain = findNavController()
        initializedAdapters()
        setListeners()
        setObservers(this)
    }

    override fun onResume() {
        super.onResume()
        plansVM.updatePlans()
        plansVM.getPeriod()
    }

    private fun setListeners() {

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
        pagerAdapter = PlansPagerAdapter(this)
        binding.viewPager2.adapter = pagerAdapter
    }

    fun setObservers(owner: LifecycleOwner) {
            plansVM.period.observe(owner, { period ->
                setTitlesToolbar(period)
            })
    }

    @SuppressLint("SimpleDateFormat")
    private fun setTitlesToolbar(period: Period){
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
                WordEnding.TYPE_1 -> resources.getString(R.string.day)
                WordEnding.TYPE_2 -> resources.getString(R.string.day_2)
                WordEnding.TYPE_3 -> resources.getString(R.string.days)
            }
            val periodText = "${resources.getString(R.string.period2)} $diffDays $days"
            binding.toolbar.title = periodText

        } catch (e: ParseException) {
            e.printStackTrace()
        }
    }
}