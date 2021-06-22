package com.example.myapplication

import android.util.Log
import androidx.lifecycle.*
import com.example.myapplication.model.Event
import com.example.myapplication.model.Point
import com.example.myapplication.room.DB
import com.example.myapplication.room.RoastInfo
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    val currentPoint = MutableLiveData<Point>()

    private var lastPoint: Point = Point(0, 0, 0, 0, Event.NONE)

    fun startTimer(beanName : String, weight : Int, roastWeight : Int) {

        startTimeId = 0L
        isActivated.value = true

        timeJob = timer("timer", false, 0, 10*PER_SECOND) {
            if (startTimeId == 0L) {
                startTimeId = scheduledExecutionTime()
            }
            val progressTime = (scheduledExecutionTime() - startTimeId) / PER_SECOND
            lastPoint = lastPoint.copy(processTime = progressTime)
            if (isActivated.value?.not() == true) {
                return@timer
            }
            millTimes.postValue(progressTime)
            historyList.value?.add(lastPoint)
            historyList.postValue(historyList.value)
            currentPoint.postValue(lastPoint)
        }
    }

    fun stopTimer() {
        timeJob.cancel()
        isActivated.value = false
    }

    fun updateCurrentPoint(level: Int? = null, temper: Int? = null, event: Event? = null) {
        if (lastPoint.startTimeId == 0L) {
            Log.d("init point", " $level, $temper, $event")
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
        event?.let {
            lastPoint = lastPoint.copy(event = it)
        }
    }


    fun saveData(beanName : String, weight : Int, roastWeight : Int) {
        viewModelScope.launch {
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
    }

    companion object {
        const val TERM_OF_GRAPH = 60
        const val PER_SECOND = 1000L
    }
}