package com.example.myapplication.model

import com.anychart.chart.common.dataentry.ValueDataEntry


data class LevelItem(
    val time: String,
    val value: Int
) : ValueDataEntry(time, value)

