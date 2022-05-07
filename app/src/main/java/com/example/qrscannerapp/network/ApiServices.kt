package com.example.qrscannerapp.network

import com.example.qrscannerapp.data.SessionResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("submit-session")
    fun fetchSession(
        @Query("location_id") locationId: String,
        @Query("time_spent") timeSpent: Int,
        @Query("end_time") endTime: String
    ): Call<SessionResponse>
}