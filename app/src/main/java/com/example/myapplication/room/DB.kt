package com.example.myapplication.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RoastInfo::class], version = 1)
abstract class DB : RoomDatabase() {

    companion object {
        lateinit var instance: DB
        private const val NAME = "roasting.db"
        fun init(context: Context) {
            instance = Room.databaseBuilder(context, DB::class.java, NAME).build()
        }
    }

    abstract fun roastInfoDao(): RoastInfoDao
}