package com.techriz.andronix.donation.ui.fragments

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    fun isUserLoggedIn(): Boolean {
        return Firebase.auth.currentUser != null
    }

    fun getUserEmail(): String? {
        return Firebase.auth.currentUser?.email
    }

    fun getUserName(): String {
        val name = Firebase.auth.currentUser?.displayName
        return when {
            name == null && getUserEmail() == null && isUserLoggedIn() -> "Anonymously Authenticated"
            name == null && getUserEmail() != null -> "Email Authenticated"
            name != null && name.isNotEmpty() -> name.toString()
            else -> "Email Authenticated"
        }
    }

    fun getUserPfp(): String {
        val email = getUserEmail() ?: ('a'..'z').random() + (0..1000).random()
            .toString() + ('a'..'z').random() + ('a'..'z').random()
        return Firebase.auth.currentUser?.photoUrl?.toString()
            ?: "https://avatars.dicebear.com/api/jdenticon/$email.svg"
    }


}