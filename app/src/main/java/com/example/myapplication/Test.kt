package com.example.myapplication

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.helper.ToastHelper
import kotlinx.coroutines.*

fun RoastingActivity.action() {
    lifecycleScope.launch {
        var result0 = 0
        async {
            for (i in 0..10) {
                delay(10)
            }
            result0 = 100
            Log.d("iks", "result 0 in async : $result0") // 100
        }
        Log.d("iks", "result 0 : $result0") // 0


        val result1 = async(Dispatchers.IO) {
            for (i in 0..10) {
                delay(10)
            }
            100
        }
        Log.d("iks", "result 1 : ${result1.await()}") // 100
//
//
//        val result2 = withContext(Dispatchers.IO) {
//            for (i in 0..10) {
//                delay(10)
//            }
//            100
//        }
//        Log.d("iks", "result 2 : $result2") // 100
//
//
//        val result3 = withContext(Dispatchers.IO) {
//            val a = async {
//                100
//            }
//            val b = async {
//                100
//            }
//            a.await() + b.await()
//        }
//        Log.d("iks", "result 3 : $result3") // 200
    }
}