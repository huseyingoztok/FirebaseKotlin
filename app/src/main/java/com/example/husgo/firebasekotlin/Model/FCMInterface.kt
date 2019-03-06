package com.example.husgo.firebasekotlin.Model


import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface FCMInterface {

    @POST("send")
    fun sendNotify(
            @HeaderMap headers: Map<String, String>,
            @Body notifyBody:SendNotify
    ):Call<Response<SendNotify>>
}