package com.example.myapplication.model

import com.anychart.chart.common.dataentry.ValueDataEntry


data class LevelItem(
    val time: Long,
    val value: Float
) : ValueDataEntry(time, value)

