package com.example.project

import android.app.Application
import com.example.project.network.RetrofitClient

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        RetrofitClient.initialize(this)
    }
}