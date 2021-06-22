package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityRoastingBinding
import com.example.myapplication.helper.Formatter
import com.example.myapplication.model.Event
import com.example.myapplication.model.Point
import com.example.myapplication.room.DB
import com.example.myapplication.room.RoastInfo
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import kotlin.concurrent.timer

class DetailActivity : AppCompatActivity() {

    companion object {
        const val INDEX_TEMP = 0
        const val INDEX_LEVEL = 1
        const val MILLISECOND = 1000L
        const val TERM = 1
    }

    private lateinit var infoAdapter: InfoListAdapter
    private lateinit var timeJob: Timer
    private var progressTime: Long = 0L
    private var startTimeId: Long = 0L
    private var pendingPoint: Point? = null

    private lateinit var adapter: LogListAdapter
    private lateinit var binding: ActivityRoastingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoastingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intChart(binding.chart)
        initClicks()

        binding.save.setOnClickListener {
            saveData()
        }

    }

    private fun saveData() {
        lifecycleScope.launch {
            val beanName = infoAdapter.list[2].value
            var weight = 0
            var roastWeight = 0
            try {
                weight = Integer.parseInt(infoAdapter.list[1].value)
                roastWeight = Integer.parseInt(infoAdapter.list[3].value)
            } catch (e: Exception) {
                Log.e("error", "error", e)
            }

            val history = Gson().toJson(adapter.list)
            val list = Gson().fromJson(history, Array<Point>::class.java)
            Log.d("aa", "list $list")
            val record = RoastInfo(
                startTimeId,
                progressTime,
                beanName,
                weight,
                roastWeight,
                history,
                null,
                null
            )
            withContext(Dispatchers.IO) {
                DB.instance.roastInfoDao().insert(record)
            }
        }
    }

    private fun initClicks() {
        with(binding) {

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
//        if (pendingPoint == null) {
//            Log.d("init point", " $level, $temper, $event")
//            pendingPoint = Point(
//                0,
//                temper ?: binding.temperatureLevel.value,
//                level ?: binding.temperature.value,
//                startTimeId,
//                0,
//                Event.INPUT
//            )
//        }
//        level?.let {
//            pendingPoint =
//                pendingPoint?.copy(level = it, time = startTimeId, processTime = progressTime)
//        }
//        temper?.let {
//            pendingPoint = pendingPoint?.copy(temperature = it, time = startTimeId)
//        }
//        event?.let {
//            pendingPoint = pendingPoint?.copy(event = it, time = startTimeId)
//        }
//        pendingPoint?.let {
//            addList(it)
//        }
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
            val recordPoint = it.copy( processTime = progressTime)
            addList(recordPoint)
            renderChart(recordPoint)
            index++
            if (index == 5) {
                stopTimer()
            }
        }
    }

    private fun startTimer() {
        startTimeId = 0L
        progressTime = 0L
        binding.control.isActivated = true
        timeJob = timer("timer", false, 0, MILLISECOND) {
            if (startTimeId == 0L) {
                startTimeId = scheduledExecutionTime()
            }
            progressTime = (scheduledExecutionTime() - startTimeId) / MILLISECOND
            if (progressTime < 1L) {
                runOnUiThread {
                    pendingPoint(null, null, event = Event.INPUT)
                }
            }
            binding.timer.text = Formatter.processTime(progressTime)
            if (progressTime % TERM == 0L) {
                runOnUiThread {
                    writePoint()
                }
            }
        }
    }

    private fun stopTimer() {
        binding.control.isActivated = false
        binding.control.isEnabled = false
        timeJob.cancel()
    }

    private fun renderChart(pendingPoint: Point) {
        binding.chart.apply {
            if (data.dataSetCount == 0) {
                val temperatureList = adapter.list.filter { it.isGraph() }
                    .mapIndexed { index, point ->
                        Entry(
                            index.toFloat(),
                            point.temperature.toFloat()
                        )
                    }
                val levelList = adapter.list.filter { it.isGraph() }
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