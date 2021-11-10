package com.techriz.andronix.donation.utils

import java.util.regex.Pattern

object AuthVerificationUtils {
    fun isConPassValid(pass: String, confirmPass: String): Boolean {

        return if (pass.isEmpty() || confirmPass.isEmpty())
            false
        else
            return pass.length >= 6 && pass == confirmPass
    }

    fun isPasswordValid(pass: String): Boolean {
        return pass.length >= 6
    }

    fun isEmailValid(email: String): Boolean {
        val pattern = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-+]+)*@[A-Za-z0-9-+]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val matcher = pattern.matcher(email)
        return if (email.isEmpty()) {
            false
        } else matcher.matches()
    }

}