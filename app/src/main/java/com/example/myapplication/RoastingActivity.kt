package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRoastingBinding
import com.example.myapplication.helper.Formatter
import com.example.myapplication.model.Point
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.*

class RoastingActivity : AppCompatActivity() {

    companion object {
        const val INDEX_TEMP = 0
        const val INDEX_LEVEL = 1

    }

    private var vibrator: Vibrator? = null
    private lateinit var tempAdapter: LogListAdapter
    private lateinit var binding: ActivityRoastingBinding
    private val viewModel: LogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRoastingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        (getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).apply {
            if (hasVibrator()) {
                vibrator = this
            }
        }
        intChart(binding.chart)
        initClicks()

        tempAdapter = LogListAdapter(this)
        binding.recyclerView.adapter = tempAdapter

        viewModel.isActivated.observe(this) {
            binding.control.isActivated = it
            binding.control.isEnabled = it
        }
        viewModel.millTimes.observe(this) { time ->
            Formatter.processTime(time).let {
                binding.timer.text = it
            }
        }
        viewModel.historyList.observe(this) {

        }
        viewModel.currentPoint.observe(this) {
            tempAdapter.add(it)
            if (it.isGraph()) {
                renderChart(it)
            }
        }
    }

    private fun initClicks() {
        with(binding) {
            close.setOnClickListener {
                startActivity(Intent(this@RoastingActivity, MainActivity::class.java))
            }
            save.setOnClickListener {
                viewModel.saveData("asd", 222, 333)
            }
            temperature.setOnValueChanged(
                { pickHaptic() },
                { slidingHaptic() }) { picker, oldVal, newVal ->
                Log.d("onChanged", "temper : $newVal")
                viewModel.updateCurrentPoint(temper = newVal)
            }
            level.displayedValues = LevelLabel.values.toTypedArray()
            level.setOnValueChanged(
                { pickHaptic() },
                { slidingHaptic() }) { picker, oldVal, newVal ->
                Log.d("onChanged", "level : $newVal")
                viewModel.updateCurrentPoint(level = newVal)
            }
            control.setOnClickListener {
                if (it.isActivated) {
                    viewModel.stopTimer()
                } else {
                    val beanName = beanName.getString()
                    val greenBeanWeightValue = greanBeanWeight.getString()?.safeIntOrZero()

                    when {
                        beanName.isNullOrEmpty() -> "콩 이름을 기록해주세요."
                        greenBeanWeightValue == 0 -> "생두 무게를 입력해주세요"
                    }
                    viewModel.startLooper()
                }
            }
            levelPlus.setOnClickListener {
                level.value--
                pickHaptic()
                viewModel.updateCurrentPoint(level = level.value)
            }
            levelMinus.setOnClickListener {
                level.value++
                viewModel.updateCurrentPoint(level = level.value)
            }
            temperaturePlus.setOnClickListener {
                temperature.value += 10
                viewModel.updateCurrentPoint(temper = temperature.value)

            }
            temperatureMinus.setOnClickListener {
                temperature.value -= 10
                viewModel.updateCurrentPoint(temper = temperature.value)
            }
        }
    }

    private fun vibe(milliseconds: Long, amp: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, amp))
        } else {
            vibrator?.vibrate(milliseconds)
        }
    }

    private fun slidingHaptic() {
        vibe(1, 50)
    }

    private fun pickHaptic() {
        vibe(10, 100)
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

    private fun renderChart(pendingPoint: Point) {
        binding.chart.apply {
            if (data.dataSetCount == 0) {
                val temperatureList = tempAdapter.list.filter { it.processTime == 0L }
                    .mapIndexed { index, point ->
                        Entry(
                            index.toFloat(),
                            point.temperature.toFloat()
                        )
                    }
                val levelList = tempAdapter.list.filter { it.processTime == 0L }
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