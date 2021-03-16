package digital.fact.saver.presentation.fragments.bank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import digital.fact.saver.databinding.FragmentBankBinding
import digital.fact.saver.presentation.viewmodels.OperationsViewModel
import digital.fact.saver.presentation.viewmodels.SourcesViewModel

class BankFragment : Fragment() {

    private lateinit var binding: FragmentBankBinding
    private lateinit var sourcesVM: SourcesViewModel
    private lateinit var operationsVM: OperationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBankBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        sourcesVM = ViewModelProvider(requireActivity())[SourcesViewModel::class.java]
        operationsVM = ViewModelProvider(requireActivity())[OperationsViewModel::class.java]
    }

}