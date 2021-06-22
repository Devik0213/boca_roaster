package com.example.myapplication.model

data class Point(
    val startTimeId: Long, // 한 로스팅에, Point's' 는 동일한 값 으로 사용
    val processTime: Long,  // 경과시간 표시.
    val level: Int,     // 화력레벨
    val temperature: Int,//현재 온도
    val event: Event = Event.NONE
) {
    fun ror(prevPoint: Point?): Int {
        prevPoint ?: return 0
        return temperature - prevPoint.temperature
    }

    fun isGraph(): Boolean {
        return (processTime % 30 == 0L) // 30초마다하거나,
                || (event != Event.NONE)//NONE 이벤트가 아닌 경우.
    }
}
