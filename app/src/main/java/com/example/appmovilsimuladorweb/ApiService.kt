package com.example.appmovilsimuladorweb

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("https://backend.talionis.eu:8443/api/notificacion")
    fun  getNotificacion(@Query("email") email: String?): Call<Any>


    @POST("https://backend.talionis.eu:8443/api/accounts/emailcheck")
    fun checkEmail(@Body email: EmailRequest): Call<Boolean>

}