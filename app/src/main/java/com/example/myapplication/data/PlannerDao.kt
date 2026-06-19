package com.example.myapplication.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PlannerDao {

    @Insert
    suspend fun insertPlanner(planner: Planner)

    @Query("SELECT * FROM planner ORDER BY createdAt DESC")
    suspend fun getAllPlanners(): List<Planner>

    @Query("DELETE FROM planner WHERE id = :plannerId")
    suspend fun deletePlanner(plannerId: Int)

    @Query("SELECT * FROM planner WHERE id = :id")
    suspend fun getPlannerById(id: Int): Planner?

    @Update
    suspend fun updatePlanner(planner: Planner)
}