package com.techriz.andronix.donation.api

import retrofit2.Call
import retrofit2.http.GET

interface CommerceStatusAPI {
    @GET("status")
    fun getServerStatus(): Call<StatusData?>
}


class StatusData {
    var response: String? = null
}