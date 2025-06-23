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

    @Query("SELECT * FROM schedules WHERE packageName = :pkg")
    suspend fun getSchedulesForApp(pkg: String): List<ScheduleEntity>

    @Update
    suspend fun update(schedule: ScheduleEntity)

    @Delete
    suspend fun delete(schedule: ScheduleEntity)

    @Query("DELETE FROM schedules WHERE id = :id")
    suspend fun deleteById(id: Int)

}

@Dao
interface PackageColorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColor(packageColor: PackageColor)

    @Query("SELECT * FROM package_colors WHERE packageName = :packageName LIMIT 1")
    suspend fun getColorForPackage(packageName: String): PackageColor?
}

@Dao
interface AppLaunchDao {
    @Insert
    suspend fun insert(launch: AppLaunchEntity)

    @Query("SELECT * FROM app_launch ORDER BY timestamp DESC")
    suspend fun getAll(): List<AppLaunchEntity>

    @Query("DELETE FROM app_launch")
    suspend fun deleteAll()
}