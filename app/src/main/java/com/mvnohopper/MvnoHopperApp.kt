package com.mvnohopper

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MvnoHopperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}
