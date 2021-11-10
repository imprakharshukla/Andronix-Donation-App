package com.techriz.andronix.donation.ui.fragments.auth

import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.techriz.andronix.donation.repository.AuthRepository
import com.techriz.andronix.donation.repository.AuthStateClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {


    fun loginAnonymously() {
        viewModelScope.launch { authRepository.loginAnony() }
    }

    fun signInWithEmail(emailFromInput: String, passwordFromInput: String) {
        viewModelScope.launch { authRepository.loginWithEmail(emailFromInput, passwordFromInput) }
    }

    fun getLoginState(): StateFlow<AuthStateClass> {
        return authRepository.loginState
    }

    fun resetAuthState() = authRepository.resetAuthState()

    suspend fun signInWithGoogle(account: GoogleSignInAccount, type: String) {
        authRepository.firebaseAuthWithGoogle(account, type)
    }


    fun getGoogleSignInIntent(): Intent {
        return authRepository.getSignInWithGoogleIntent()
    }

    fun getTermSpans(
        termsClickFunction: () -> Unit,
        privacyClickFunction: () -> Unit
    ): SpannableString {
        val text =
            "By signing up on Andronix App you agree to our Terms and that you have read our Privacy policy, including how we handle your data."
        val ss = SpannableString(text)
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                termsClickFunction()
            }
        }
        val clickableSpan2: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                privacyClickFunction()
            }
        }
        val clickableSpan3: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                privacyClickFunction()
            }
        }
        ss.setSpan(
            clickableSpan1,
            ss.indexOf("Terms"),
            ss.indexOf("Terms") + 5,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            clickableSpan2, ss.indexOf("Privacy policy"),
            ss.indexOf("Privacy policy") + 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            clickableSpan3, ss.indexOf("how we handle your data."),
            ss.indexOf("how we handle your data.") + 24, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }


}