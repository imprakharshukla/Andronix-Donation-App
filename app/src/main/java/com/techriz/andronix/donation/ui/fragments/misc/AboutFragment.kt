package com.techriz.andronix.donation.ui.fragments.misc

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.FragmentAboutBinding
import com.techriz.andronix.donation.utils.ActionUtils
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_DISCORD
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_DOCS
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_GITHUB
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_POLICIES
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_WEB
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_YT
import com.techriz.andronix.donation.utils.Constants.DARK_MODE
import com.techriz.andronix.donation.utils.Constants.DEVRIZ_LOGO_DARK
import com.techriz.andronix.donation.utils.Constants.DEVRIZ_LOGO_LIGHT
import com.techriz.andronix.donation.utils.Constants.DEVRIZ_WEBSITE
import com.techriz.andronix.donation.utils.Constants.LIGHT_MODE
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class AboutFragment @Inject constructor() : Fragment(R.layout.fragment_about) {

    lateinit var binding: FragmentAboutBinding

    private val viewModel: SharedViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding = FragmentAboutBinding.bind(view)

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.version.text = getVersion()
        setClickListenersOnLinks()
        binding.devrizWebsiteText.apply {
            text = getDevrizWebsiteSpannableText()
            movementMethod = LinkMovementMethod.getInstance()
        }
        lifecycleScope.launch {
            setDevrizLogos()
        }
    }

    private suspend fun setDevrizLogos() {
        viewModel.themeLive.collect {
            when (it) {
                DARK_MODE -> {
                    binding.devrizLogo.load(DEVRIZ_LOGO_DARK)
                }
                LIGHT_MODE -> {
                    binding.devrizLogo.load(DEVRIZ_LOGO_LIGHT)
                }
            }
        }
    }

    private fun getVersion(): String? {
        return try {
            val pInfo =
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            pInfo.versionName ?: ""
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
    }

    private fun getDevrizWebsiteSpannableText(): Spannable {
        val ss =
            SpannableString("www.devriz.com")
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                ActionUtils.getBrowser(requireContext(), DEVRIZ_WEBSITE)
            }
        }
        ss.setSpan(
            clickableSpan1,
            0,
            ss.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }

    private fun setClickListenersOnLinks() {
        val actionUtils = ActionUtils
        binding.apply {
            discordAbout.setOnClickListener {
                actionUtils.getBrowser(
                    requireContext(),
                    ANDRONIX_DISCORD
                )
            }
            gitAbout.setOnClickListener {
                actionUtils.getBrowser(
                    requireContext(),
                    ANDRONIX_GITHUB
                )
            }
            webAbout.setOnClickListener { actionUtils.getBrowser(requireContext(), ANDRONIX_WEB) }
            ytAbout.setOnClickListener { actionUtils.getBrowser(requireContext(), ANDRONIX_YT) }
            policiesAbout.setOnClickListener {
                actionUtils.getBrowser(
                    requireContext(),
                    ANDRONIX_POLICIES
                )
            }
            discordAbout.setOnClickListener {
                actionUtils.getBrowser(
                    requireContext(),
                    ANDRONIX_DOCS
                )
            }
        }
    }

}