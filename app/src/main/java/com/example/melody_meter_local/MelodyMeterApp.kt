package com.example.melody_meter_local

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MelodyMeterApp: Application(){

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        // Other initialization code for your app
    }
}