package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Planner::class,
        Album::class,
        Photo::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun plannerDao(): PlannerDao

    abstract fun albumDao(): AlbumDao
    
    abstract fun photoDao(): PhotoDao
}