package com.example.qrscannerapp.network

import androidx.lifecycle.MutableLiveData
import com.example.qrscannerapp.data.SessionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class RetroRepository @Inject constructor(private val apiInstance: ApiServices) {

    fun safeApiCall(
        locationId: String,
        timeSpent: Int,
        endTime: String,
        liveDataList: MutableLiveData<SessionResponse>
    ) {
        val call: Call<SessionResponse> = apiInstance.fetchSession(locationId, timeSpent, endTime)

        call.enqueue(object : Callback<SessionResponse> {
            override fun onResponse(
                call: Call<SessionResponse>,
                response: Response<SessionResponse>
            ) {
                liveDataList.postValue(response.body())
            }

            override fun onFailure(call: Call<SessionResponse>, t: Throwable) {
                liveDataList.postValue(null)
            }
        })
    }
}