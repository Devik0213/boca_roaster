package com.example.myapplication

import android.app.Application
import android.content.Context
import com.example.myapplication.room.DB

class RoastingApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        DB.init(applicationContext)
    }
}