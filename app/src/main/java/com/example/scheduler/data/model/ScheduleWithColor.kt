package com.example.scheduler.data.model

import com.example.scheduler.data.local.ScheduleEntity

data class ScheduleWithColor(
    val schedules: List<ScheduleEntity>,
    val colorHex: String
)
