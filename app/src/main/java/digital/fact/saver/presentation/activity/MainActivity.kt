package digital.fact.saver.presentation.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import digital.fact.saver.R
import digital.fact.saver.data.repositories.ClassesRepositoryIml
import digital.fact.saver.databinding.ActivityMainBinding
import digital.fact.saver.domain.models.Class

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.bnv.setupWithNavController(findNavController(R.id.nav_host_fragment))
        binding.bnv.setOnNavigationItemReselectedListener { }
    }
}