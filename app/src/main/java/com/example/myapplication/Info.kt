package com.example.myapplication

sealed class Info(val isEditable: Boolean, val label: String, var value: String) {
    class Date(value: String) : Info(false, "날짜", value)
    class Name(value: String) : Info(true, "원두명", value)
    class BeanWeight(value: String) : Info(true, "생두 중량", value)
    class RoastedBeanWeight(value: String) : Info(true, "원두 중량", value)
}
