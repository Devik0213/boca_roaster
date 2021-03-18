package com.example.myapplication.model

data class Point(
    val index: Int,
    val level: Int,
    val temperature: Int,
    val time: Long,
    val processTime: Long,
    val event: Event = Event.NONE
)
