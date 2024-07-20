package com.temp.lifestylegps

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstanceKey {
    private const val BASE_URL_LIFESTYLEGPS = "https://lifestylegps.com/"
    val retrofitLifestyleGPS: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_LIFESTYLEGPS)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}