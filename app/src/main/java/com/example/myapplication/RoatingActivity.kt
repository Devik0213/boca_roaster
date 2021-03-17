package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.core.axes.Linear
import com.anychart.data.Set
import com.anychart.enums.Orientation
import com.anychart.enums.ScaleTypes
import com.anychart.enums.TextParsingMode
import com.example.myapplication.databinding.ActivityRoatingBinding
import com.example.myapplication.model.Event
import com.example.myapplication.model.LevelItem
import com.example.myapplication.model.RPoint
import com.example.myapplication.model.TemperatureItem
import kotlin.math.max

class RoatingActivity : AppCompatActivity() {

    companion object {
        const val INDEX_TEMP = 0
        const val INDEX_LEVEL = 1
    }

    private lateinit var binding: ActivityRoatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoatingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val pointList = arrayListOf<RPoint>()
        val levelList = arrayListOf<LevelItem>()
        val temperList = arrayListOf<TemperatureItem>()

        for (i in 0..15) {
            val currentTime = i * 1L//System.currentTimeMillis() / 1000L + i*1000
            val li = LevelItem(currentTime, (15 - i).toFloat())
            val ti = TemperatureItem(currentTime, i * 10)
            pointList.add(RPoint(li, ti, Event.values()[i % 5]))
            temperList.add(ti)
            levelList.add(li)
        }


        val chart = AnyChart.line()
        chart.apply {
            title("시간별 온도 추이")

            yAxis(INDEX_TEMP).apply {
                title("온도")
                orientation(Orientation.LEFT)
                scale(yScale().apply {
                    minimum(0)
                    maximum(250)
                })
            }

            val extraScale = AnyChart.line().yScale().apply {
                maximum(20)
                minimum(0)
            }
            yAxis(INDEX_LEVEL).apply {
                title("화력 레벨")
                orientation(Orientation.RIGHT)
                scale(extraScale)
            }

            xAxis("시간")

            line(temperList.toList()).apply {
                yScale(chart.yScale())
            }

            column(levelList.toList()).apply {
                yScale(extraScale)
            }
        }
        binding.chart.setChart(chart)

        // https://github.com/milosmns/actual-number-picker 구현
    }
}