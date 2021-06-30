package com.example.myapplication

import android.util.Log
import android.widget.EditText
import com.shawnlin.numberpicker.NumberPicker
import java.lang.Exception

fun NumberPicker.setOnValueChanged(
    pick: () -> Unit,
    sliding: () -> Unit,
    action: (NumberPicker, Int, Int) -> Unit
) {

    var pendingValue = -1
    var isPending = false

    setOnValueChangedListener { picker, oldVal, newVal ->
        pendingValue = newVal
        sliding()
        if (isPending) {
            Log.d("IKS", "pending $newVal")
            return@setOnValueChangedListener
        }
        isPending = true
        picker.postDelayed({
            pick()
            isPending = false
            action(picker, oldVal, pendingValue)
        }, 500)
    }
}

fun EditText.getString(): String? {
    return this.editableText.toString()
}

fun String.safeIntOrZero(): Int {
    return try {
        toInt()
    } catch (e: Exception) {
        0
    }
}
