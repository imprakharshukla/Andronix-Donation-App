package com.techriz.andronix.donation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.techriz.andronix.donation.databinding.ActivityMainBinding
import com.techriz.andronix.donation.databinding.NavHeaderDashboardBinding
import com.techriz.andronix.donation.ui.fragments.DashboardViewModel
import com.techriz.andronix.donation.utils.ActionUtils
import com.techriz.andronix.donation.utils.ConnectionLiveData
import com.techriz.andronix.donation.utils.NavigationAnimations
import com.techriz.andronix.donation.utils.NavigationAnimations.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    val viewModel: DashboardViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val navMenu = binding.navView.menu
        binding.navView.setNavigationItemSelectedListener(navigationItemSelectedListener)
        setDrawerListeners()
        for (i in 0 until navMenu.size()) {
            navMenu.getItem(i).isChecked = false
        }

        observeConnectivity()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun clearCheckedItems(menu: Menu) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            item.isChecked = false
        }
    }

    private fun setDrawerListeners() {
        val navMenu = binding.navView.menu

        binding.drawerLayout.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {

                    clearCheckedItems(navMenu)

                    val navigationView: NavigationView = binding.navView
                    val headerView = navigationView.getHeaderView(0)
                    val headerBinding = NavHeaderDashboardBinding.bind(headerView)

                    headerBinding.root.setOnClickListener {
                        if (viewModel.isUserLoggedIn()) {
                            findNavController(R.id.nav_host_fragment).safeNavigate(
                                R.id.dashboardFragment,
                                null,
                                NavigationAnimations.getAlphaAnimation()
                            )
                        } else {
                            findNavController(R.id.nav_host_fragment).safeNavigate(
                                R.id.action_dashboardFragment_to_loginFragment,
                                null,
                                NavigationAnimations.getAlphaAnimation()
                            )
                        }
                        binding.drawerLayout.closeDrawers()
                    }
                }
            })

    }

    private
    val navigationItemSelectedListener =
        NavigationView.OnNavigationItemSelectedListener { item ->
            item.isChecked = false
            when (item.itemId) {
                R.id.about -> {
                    findNavController(R.id.nav_host_fragment).safeNavigate(
                        R.id.action_dashboardFragment_to_aboutFragment,
                        null,
                        NavigationAnimations.getAlphaAnimation()
                    )
                }
                R.id.settings -> {
                    findNavController(R.id.nav_host_fragment).safeNavigate(
                        R.id.action_dashboardFragment_to_settingsFragment,
                        null,
                        NavigationAnimations.getAlphaAnimation()
                    )
                }
                R.id.share -> {
                    ActionUtils.shareApp(this)
                }
            }
            item.isChecked = false
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


    private val navChangeListener =
        NavController.OnDestinationChangedListener { _, destination, arguments ->
            FirebaseCrashlytics.getInstance().log("Destination Fragment -> ${destination.label}")
            FirebaseCrashlytics.getInstance().log("Arguments -> $arguments")
        }

    override
    fun onPause() {
        super.onPause()
        findNavController(R.id.nav_host_fragment).removeOnDestinationChangedListener(
            navChangeListener
        )

    }

    override fun onResume() {
        super.onResume()
        findNavController(R.id.nav_host_fragment).addOnDestinationChangedListener(navChangeListener)
        setDrawerListeners()
    }

    override fun onRestart() {
        super.onRestart()
        setDrawerListeners()
    }

    private fun observeConnectivity() {
        val connectionLiveData = ConnectionLiveData(this)
        connectionLiveData.observe(this, { isConnected ->
            if (isConnected) {
                Toasty.success(
                    this,
                    ("Internet Connected"),
                    Toast.LENGTH_SHORT,
                    true
                ).show()
            } else {
                Toasty.error(
                    this,
                    "Internet Disconnected",
                    Toast.LENGTH_SHORT,
                    true
                ).show()
            }
        })
    }

}