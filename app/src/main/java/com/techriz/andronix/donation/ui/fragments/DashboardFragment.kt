package com.techriz.andronix.donation.ui.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.DashboardFragmentBinding
import com.techriz.andronix.donation.databinding.LoginFirstSheetBinding
import com.techriz.andronix.donation.databinding.UserInfoBottomsheetBinding
import com.techriz.andronix.donation.utils.NavigationAnimations
import com.techriz.andronix.donation.utils.NavigationAnimations.safeNavigate
import com.techriz.andronix.donation.utils.SkuInfo
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DashboardFragment : Fragment(R.layout.dashboard_fragment) {
    private lateinit var binding: DashboardFragmentBinding
    val viewModel: DashboardViewModel by viewModels();
    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DashboardFragmentBinding.bind(view)


        binding.hamMenu.setOnClickListener {
            val navDrawer = activity?.findViewById<DrawerLayout>(R.id.drawer_layout)
            navDrawer?.let {
                if (!navDrawer.isDrawerOpen(GravityCompat.START)) navDrawer.openDrawer(Gravity.START) else navDrawer.closeDrawer(
                    Gravity.END
                )
            }
        }

        binding.primusCard.apply {
            SkuInfo.PRIMUS().apply {
                titleTv.text = title
                descTv.text = desc
                logoIv.setImageResource(logo)
                setOnClickListener {
                    if (viewModel.isUserLoggedIn()) {
                        findNavController().safeNavigate(
                            R.id.action_dashboardFragment_to_payFragment,
                            bundleOf("sku" to sku_id),
                            NavigationAnimations.getAlphaAnimation()
                        )
                    } else {
                        showLoginDialog()
                    }
                }
            }
        }
        binding.blazeCard.apply {
            SkuInfo.BLAZE().apply {
                titleTv.text = title
                descTv.text = desc
                logoIv.setImageResource(logo)
                setOnClickListener {
                    if (viewModel.isUserLoggedIn()) {
                        findNavController().safeNavigate(
                            R.id.action_dashboardFragment_to_payFragment,
                            bundleOf("sku" to sku_id),
                            NavigationAnimations.getAlphaAnimation()
                        )
                    } else {
                        showLoginDialog()
                    }
                }
            }
        }
        binding.warriorCard.apply {
            SkuInfo.WARRIOR().apply {
                titleTv.text = title
                descTv.text = desc
                logoIv.setImageResource(logo)
                setOnClickListener {
                    if (viewModel.isUserLoggedIn()) {
                        findNavController().safeNavigate(
                            R.id.action_dashboardFragment_to_payFragment,
                            bundleOf("sku" to sku_id),
                            NavigationAnimations.getAlphaAnimation()
                        )
                    } else {
                        showLoginDialog()
                    }
                }
            }
        }
        binding.saviorCard.apply {
            SkuInfo.SAVIOR().apply {
                titleTv.text = title
                descTv.text = desc
                logoIv.setImageResource(logo)
                setOnClickListener {
                    if (viewModel.isUserLoggedIn()) {
                        findNavController().safeNavigate(
                            R.id.action_dashboardFragment_to_payFragment,
                            bundleOf("sku" to sku_id),
                            NavigationAnimations.getAlphaAnimation()
                        )
                    } else {
                        showLoginDialog()
                    }
                }
            }
        }

        binding.profileImage.apply {
            setUserPfp()
            setOnClickListener { if (viewModel.isUserLoggedIn()) showProfileDialog() else showLoginDialog() }
        }

    }

    private fun ImageView.setUserPfp() {

        val url = viewModel.getUserPfp()

        when {
            url.endsWith(".svg") -> {
                val imageLoader = ImageLoader.Builder(requireContext())
                    .componentRegistry {
                        add(SvgDecoder(requireContext()))
                    }
                    .build()
                val request = ImageRequest.Builder(requireContext())
                    .data(url)
                    .target(this)
                    .build()
                imageLoader.enqueue(request)
            }
            else -> {
                this.load(url)
            }
        }
    }

    private fun showLoginDialog() {
        val binding = LoginFirstSheetBinding.inflate(layoutInflater)
        val dialog = RoundedBottomSheetDialog(requireContext())
        dialog.setContentView(binding.root)
        binding.login.setOnClickListener {
            dialog.dismiss()
            findNavController().safeNavigate(
                R.id.action_dashboardFragment_to_loginFragment,
                null,
                NavigationAnimations.getAlphaAnimation()
            )
        }
        binding.closeLoginFirst.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun showProfileDialog() {
        val binding = UserInfoBottomsheetBinding.inflate(layoutInflater)
        val dialog = RoundedBottomSheetDialog(requireContext())
        dialog.setContentView(binding.root)
        binding.logoutButton.setOnClickListener {
            dialog.dismiss()
            Firebase.auth.signOut()
            this.binding.profileImage.setUserPfp()
        }
        binding.emailTv.text = viewModel.getUserEmail() ?: ""
        binding.profileImage.setUserPfp()
        binding.nameTv.text = viewModel.getUserName()
        binding.closeLoginFirst.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }
}