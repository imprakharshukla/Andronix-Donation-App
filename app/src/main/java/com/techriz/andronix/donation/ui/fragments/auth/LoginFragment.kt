package com.techriz.andronix.donation.ui.fragments.auth

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.text.method.LinkMovementMethod
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.techriz.andronix.donation.R
import com.techriz.andronix.donation.databinding.LoginFragmentBinding
import com.techriz.andronix.donation.repository.AuthStateClass
import com.techriz.andronix.donation.repository.authLog
import com.techriz.andronix.donation.utils.ActionUtils
import com.techriz.andronix.donation.utils.ActionUtils.showSnackbar
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_PRIVACY
import com.techriz.andronix.donation.utils.Constants.ANDRONIX_TERMS
import com.techriz.andronix.donation.utils.NavigationAnimations
import com.techriz.andronix.donation.utils.NavigationAnimations.safeNavigate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import studio.com.techriz.andronix.ui.Loader
import studio.com.techriz.andronix.ui.PasswordResetFragment
import javax.inject.Inject

const val GOOGLE_SIGN_IN_CODE = 103

@AndroidEntryPoint
@InternalCoroutinesApi
@ExperimentalCoroutinesApi
class LoginFragment : Fragment(R.layout.login_fragment) {


    val viewModel: LoginViewModel by viewModels()

    lateinit var binding: LoginFragmentBinding
    private lateinit var loginStateFetchJob: Job


    @Inject
    lateinit var passwordResetSheet: PasswordResetFragment

    @Inject
    lateinit var loader: Loader

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = LoginFragmentBinding.bind(view)

        loginStateFetchJob = lifecycleScope.launch { fetchLoginState() }

        setPolicyTextView()

        binding.forgotPasswordText.setOnClickListener {
            passwordResetSheet.show(
                requireActivity().supportFragmentManager,
                passwordResetSheet.tag
            )
        }

        binding.anonSignInButton.setOnClickListener {
            loader.startLoader()
            viewModel.loginAnonymously()
        }

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.emailLoginButton.setOnClickListener {
            loader.startLoader()
            val emailFromInput = binding.emailInput.text.toString()
            val passwordFromInput = binding.passInput.text.toString()
            viewModel.signInWithEmail(emailFromInput, passwordFromInput)
        }

        binding.googleSignInButton.setOnClickListener {
            loader.startLoader()
            startActivityForResult(viewModel.getGoogleSignInIntent(), GOOGLE_SIGN_IN_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("requestCode $requestCode")
        lifecycleScope.launch(Dispatchers.IO) {
            if (requestCode == GOOGLE_SIGN_IN_CODE) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    println("ACCOUNT $account")
                    account?.let { viewModel.signInWithGoogle(it, "login") }
                } catch (e: ApiException) {
                    e.message.toString().authLog()
                    "Error! Couldn't sign in user with Google".showSnackbar(binding.root, true)
                }
            }
        }
    }


    private fun setPolicyTextView() {
        binding.terms.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text =
                viewModel.getTermSpans({ ActionUtils.getBrowser(requireContext(), ANDRONIX_TERMS) },
                    { ActionUtils.getBrowser(requireContext(), ANDRONIX_PRIVACY) })

        }
    }


    private suspend fun fetchLoginState() {
        viewModel.getLoginState()
            .onCompletion {
                viewModel.resetAuthState()
            }.collect {
                when (it) {
                    is AuthStateClass.Error -> {
                        "Login failed".authLog()
                        it.message.apply {
                            "Auth error - $this".authLog()
                            this.showSnackbar(binding.root, true)
                        }
                        loader.stopLoading()
                    }
                    is AuthStateClass.Success -> {
                        findNavController().safeNavigate(
                            R.id.action_loginFragment_to_dashboardFragment,
                            null,
                            NavigationAnimations.getAlphaAnimation()
                        )
                        loader.stopLoading()
                    }
                    is AuthStateClass.InputError -> {
                        "Auth error - ${it.message}"
                        when {
                            it.message.contains("email") -> {
                                binding.emailInput.error = it.message
                                it.message.showSnackbar(binding.root, true)
                            }
                            it.message.contains("password") -> {
                                it.message.showSnackbar(binding.root, true)
                                binding.passInput.error = it.message
                            }
                        }
                        loader.stopLoading()
                    }
                    else -> Unit
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::loginStateFetchJob.isInitialized) {
            loginStateFetchJob.cancel()
        }
    }

    override fun onStop() {
        super.onStop()
        if (this::loginStateFetchJob.isInitialized) {
            loginStateFetchJob.cancel()
        }
    }

}
