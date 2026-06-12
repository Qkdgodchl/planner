package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Planner::class,
        Album::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plannerDao(): PlannerDao

    abstract fun albumDao(): AlbumDao
}