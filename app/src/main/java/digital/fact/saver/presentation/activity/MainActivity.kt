package digital.fact.saver.presentation.activity

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import digital.fact.saver.R
import digital.fact.saver.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isBnvShown = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        binding.bnv.setupWithNavController(findNavController(R.id.nav_host_fragment))
        binding.bnv.setOnNavigationItemReselectedListener { }
        setupDrawerLayout()
    }

    private fun setupDrawerLayout() {
        val navController = findNavController(R.id.nav_host_fragment)
        binding.navigationView.setupWithNavController(navController)
    }

    fun openDrawer() {
        binding.drawerLayout.open()
    }

    fun hideBottomNavigationView() {
        if (!isBnvShown) return
        val animator = ObjectAnimator.ofFloat(
                binding.bnv,
                View.TRANSLATION_Y,
                0f,
                binding.bnv.height.toFloat()
        )
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                binding.bnv.visibility = View.GONE
                isBnvShown = false
            }
        })
        animator.start()
    }

    fun showBottomNavigationView() {
        if (isBnvShown) return
        val animator = ObjectAnimator.ofFloat(
                binding.bnv,
                View.TRANSLATION_Y,
                binding.bnv.height.toFloat(),
                0f
        )
        animator.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                binding.bnv.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator?) {
                isBnvShown = true
            }
        })
        animator.start()
    }

}