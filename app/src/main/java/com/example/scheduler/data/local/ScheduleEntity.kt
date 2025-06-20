package com.example.scheduler.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val appLabel: String,
    val time: String,
    val recurrence: String,
    val days: String
)

@Entity(tableName = "package_colors")
data class PackageColor(
    @PrimaryKey val packageName: String,
    val colorHex: String
)

@Entity(tableName = "app_launch")
data class AppLaunchEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val packageName: String,
    val timestamp: Long
)