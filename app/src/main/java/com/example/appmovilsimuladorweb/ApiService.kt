package com.example.appmovilsimuladorweb

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("https://backend.talionis.eu:8443/api/notificacion")
    fun  getNotificacion(@Query("email") email: String?): Call<Any>

}