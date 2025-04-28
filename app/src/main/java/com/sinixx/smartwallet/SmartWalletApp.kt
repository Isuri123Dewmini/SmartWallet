package com.sinixx.smartwallet

import android.app.Application
import android.content.Context

class SmartWalletApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private var instance: SmartWalletApp? = null

        fun context(): Context {
            return instance!!.applicationContext
        }
    }
}