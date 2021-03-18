package com.example.myapplication

import android.util.Log
import android.view.View
import com.shawnlin.numberpicker.NumberPicker

fun NumberPicker.setOnValueChanged(action: (View, Int, Int) -> Unit) {

    var pendingValue = -1
    var isPending = false
    setOnValueChangedListener { picker, oldVal, newVal ->
        pendingValue = newVal
        if (isPending) {
            Log.d("IKS", "pending $newVal")
            return@setOnValueChangedListener
        }
        isPending = true
        picker.postDelayed({
            isPending = false
            action(picker, oldVal, pendingValue)
        }, 500)
    }
}