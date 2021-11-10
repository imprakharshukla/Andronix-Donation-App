package com.techriz.andronix.donation.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.techriz.andronix.donation.api.CommerceStatusAPI
import com.techriz.andronix.donation.api.PurchaseAPI
import com.techriz.andronix.donation.utils.ActionUtils.log
import com.techriz.andronix.donation.utils.SkuInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import javax.inject.Inject

class BillingRepository @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val purchaseAPI: PurchaseAPI,
    private val purchaseRepository: PurchaseRepository,
    private val commerceStatusAPI: CommerceStatusAPI
) {

    lateinit var billingClient: BillingClient
    val isBillingClientReady = MutableLiveData<Boolean>()
    private val _purchaseState = MutableStateFlow<PurchaseStateClass>(PurchaseStateClass.Empty)
    val purchaseState: StateFlow<PurchaseStateClass> = _purchaseState
    lateinit var uid: String
    lateinit var email: String

    private fun returnBillingClient(): BillingClient {
        return BillingClient.newBuilder(appContext)
            .enablePendingPurchases()
            .setListener(purchaseListener)
            .build()
    }

    fun resetBillingState() {
        _purchaseState.value = PurchaseStateClass.Empty
    }

    fun startConnection() {
        billingClient = returnBillingClient()
        CoroutineScope(Dispatchers.IO).launch { checkForCommerceServerStatus() }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                "Billing connection is ready!".log()
                _purchaseState.value = PurchaseStateClass.Ready
            }

            override fun onBillingServiceDisconnected() {
                "Billing service disconnected".log()
                _purchaseState.value = PurchaseStateClass.Disconnected
                isBillingClientReady.postValue(false)
            }
        })
    }

    fun launchBillingFlow(
        activity: Activity,
        skuDetails: SkuDetails,
        uid: String,
        email: String
    ): Boolean {
        this.uid = uid
        this.email = email
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val response = billingClient.launchBillingFlow(activity, flowParams).responseCode
        return response == BillingClient.BillingResponseCode.OK
    }

    suspend fun getSkuDetails() {
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        withContext(Dispatchers.IO) {
            billingClient.querySkuDetailsAsync(params.build()) { _, skuDetailsList ->
                if (!skuDetailsList.isNullOrEmpty()) {
                    _purchaseState.value = PurchaseStateClass.SkuDetails(skuDetailsList)
                } else {
                    _purchaseState.value = PurchaseStateClass.Error("Empty SKU detail list.")
                }

            }
        }
    }

    fun getSingleSkuDetails(skuDetailList: List<SkuDetails>, requiredSku: String): SkuDetails? {
        skuDetailList.forEach {
            if (it.sku == requiredSku) {
                return it
            }
        }
        return null
    }

    private val purchaseListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseState.value = PurchaseStateClass.Cancelled
        } else {
            _purchaseState.value =
                PurchaseStateClass.Error("Google Play Billing internal issues detected. Aborting purchase flow!")
        }

    }

    private suspend fun checkForCommerceServerStatus() {
        try {
            val response =
                commerceStatusAPI.getServerStatus().awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                _purchaseState.value = PurchaseStateClass.ServerStatusOK
            } else {
                _purchaseState.value = PurchaseStateClass.ServerStatusFailed
            }
        } catch (e: Exception) {
            _purchaseState.value = PurchaseStateClass.ServerStatusFailed
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                /* Consuming Purchase */
                CoroutineScope(Dispatchers.IO).launch {
                    consumePurchase(purchase)
                    verifyPurchase(purchase)
                }
            }
            Purchase.PurchaseState.PENDING -> {
                _purchaseState.value = PurchaseStateClass.Delayed
            }
            else -> {
                _purchaseState.value = PurchaseStateClass.Error("Payment verification failed!")
                "Payment verification failed".log()
            }
        }
    }


    private fun grantProduct(
        donationUploadData: DonationUploadData,
    ) {

        purchaseRepository.addDonation(donationUploadData)

        CoroutineScope(Dispatchers.IO).launch {
            purchaseRepository.uploadState.collect {
                when (it) {
                    is ProductUploadStateClass.Uploaded -> {
                        _purchaseState.value = PurchaseStateClass.Completed
                    }
                    is ProductUploadStateClass.Error -> {
                        _purchaseState.value =
                            PurchaseStateClass.Error("Purchase failed while granting entitlement. Sad noises 😞.")
                    }
                    else -> Unit
                }
            }
        }

    }


    private fun consumePurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                "Purchase consumed".log()
                _purchaseState.value = PurchaseStateClass.Consumed
            }
        }
    }

    private suspend fun verifyPurchase(purchase: Purchase) {
        try {
            val response =
                purchaseAPI.getPurchaseVerification(purchase.sku, purchase.purchaseToken, "true")
                    .awaitResponse()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.orderID.let { orderIdFromServer ->
                    if (orderIdFromServer == purchase.orderId) {
                        _purchaseState.value = PurchaseStateClass.Verified
                        grantProduct(
                            getDonationData(purchase)
                        )
                    } else
                        _purchaseState.value = PurchaseStateClass.Error("Payment unverified!")
                }
            } else
                _purchaseState.value = PurchaseStateClass.Error("Payment unverified!")
        } catch (e: java.lang.Exception) {
            _purchaseState.value = PurchaseStateClass.Error("Payment unverified!")
        }
    }

    private fun getDonationData(purchase: Purchase): DonationUploadData {
        purchase.apply {
            return DonationUploadData(
                orderId,
                purchaseToken,
                uid,
                email,
                com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
        }
    }

}

sealed class PurchaseStateClass {
    object Empty : PurchaseStateClass()
    data class Start(
        val skuDetails: com.android.billingclient.api.SkuDetails,
        val email: String,
        val uid: String
    ) : PurchaseStateClass()

    object Ready : PurchaseStateClass()
    object ServerStatusOK : PurchaseStateClass()
    object ServerStatusFailed : PurchaseStateClass()
    data class SkuDetails(val skuDetailsList: MutableList<com.android.billingclient.api.SkuDetails>) :
        PurchaseStateClass()

    data class SkuDetail(val skuDetail: com.android.billingclient.api.SkuDetails) :
        PurchaseStateClass()

    object Consumed : PurchaseStateClass()
    object Verified : PurchaseStateClass()
    object Completed : PurchaseStateClass()
    object Delayed : PurchaseStateClass()
    object Disconnected : PurchaseStateClass()
    object Cancelled : PurchaseStateClass()
    data class Error(val message: String) : PurchaseStateClass()
}

val skuList = arrayListOf(
    SkuInfo.PRIMUS().sku_id,
    SkuInfo.BLAZE().sku_id,
    SkuInfo.WARRIOR().sku_id,
    SkuInfo.SAVIOR().sku_id,
)

fun String.billingLog(error: Boolean = false) {
    Log.d("ANDRONIX BILLING", this)
}