package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRoatingBinding
import com.example.myapplication.model.Event
import com.example.myapplication.model.Point
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.util.Timer
import kotlin.concurrent.timer

class RoatingActivity : AppCompatActivity() {

    companion object {
        const val INDEX_TEMP = 0
        const val INDEX_LEVEL = 1
        const val MILLISECOND = 1000L
        const val TERM = 3
    }

    private lateinit var timeJob: Timer
    private var progressTime: Long = 0L
    private var startTime: Long = 0L
    private var pendingPoint: Point? = null

    private lateinit var decimalFormat: DecimalFormat
    private lateinit var adapter: ListAdapter
    private lateinit var binding: ActivityRoatingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        decimalFormat = DecimalFormat("00")
        binding = ActivityRoatingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intChart(binding.chart)
        binding.control.setOnClickListener {
            if (it.isActivated) {
                it.isActivated = false
                stopTimer()
            } else {
                it.isActivated = true
                startTimer()
            }
        }
        binding.temperatureDegree.setOnValueChanged { picker, oldVal, newVal ->
            Log.d("onChanged", "temp : $newVal")
            pendingPoint(null, newVal, Event.NONE)
        }
        binding.temperatureLevel.setOnValueChanged { picker, oldVal, newVal ->
            Log.d("onChanged", "level : $newVal")
            pendingPoint(newVal, null, Event.NONE)
        }
        adapter = ListAdapter(this)
        binding.recyclerView.adapter = adapter

    }

    private fun intChart(chart: CombinedChart) {
        chart.apply {
            setBackgroundColor(resources.getColor(R.color.rst_grey1))
            setTouchEnabled(false)
            setScaleEnabled(true)
            setPinchZoom(false)

            val l = chart.legend
            l.isWordWrapEnabled = true
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
            xAxis.let {
                it.position = XAxis.XAxisPosition.BOTH_SIDED
                it.mAxisMaximum = 20f
                it.labelCount = 20
                it.axisMinimum = 0f
                it.granularity = 1f
                it.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return super.getFormattedValue(value)
                    }
                }
            }
            axisLeft.let {
                it.setDrawGridLines(false)
                it.mAxisMaximum = 250f
                it.axisMinimum = 0f
            }
            axisRight.let {
                it.setDrawGridLines(false)
                it.mAxisMaximum = 20f
                it.axisMinimum = 0f
            }
            setDrawGridBackground(true)

            data = CombinedData()
            invalidate()
        }
    }

    private fun pendingPoint(pendingLevel: Int?, newVal: Int?, event: Event?) {
        if (pendingPoint == null) {
            pendingPoint = Point(
                -1,
                binding.temperatureLevel.value,
                binding.temperatureDegree.value,
                startTime,
                0,
                Event.INPUT
            )
        }
        pendingLevel?.let {
            pendingPoint =
                pendingPoint?.copy(level = it, time = startTime, processTime = progressTime)
        }
        newVal?.let {
            pendingPoint = pendingPoint?.copy(temperature = it, time = startTime)
        }
        event?.let {
            pendingPoint = pendingPoint?.copy(event = it, time = startTime)
        }
        pendingPoint?.let {
            addList(it)
        }
    }

    private fun addList(it: Point) {
        adapter.add(it)
    }

    var index = 0
    private fun writePoint() {
        if (binding.control.isActivated.not()) {
            return
        }
        pendingPoint?.let {
            val recordPoint = it.copy(index = index, processTime = progressTime)
            addList(recordPoint)
            renderChart(recordPoint)
            index++
            if (index == 20) {
                stopTimer()
            }
        }
    }

    private fun startTimer() {
        startTime = 0L
        binding.control.isActivated = true
        timeJob = timer("timer", false, 0, MILLISECOND) {
            if (startTime == 0L) {
                startTime = scheduledExecutionTime()
            }
            progressTime = (scheduledExecutionTime() - startTime) / MILLISECOND
            if (progressTime < 1L) {
                runOnUiThread {
                    pendingPoint(null, null, event = Event.INPUT)
                }
            }
            binding.timer.text =
                "${decimalFormat.format(progressTime / 60)}:${decimalFormat.format(progressTime % 60)}"
            if (progressTime % TERM == 0L) {
                runOnUiThread {
                    writePoint()
                }
            }
        }
    }

    private fun stopTimer() {
        adapter.clear()
        binding.control.isActivated = false
        timeJob.cancel()
    }

    fun renderChart(pendingPoint: Point) {
        binding.chart.apply {
            if (data.dataSetCount == 0) {
                val temperatureList = adapter.list.filter { it.index != -1 }
                    .mapIndexed { index, point ->
                        Entry(
                            index.toFloat(),
                            point.temperature.toFloat()
                        )
                    }
                val levelList = adapter.list.filter { it.index != -1 }
                    .mapIndexed { index, point -> BarEntry(index.toFloat(), point.level.toFloat()) }
                val temperatureSet = LineDataSet(temperatureList, "temperatures").also { set ->
                    set.setColor(Color.rgb(240, 238, 70))
                    set.setLineWidth(2.5f)
                    set.setCircleColor(Color.rgb(240, 238, 70))
                    set.setCircleRadius(5f)
                    set.setFillColor(Color.rgb(240, 238, 70))
                    set.setMode(LineDataSet.Mode.CUBIC_BEZIER)
                    set.setDrawValues(true)
                    set.setValueTextSize(10f)
                    set.setValueTextColor(Color.rgb(240, 238, 70))
                    set.setAxisDependency(AxisDependency.LEFT)
                }

                val levelSet = BarDataSet(levelList, "levels").also { set ->
                    set.setColor(Color.rgb(60, 220, 78))
                    set.setValueTextColor(Color.rgb(60, 220, 78))
                    set.setValueTextSize(10f)
                    set.axisDependency = AxisDependency.RIGHT
                }

                data.setData(LineData().apply {
                    addDataSet(temperatureSet)
                })
                data.setData(BarData().apply {
                    barWidth = .3f
                    addDataSet(levelSet)
                })
                xAxis.axisMaximum = data.xMax + 0.25f

                data = data
            } else {
                data.getDataByIndex(INDEX_TEMP).apply {
                    addEntry(
                        Entry(
                            pendingPoint.index.toFloat(),
                            pendingPoint.temperature.toFloat()
                        ), entryCount - 1
                    )
                    notifyDataChanged()
                    notifyDataSetChanged()
                }
                data.getDataByIndex(INDEX_LEVEL).apply {
                    addEntry(
                        Entry(
                            pendingPoint.index.toFloat(),
                            pendingPoint.level.toFloat()
                        ), entryCount - 1
                    )
                    notifyDataChanged()
                }
            }
            data.notifyDataChanged()
            notifyDataSetChanged()
            invalidate()
        }
    }
}