package com.techriz.andronix.donation.ui.fragments.commerce

import android.text.SpannableString
import android.text.Spanned
import android.text.style.ClickableSpan
import android.view.View
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.SkuDetails
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.techriz.andronix.donation.repository.BillingRepository
import com.techriz.andronix.donation.repository.PurchaseStateClass
import com.techriz.andronix.donation.utils.ActionUtils.log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PayViewModel @Inject constructor(
    private val billingRepository: BillingRepository,
) : ViewModel() {

    lateinit var skuDetails: SkuDetails
    private var _purchaseState = MutableStateFlow<PurchaseStateClass>(PurchaseStateClass.Empty)
    var purchaseState: StateFlow<PurchaseStateClass> = _purchaseState


    var selectedSku: String = ""


    @InternalCoroutinesApi
    @ExperimentalCoroutinesApi
    fun initBilling(): SharedFlow<PurchaseStateClass> {
        billingRepository.startConnection()
        return billingRepository.purchaseState
    }

    fun resetBillingState() = billingRepository.resetBillingState()

    @ExperimentalCoroutinesApi
    fun getFlowDetails(): FlowDetails {
        val uid = Firebase.auth.currentUser?.uid
        var email = Firebase.auth.currentUser?.email
        if (!uid.isNullOrEmpty() && email.isNullOrEmpty()) {
            //giving this a temporary email is since the signing is anonymous
            email = "anonymous@sign.in"
        }
        return if (!uid.isNullOrEmpty() && !email.isNullOrEmpty()) {
            println("SKU details $skuDetails")
            println("Email $email")
            println("Uid details $uid")
            FlowDetails(true, uid, email, skuDetails)
        } else {
            "User not logged in".log()
            FlowDetails(false, "", "", null)
        }
    }


    fun isSkuDetailInit(): Boolean {
        return this::skuDetails.isInitialized
    }

    fun getSupportSpannableTextSuccess(text: String, run: () -> Unit): SpannableString {
        val ss = SpannableString(text)
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                run()
            }
        }
        ss.setSpan(
            clickableSpan1,
            ss.indexOf("support@andronix.app"),
            ss.length - 1,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }


    fun getSupportSpannableTextFailed(text: String, run: () -> Unit): SpannableString {
        val ss = SpannableString(text)
        val clickableSpan1: ClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                run()
            }
        }
        ss.setSpan(
            clickableSpan1,
            ss.indexOf("support@andronix.app"),
            ss.indexOf("support@andronix.app") + 20,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }

}


data class FlowDetails(
    var isAllowed: Boolean = false,
    var uid: String = "",
    var email: String = "",
    var skuDetails: SkuDetails?,
)