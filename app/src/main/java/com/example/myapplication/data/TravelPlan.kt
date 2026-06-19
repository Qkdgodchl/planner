package com.example.myapplication.data

data class TravelPlan(
    val destination: String,
    val days: List<DayPlan>
)

data class DayPlan(
    val day: Int,
    val items: List<PlanItem>
)

data class PlanItem(
    val time: String,
    val title: String,
    val category: String
)
