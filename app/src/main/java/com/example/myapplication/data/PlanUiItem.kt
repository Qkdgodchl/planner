package com.example.myapplication.data

sealed class PlanUiItem {

    data class DayHeader(
        val day: Int
    ) : PlanUiItem()

    data class Schedule(
        val item: PlanItem
    ) : PlanUiItem()
}
