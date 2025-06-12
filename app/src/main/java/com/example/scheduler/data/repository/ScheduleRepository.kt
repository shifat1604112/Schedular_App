package com.example.scheduler.data.repository

import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.PackageColor
import com.example.scheduler.data.local.ScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ScheduleRepository(private val db: AppDatabase) {

    private val scheduleDao = db.scheduleDao()
    private val colorDao = db.packageColorDao()

    suspend fun getSchedules(packageName: String) = withContext(Dispatchers.IO) {
        scheduleDao.getSchedulesForApp(packageName)
    }

    suspend fun insertSchedule(schedule: ScheduleEntity) = withContext(Dispatchers.IO) {
        scheduleDao.insert(schedule)
    }

    suspend fun deleteSchedule(schedule: ScheduleEntity) = withContext(Dispatchers.IO) {
        scheduleDao.delete(schedule)
    }

    suspend fun deleteById(id: Int) = withContext(Dispatchers.IO) {
        scheduleDao.deleteById(id)
    }

    suspend fun checkConflict(packageName: String, time: String, recurrence: String, days: String) =
        withContext(Dispatchers.IO) {
            scheduleDao.checkTimeConflict(packageName, time, recurrence, days)
        }

    suspend fun getColor(packageName: String) = withContext(Dispatchers.IO) {
        colorDao.getColorForPackage(packageName)
    }

    suspend fun getColorHex(packageName: String): String? {
        return db.packageColorDao().getColorForPackage(packageName)?.colorHex
    }

    suspend fun insertColor(color: PackageColor) = withContext(Dispatchers.IO) {
        colorDao.insertColor(color)
    }
}