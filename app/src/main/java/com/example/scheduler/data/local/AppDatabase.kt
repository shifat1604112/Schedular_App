package com.example.scheduler.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScheduleEntity::class, PackageColor::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun packageColorDao(): PackageColorDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scheduler_db"
                ).build().also { instance = it }
            }
    }
}