package com.temp.lifestylegps

import retrofit2.Call
import retrofit2.http.GET

interface ApiServiceKey {
    @GET("api_key.txt")
     fun getApiKey(): Call<ApiKeyResponse>
}