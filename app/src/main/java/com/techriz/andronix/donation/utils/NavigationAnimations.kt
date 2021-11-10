package com.techriz.andronix.donation.utils

import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.techriz.andronix.donation.R

object NavigationAnimations {
    fun getAlphaAnimation() =
        NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

    fun NavController.safeNavigate(action: Int, arguments: Bundle?, options: NavOptions) {
        /*
        * We have to check if destination is the same as the current fragment
        */
        val destinationId = currentDestination?.getAction(action)?.destinationId
        val currentDestinationId = this.currentDestination?.id
        if (destinationId != currentDestinationId && destinationId != null) {
            navigate(action, arguments, options)
        }
    }


}

fun View.makeVisible() {
    this.visibility = View.VISIBLE
}

fun View.makeInvisible() {
    this.visibility = View.INVISIBLE
}

fun View.makeGone() {
    this.visibility = View.GONE
}