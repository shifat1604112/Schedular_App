package com.example.scheduler.data.local

import androidx.room.*

@Dao
interface ScheduleDao {
    @Insert
    suspend fun insert(schedule: ScheduleEntity)

    @Query("SELECT * FROM schedules")
    suspend fun getAll(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE packageName = :pkg AND time = :time AND recurrence = :rec AND days = :days")
    suspend fun checkTimeConflict(pkg: String, time: String, rec: String, days: String): List<ScheduleEntity>

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)
}
