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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.DecimalFormat
import java.util.Timer
import kotlin.concurrent.timer

class RoastingActivity : AppCompatActivity() {

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
        initClicks()

        binding.temperatureDegree.setOnValueChanged { picker, oldVal, newVal ->
            Log.d("onChanged", "temper : $newVal")
            pendingPoint(null, newVal, Event.NONE)
        }
        binding.temperatureLevel.setOnValueChanged { picker, oldVal, newVal ->
            Log.d("onChanged", "level : $newVal")
            pendingPoint(newVal, null, Event.NONE)
        }
        adapter = ListAdapter(this)
        binding.recyclerView.adapter = adapter

    }

    private fun initClicks() {
        with(binding) {
            control.setOnClickListener {
                if (it.isActivated) {
                    it.isActivated = false
                    stopTimer()
                } else {
                    it.isActivated = true
                    startTimer()
                }
            }
            levelPlus.setOnClickListener {
                temperatureLevel.value--
                pendingPoint(temperatureLevel.value, null, Event.NONE)
            }
            levelMinus.setOnClickListener {
                temperatureLevel.value++
                pendingPoint(temperatureLevel.value, null, Event.NONE)
            }
            temperaturePlus.setOnClickListener {
                temperatureDegree.value += 10
                pendingPoint(null, temperatureDegree.value, Event.NONE)

            }
            temperatureMinus.setOnClickListener {
                temperatureDegree.value -= 10
                pendingPoint(null, temperatureDegree.value, Event.NONE)
            }
        }
    }

    private fun intChart(chart: CombinedChart) {
        chart.apply {
            setBackgroundColor(resources.getColor(R.color.rst_grey1))

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

    private fun pendingPoint(level: Int?, temper: Int?, event: Event?) {
        if (pendingPoint == null) {
            Log.d("init point", " $level, $temper, $event")
            pendingPoint = Point(
                0,
                temper ?: binding.temperatureLevel.value,
                level ?: binding.temperatureDegree.value,
                startTime,
                0,
                Event.INPUT
            )
        }
        level?.let {
            pendingPoint =
                pendingPoint?.copy(level = it, time = startTime, processTime = progressTime)
        }
        temper?.let {
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
                    .mapIndexed { index, point ->
                        BarEntry(
                            index.toFloat(),
                            point.level.toFloat()
                        )
                    }
                val temperatureSet = LineDataSet(temperatureList, "temperatures").also { set ->
                    set.setColor(Color.rgb(255, 0, 0))
                    set.setLineWidth(2.5f)
                    set.setCircleColor(Color.rgb(240, 238, 70))
                    set.mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    set.cubicIntensity = .5f
                    set.valueTextSize = 15f
                    set.setValueTextSize(10f)
                    set.setValueTextColor(Color.rgb(255, 0, 0))
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
                            entryCount.toFloat(),
                            pendingPoint.temperature.toFloat()
                        ), 0
                    )
                    notifyDataSetChanged()
                }
                data.getDataByIndex(INDEX_LEVEL).apply {
                    addEntry(
                        BarEntry(
                            entryCount.toFloat(),
                            pendingPoint.level.toFloat()
                        ), 0
                    )
                    notifyDataSetChanged()
                }
            }

            data.notifyDataChanged()
            xAxis.axisMaximum = data.getDataByIndex(INDEX_LEVEL).entryCount.toFloat()
            notifyDataSetChanged()
            invalidate()
            moveViewToX(data.entryCount.toFloat())
        }
    }
}