package com.example.qrscannerapp.ui.scanner

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qrscannerapp.data.SessionResponse
import com.example.qrscannerapp.network.RetroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel @Inject constructor(private val repository: RetroRepository) : ViewModel() {
    var liveDataList: MutableLiveData<SessionResponse> = MutableLiveData()

    fun sessionSuccess(
        locationId: String,
        timeSpent: Int,
        endTime: String
    ) {
        repository.safeApiCall(locationId, timeSpent, endTime, liveDataList)
    }
}