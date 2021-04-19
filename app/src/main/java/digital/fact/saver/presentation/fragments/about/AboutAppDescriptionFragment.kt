package digital.fact.saver.presentation.fragments.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import digital.fact.saver.databinding.FragmentAboutAppDescriptionBinding

class AboutAppDescriptionFragment : Fragment() {

    private lateinit var binding: FragmentAboutAppDescriptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutAppDescriptionBinding.inflate(
            inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setListeners()
    }

    private fun setListeners() {
        binding.factContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://fact.digital"))
            startActivity(intent)
        }
        binding.googlePlayContainer.setOnClickListener {
            //sergeev: Заменить на ссылку нового приложения.
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=ru.phoenix.saver"))
            startActivity(intent)
        }
        binding.vkContainer.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://vk.com/kopilka_app"))
            startActivity(intent)
        }
    }

}