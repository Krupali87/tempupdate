package com.temp.lifestylegps

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api.php")
    fun getTempData(
        @Query("api") api: String,
        @Query("key") Key: String,
        @Query("cmd") cmd: String,

        ): Call<List<TempData>>

       // Use your data class

}