package com.example.scheduler.ui.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.PackageColor
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.data.repository.ScheduleRepository
import kotlinx.coroutines.launch


class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = ScheduleRepository(db)

    private val _schedules = MutableLiveData<List<ScheduleEntity>>()
    val schedules: LiveData<List<ScheduleEntity>> = _schedules

    private val _colorHex = MutableLiveData<String>()
    val colorHex: LiveData<String> = _colorHex

    fun loadSchedules(packageName: String) {
        viewModelScope.launch {
            /*_schedules.value = repository.getSchedules(packageName)*/
            val data = repository.getSchedules(packageName)
            _schedules.postValue(data)

            val color = repository.getColorHex(packageName)
            _colorHex.postValue(color ?: "#888888")
        }
    }

    fun insertSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.insertSchedule(schedule)
            loadSchedules(schedule.packageName)
        }
    }

    fun deleteSchedule(schedule: ScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
            loadSchedules(schedule.packageName)
        }
    }

    fun deleteById(id: Int, packageName: String) {
        viewModelScope.launch {
            repository.deleteById(id)
            loadSchedules(packageName)
        }
    }

    fun checkTimeConflict(
        packageName: String,
        time: String,
        recurrence: String,
        days: String,
        onResult: (Boolean) -> Unit,
        excludeId: Int?
    ) {
        viewModelScope.launch {
            val conflicts = repository.checkConflict(packageName, time, recurrence, days)
                .filterNot { it.id == excludeId }
            onResult(conflicts.isNotEmpty())
        }
    }

    fun ensureColorExists(packageName: String) {
        viewModelScope.launch {
            if (repository.getColor(packageName) == null) {
                val rnd = java.util.Random()
                val color = String.format(
                    "#%02X%02X%02X",
                    100 + rnd.nextInt(156),
                    100 + rnd.nextInt(156),
                    100 + rnd.nextInt(156)
                )
                repository.insertColor(PackageColor(packageName, color))
            }
        }
    }
}