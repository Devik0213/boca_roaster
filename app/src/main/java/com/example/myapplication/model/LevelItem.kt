package com.example.myapplication.model

import com.anychart.chart.common.dataentry.ValueDataEntry

data class LevelItem(
    val time: Long,
    val value: Float
): ValueDataEntry(time, value)

data class TemperatureItem(
    val time : Long,
    val value : Int
): ValueDataEntry(time, value)