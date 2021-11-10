package com.techriz.andronix.donation.repository

import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.model.ServerTimestamps
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PurchaseRepository {

    private val _uploadState =
        MutableStateFlow<ProductUploadStateClass>(ProductUploadStateClass.Empty)
    val uploadState: StateFlow<ProductUploadStateClass> = _uploadState


    //id, token, email, timestamp
    //   firebaseFirestore.collection("donations").document(uid).collection("donations")

    fun addDonation(donationUploadData: DonationUploadData) {
        try {
            Firebase.firestore.collection("donations").document(donationUploadData.uid)
                .collection("donations")
                .add(donationUploadData)
                .addOnSuccessListener {
                    _uploadState.value = ProductUploadStateClass.Uploaded
                    println("Uploaded Prem")
                }
                .addOnFailureListener { e ->
                    println("Firebase error Prem $e")
                    _uploadState.value = ProductUploadStateClass.Error("Premium upload failed $e")
                }

        } catch (e: FirebaseFirestoreException) {
            println("Firebase error $e")
            _uploadState.value = ProductUploadStateClass.Error("Donation upload failed $e")
        }
    }


}

data class DonationUploadData(
    var id: String = "",
    var token: String = "",
    var uid: String = "",
    var email: String = "",
    var timestamps: FieldValue?
)


fun String.purchaseLog(error: Boolean = false) {
    Log.d("ANDRONIX PURCHASES", this)
}

sealed class ProductUploadStateClass {
    object Empty : ProductUploadStateClass()
    object Uploaded : ProductUploadStateClass()
    data class Error(var message: String) : ProductUploadStateClass()
}
