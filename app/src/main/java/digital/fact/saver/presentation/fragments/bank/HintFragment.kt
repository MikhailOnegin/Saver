package digital.fact.saver.presentation.fragments.bank

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentHintBinding

class HintFragment : Fragment() {

    private lateinit var binding: FragmentHintBinding
    private var hint: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHintBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        hint = arguments?.getString(BankAddFragment.HINT, BankAddFragment.HINT_VIRTUAL)
        setHintToPager()
    }

    private fun setHintToPager() {
        when (hint) {
            BankAddFragment.HINT_VIRTUAL -> {
                binding.image.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_hint_virtual
                    )
                )
                binding.title.text = getString(R.string.hint_bank_virtual_title)
                binding.description.text = getString(R.string.hint_bank_virtual_description)
            }
            BankAddFragment.HINT_INCOME -> {
                binding.image.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_hint_virtual
                    )
                )
                binding.title.text = getString(R.string.hint_bank_income_title)
                binding.description.text = getString(R.string.hint_bank_income_description)
            }
            BankAddFragment.HINT_OUTCOME -> {
                binding.image.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireActivity(),
                        R.drawable.ic_hint_virtual
                    )
                )
                binding.title.text = getString(R.string.hint_bank_expenses_title)
                binding.description.text = getString(R.string.hint_bank_expenses_description)
            }
        }
    }

}