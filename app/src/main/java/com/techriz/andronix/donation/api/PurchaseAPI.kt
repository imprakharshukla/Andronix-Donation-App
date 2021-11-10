package com.techriz.andronix.donation.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


data class OrderIDModel(val orderID: String, val sku_id: String, val order_token: String, val isDonation: String)

interface PurchaseAPI {
    @GET("/v1/gpa/verify")
    fun getPurchaseVerification(
        @Query("sku_id") sku_id: String,
        @Query("token") order_token: String,
        @Query("isDonation") isDonation: String
    ): Call<OrderIDModel>
}