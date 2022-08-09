package com.techriz.andronix.donation.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.github.florent37.viewanimator.ViewAnimator
import com.techriz.andronix.donation.MainActivity
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding

    val viewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        println("Executed!")
        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.applyTheme()
            animateLogo()
        }
    }

    private fun animateLogo() {
        ViewAnimator
            .animate(binding.logo)
            .pulse()
            .duration(350)
            .repeatCount(1)
            .onStop {
                lifecycleScope.launch {
                    this@SplashActivity.startActivity(
                        Intent(
                            this@SplashActivity,
                            MainActivity::class.java
                        )
                    )
                    finish()
                }
            }
            .start()
    }
}
