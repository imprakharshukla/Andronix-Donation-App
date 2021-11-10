package com.techriz.andronix.donation.repository

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.techriz.andronix.donation.utils.AuthVerificationUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val signInIntent: Intent
) {

    private val firebaseAuth = Firebase.auth


    private var _loginState = MutableStateFlow<AuthStateClass>(AuthStateClass.Empty)
    var loginState: StateFlow<AuthStateClass> = _loginState


    private var _passwordResetState = MutableStateFlow<SuccessFail>(SuccessFail.Empty)
    var passwordResetState: StateFlow<SuccessFail> = _passwordResetState


    fun getSignInWithGoogleIntent(): Intent {
        return signInIntent
    }

    fun resetAuthState() {
        _loginState.value = AuthStateClass.Empty
    }

    fun sendForgotPasswordEmail(email: String) {
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener {
            _passwordResetState.value = SuccessFail.Success
        }.addOnFailureListener {
            _passwordResetState.value = SuccessFail.Error(it.toString())
        }
    }

    suspend fun firebaseAuthWithGoogle(acct: GoogleSignInAccount, type: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(acct.idToken, null)
        try {
            firebaseAuth.signInWithCredential(credential).await()
            val user = firebaseAuth.currentUser
            println(user)
            if (user != null) {
                println("User not null. Sign in success")
                _loginState.value =
                    AuthStateClass.Success(user)
            } else {
                println(
                    "User null no success"
                )
                _loginState.value =
                    AuthStateClass.Error("Error logging in user with Google!")
            }

        } catch (e: java.lang.Exception) {
            println(
                "User null no success $e"
            )
            _loginState.value =
                AuthStateClass.Error(e.message.toString())
        }
    }

    suspend fun loginAnony() {
        firebaseAuth.signInAnonymously().await()
        try {
            if (firebaseAuth.currentUser != null)
                _loginState.value = AuthStateClass.Success(firebaseAuth.currentUser!!)
            else _loginState.value =
                AuthStateClass.Error("User null or empty!")
        } catch (e: Exception) {
            _loginState.value = AuthStateClass.Error(e.message.toString())
        }
    }

    suspend fun loginWithEmail(email: String, pass: String) {
        if (AuthVerificationUtils.isEmailValid(email)) {
            "Email valid".authLog()
            if (AuthVerificationUtils.isPasswordValid(pass)) {
                "Password valid".authLog()
                try {
                    firebaseAuth.signInWithEmailAndPassword(email, pass).await()
                    "Logging in now".authLog()
                    if (firebaseAuth.currentUser != null) _loginState.value =
                        AuthStateClass.Success(firebaseAuth.currentUser!!) else _loginState.value =
                        AuthStateClass.Error("User null or empty!")
                } catch (e: FirebaseAuthWeakPasswordException) {
                    _loginState.value = AuthStateClass.Error("Sign in failed.Password too weak.")
                } catch (e: FirebaseAuthInvalidCredentialsException) {
                    _loginState.value =
                        AuthStateClass.Error("Sign in failed \n Invalid email or password")
                } catch (e: FirebaseAuthUserCollisionException) {
                    _loginState.value =
                        AuthStateClass.Error("Oops! Seems like another user has already registered an account with this email. Please try a different email.")
                } catch (e: Exception) {
                    _loginState.value = AuthStateClass.Error(e.message.toString())
                }
            } else {
                _loginState.value =
                    AuthStateClass.InputError("Invalid password! Please enter a password at least 6 characters long.")
            }
        } else
            _loginState.value =
                AuthStateClass.InputError("Invalid email! Please enter an email with a valid format. Eg. johndoe@example.com")

    }

}

sealed class AuthStateClass {
    object Empty : AuthStateClass()
    data class Success(val user: FirebaseUser) : AuthStateClass()
    data class Error(val message: String) : AuthStateClass()
    data class InputError(val message: String) : AuthStateClass()
}

sealed class SuccessFail {
    object Empty : SuccessFail()
    object Success : SuccessFail()
    data class Error(val error: String) : SuccessFail()
}


fun String.authLog() {
    Log.d("ANDRONIX AUTH", this)
}
