package com.techriz.andronix.donation.ui.fragments

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
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
        val email = getUserEmail() ?: (('a'..'z').random() + (0..1000).random()
            .toString() + ('a'..'z').random() + ('a'..'z').random())
        return Firebase.auth.currentUser?.photoUrl?.toString()
            ?: "https://avatars.dicebear.com/api/jdenticon/$email.svg"
    }

    fun getWarningSpans(

        text: String,
        downloadLinkFunction: () -> Unit,
    ): SpannableString {
        val ss = SpannableString(text)
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                downloadLinkFunction()
            }
        }
        val styleSpan = StyleSpan(android.graphics.Typeface.BOLD)  // Span to make text bold

        ss.setSpan(
            clickableSpan1,
            ss.indexOf("here"),
            ss.indexOf("here") + 4,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        ss.setSpan(
            styleSpan,
            ss.indexOf("and"),
            ss.indexOf("App"),
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }


}