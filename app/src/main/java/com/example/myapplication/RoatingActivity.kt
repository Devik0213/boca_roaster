package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.enums.Orientation
import com.example.myapplication.databinding.ActivityRoatingBinding
import com.example.myapplication.model.Event
import com.example.myapplication.model.LevelItem
import com.example.myapplication.model.RPoint
import com.example.myapplication.model.TemperatureItem
import java.text.DecimalFormat
import java.util.Timer
import kotlin.concurrent.timer

class RoatingActivity : AppCompatActivity() {

    companion object {
        const val INDEX_TEMP = 0
        const val INDEX_LEVEL = 1
        const val MILLISECOND = 1000L
    }

    private lateinit var decimalFormat: DecimalFormat
    private lateinit var adapter: ListAdapter
    private lateinit var timeJob: Timer
    private lateinit var timer: Timer
    private lateinit var binding: ActivityRoatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        decimalFormat = DecimalFormat("00")
        binding = ActivityRoatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.timer.setOnClickListener {
            if (it.isActivated) {
                it.isActivated = false
                stopTimer()
            } else {
                it.isActivated = true
                startTimer()
            }
        }
        adapter = ListAdapter(this)
        binding.recyclerView.adapter = adapter

        binding.write.setOnClickListener {
            writePoint()
        }

    }

    private fun writePoint() {
        if (binding.write.isActivated.not()) {
            return
        }
        val level = binding.temperatureLevel.value
        val degree = binding.temperatureDegree.value
        val time = binding.timer.text.toString()
        val li = LevelItem(time, level)
        val ti = TemperatureItem(time, degree)
        adapter.add(RPoint(li, ti, Event.NONE))
        renderChart(li, ti)
    }

    private fun startTimer() {
        var startTime = 0L
        binding.write.isActivated = true
        timeJob = timer("timer", false, 0, MILLISECOND) {
            if (startTime == 0L) {
                startTime = scheduledExecutionTime()
            }
            val time = (scheduledExecutionTime() - startTime) / MILLISECOND
            binding.timer.text =
                "${decimalFormat.format(time / 60)}:${decimalFormat.format(time % 60)}"
        }
    }

    private fun stopTimer() {
        adapter.clear()
        binding.write.isActivated = false
        timeJob.cancel()
        levelList.clear()
        temperList.clear()
    }

    val levelList = arrayListOf<LevelItem>()
    val temperList = arrayListOf<TemperatureItem>()
    fun renderChart(level: LevelItem, degree: TemperatureItem) {

        levelList.add(level)
        temperList.add(degree)

        val chart = AnyChart.line()
        //TODO char manipulate : https://docs.anychart.com/Working_with_Data/Data_Sets
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
        binding.chart.post {
            binding.chart.setChart(chart)
        }
    }
}