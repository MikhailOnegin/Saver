package digital.fact.saver.presentation.fragments.plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import digital.fact.saver.databinding.FragmentPlansWithoutPeriodBinding

class PlansWithoutPeriod: Fragment() {
    private lateinit var binding: FragmentPlansWithoutPeriodBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlansWithoutPeriodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }
}