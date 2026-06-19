package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "planner")
data class Planner(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val duration: String,
    val planContent: String,
    val createdAt: Long = System.currentTimeMillis()
)