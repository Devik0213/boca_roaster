package com.example.myapplication.helper

import android.content.Context
import java.text.DecimalFormat
import java.text.SimpleDateFormat

object Formatter {
    var shortDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd")
    var fullDateFormat: SimpleDateFormat = SimpleDateFormat("yyyy/MM/dd-HH:mm")
    val decimalFormat: DecimalFormat = DecimalFormat("00")
    fun processTime(time: Long): String {
        return "${decimalFormat.format(time / 60)}:${decimalFormat.format(time % 60)}"
    }

}