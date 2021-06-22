package com.example.myapplication

object LevelLabel {
    val values = mutableListOf<String>()

    init {
        for (i in 0..24) {
            if (i % 2 == 0) {
                values.add("${i / 2}")
            } else {
                values.add("${i / 2}.5")
            }
        }
    }
}