package com.example.myapplication.model

//TODO Room
data class RoastInfo(
    val roastId: Long, //로스팅
    val date: Long, // 날짜
    val temp: Int, //폰온도
    val envTemp: Int, // 기온
    val inputVolume: Int,//인풋
    val outputVolume: Int//아웃풋
)
