package com.example.qrscannerapp.ui

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    private var ID: String? = ""

    fun getLocationId(): String? {
        return ID
    }

    fun saveLocationId(locId: String?) {
        ID = locId
    }
}