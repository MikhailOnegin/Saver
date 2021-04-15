package digital.fact.saver.presentation.fragments.about

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import digital.fact.saver.R
import digital.fact.saver.databinding.FragmentAboutAppDescriptionBinding
import digital.fact.saver.utils.startIntentActionView
import digital.fact.saver.utils.startIntentSend

class AboutAppDescriptionFragment: Fragment() {

    private lateinit var binding: FragmentAboutAppDescriptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutAppDescriptionBinding.inflate(
            inflater, container,false
        )
        return binding.root
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setSpannable()



    }

    private fun setSpannable() {
        val spannableEmail = SpannableString(binding.textViewMailLink.text)
        val colorEmailSpan =
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.pink))
        val clickableEmailSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                startIntentSend(requireContext(), binding.textViewMailLink.text.toString())
            }
        }
        spannableEmail.setSpan(clickableEmailSpan, 0, binding.textViewMailLink.text.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableEmail.setSpan(colorEmailSpan, 0, binding.textViewMailLink.text.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textViewMailLink.text = spannableEmail
        binding.textViewMailLink.movementMethod = LinkMovementMethod.getInstance()


        val spannableVk = SpannableString(binding.textViewVkLink.text)
        val colorVkSpan =
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.pink))
        val clickableVkSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                startIntentActionView(requireContext(), "https://vk.com/kopilka_app")
            }
        }
        spannableVk.setSpan(clickableVkSpan, 0, binding.textViewVkLink.text.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableVk.setSpan(colorVkSpan, 0, binding.textViewVkLink.text.toString().length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.textViewVkLink.text = spannableVk
        binding.textViewVkLink.movementMethod = LinkMovementMethod.getInstance()
    }
}