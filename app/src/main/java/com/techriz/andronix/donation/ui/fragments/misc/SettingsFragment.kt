package com.techriz.andronix.donation.ui.fragments.misc

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.github.florent37.viewanimator.ViewAnimator
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.SettingsFragmentBinding
import com.techriz.andronix.donation.utils.Constants.DARK_MODE
import com.techriz.andronix.donation.utils.Constants.DEVICE_MODE
import com.techriz.andronix.donation.utils.Constants.LIGHT_MODE
import com.techriz.andronix.donation.utils.makeGone
import com.techriz.andronix.donation.utils.makeVisible
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import studio.com.techriz.andronix.ui.Loader
import javax.inject.Inject

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class SettingsFragment @Inject constructor(
) : Fragment(R.layout.settings_fragment) {

    lateinit var binding: SettingsFragmentBinding
    private val viewModel: SettingsViewModel by viewModels()

    @Inject
    lateinit var loader: Loader
    private var isThemeLayoutOpen = false

    private var langRotatedDegree = 0F
    private var themeRotatedDegree = 0F


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = SettingsFragmentBinding.bind(view)

        lifecycleScope.launch {
            setRefreshValues()
        }

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        /*Theme*/
        binding.darkThemeButton.setOnClickListener {
            lifecycleScope.launch {
                AppCompatDelegate
                    .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                withContext(Dispatchers.Default) {
                    viewModel.setCurrentTheme(DARK_MODE)
                }
                setRefreshValues()
            }
        }
        binding.lightThemeButton.setOnClickListener {
            lifecycleScope.launch {
                AppCompatDelegate
                    .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                withContext(Dispatchers.Default) {
                    viewModel.setCurrentTheme(LIGHT_MODE)
                }
                setRefreshValues()
            }
        }
        binding.systemThemeButton.setOnClickListener {
            lifecycleScope.launch {
                AppCompatDelegate
                    .setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                withContext(Dispatchers.Default) {
                    viewModel.setCurrentTheme(DEVICE_MODE)
                }
                setRefreshValues()
            }
        }
    }

    private suspend fun setRefreshValues() {
        withContext(Dispatchers.Main) {
            val theme = viewModel.getCurrentTheme()
            println("Theme $theme")
            when (theme) {
                DARK_MODE -> {
                    binding.darkThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders_orange
                        )

                    binding.lightThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )

                    binding.systemThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )

                }
                LIGHT_MODE -> {
                    binding.lightThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders_orange
                        )

                    binding.systemThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )

                    binding.darkThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )
                }
                DEVICE_MODE -> {
                    binding.systemThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders_orange
                        )

                    binding.lightThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )

                    binding.darkThemeButton.background =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.rounded_borders
                        )
                }

            }


        }

    }


    private fun View.rotate180(layout: String) {
        var degree = 0F
        if (layout == "lang") {
            degree += langRotatedDegree + 180F
            langRotatedDegree = degree
        } else {
            degree += themeRotatedDegree + 180F
            themeRotatedDegree = degree
        }
        ViewAnimator
            .animate(this)
            .duration(200)
            .rotation(degree)
            .start()

    }
}