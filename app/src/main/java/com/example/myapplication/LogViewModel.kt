package com.example.myapplication

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.model.Event
import com.example.myapplication.model.Point
import com.example.myapplication.room.DB
import com.example.myapplication.room.RoastInfo
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.Timer
import kotlin.concurrent.timer

class LogViewModel : ViewModel() {
    private lateinit var timeJob: Timer
    private var progressTime: Long = 0L
    private var startTimeId: Long = 0L
    private val db = DB.instance.roastInfoDao()

    val isActivated = MutableLiveData<Boolean>()

    val millTimes = MutableLiveData<Long>() // 시간 전달

    val historyList = MutableLiveData<ArrayList<Point>>() // 전체 변경사항

    val currentPendingPoint = MutableLiveData<Point>()

    private var lastPoint: Point = Point(0, 0, 0, 0, Event.NONE)

    private var _beanName: String = ""
    private var _greenBeenWeight: Int = 0
    private var _beanWeight: Int = 0

    fun startLooper() {

        startTimeId = 0L
        isActivated.value = true

        timeJob = timer("timer", false, 0, 1 * PER_SECOND) {
            if (startTimeId == 0L) {
                startTimeId = scheduledExecutionTime()
            }
            val progressTime = (scheduledExecutionTime() - startTimeId) / PER_SECOND
            lastPoint = lastPoint.copy(processTime = progressTime)
            if (isActivated.value?.not() == true) {
                return@timer
            }
            Log.d("Log", "$progressTime, lastPoint $lastPoint")
            millTimes.postValue(progressTime)
            historyList.run {
                if (lastPoint.isGraph()) {
                    value?.add(lastPoint)
                    postValue(value)
                }
            }
            currentPendingPoint.postValue(lastPoint)
        }
    }

    fun stopTimer() {
        timeJob.cancel()
        isActivated.value = false
    }

    fun updateCurrentPoint(level: Int? = null, temper: Int? = null, event: Event? = null) {
        if (lastPoint.startTimeId == 0L) {
            lastPoint = Point(
                startTimeId, 0, level ?: 0, temper ?: 0, Event.INPUT
            )
            return
        }
        level?.let {
            lastPoint = lastPoint.copy(level = it)
        }
        temper?.let {
            lastPoint = lastPoint.copy(temperature = it)
        }

        lastPoint = lastPoint.copy(event = event ?: Event.NONE)

        currentPendingPoint.value = lastPoint
        Log.d("updated point", "L:$level , T:$temper, E:$event, = $lastPoint")
    }


    fun saveData(beanName: String, weight: Int, roastWeight: Int) {
        val a= viewModelScope.launch {
            async(Dispatchers.IO) {

            }
            val history = Gson().toJson(historyList)
            val list = Gson().fromJson(history, Array<Point>::class.java)
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
        val b = viewModelScope.launch {
            var result0 = -1
            async {
                for (i in 0..10){
                    delay(1000)
                }
                result0 = 0
            }
            result0 + 100 // 99

            val result = async(Dispatchers.IO){
                for (i in 0..10){
                    delay(1000)
                }
                0
            }
            result.await() + 100 // 100


            val result2 = withContext(Dispatchers.IO){
                for (i in 0..10){
                    delay(1000)
                }
                0
            }
            result2 + 100 // 100
        }
    }

    companion object {
        const val TERM_OF_GRAPH = 60
        const val PER_SECOND = 1000L
    }
}