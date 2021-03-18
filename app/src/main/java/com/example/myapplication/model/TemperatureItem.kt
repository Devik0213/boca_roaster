package com.example.myapplication.model

import com.anychart.chart.common.dataentry.ValueDataEntry

data class TemperatureItem(
    val time: String,
    val value: Int
) : ValueDataEntry(time, value)