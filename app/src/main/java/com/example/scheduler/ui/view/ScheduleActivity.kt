package com.example.scheduler.ui.view

import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityScheduleBinding
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.PackageColor
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.receiver.AlarmReceiver
import com.example.scheduler.ui.adapter.PackageScheduleListAdapter
import kotlinx.coroutines.launch
import kotlin.random.Random


class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var selectedApp: ApplicationInfo
    private lateinit var scheduleAdapter: PackageScheduleListAdapter

    private var isEditMode = false
    private var originalScheduleId: Int? = null
    private var originalTime: String? = null
    private var originalRecurrence: String? = null
    private var originalDays: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra("packageName")
        selectedApp = packageManager.getApplicationInfo(packageName ?: "", 0)
        val appLabel = intent.getStringExtra("appLabel")
        binding.appName.text = "Schedule: $appLabel"


        originalScheduleId = intent.getIntExtra("id", -1)
        originalTime = intent.getStringExtra("time")
        originalRecurrence = intent.getStringExtra("recurrence")
        originalDays = intent.getStringExtra("days")

        isEditMode = originalScheduleId != -1

        // Pre-fill time
        originalTime?.let {
            binding.timeSelected.text = it
        }


        // Pre-select weekly days
        originalDays?.split(",")?.forEach {
            when (it.trim()) {
                "Mon" -> binding.monCheck.isChecked = true
                "Tue" -> binding.tueCheck.isChecked = true
                "Wed" -> binding.wedCheck.isChecked = true
                "Thu" -> binding.thuCheck.isChecked = true
                "Fri" -> binding.friCheck.isChecked = true
                "Sat" -> binding.satCheck.isChecked = true
                "Sun" -> binding.sunCheck.isChecked = true
            }
        }



        // Time Picker
        binding.pickTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(this, { _, h, m ->
                binding.timeSelected.text = String.format("%02d:%02d", h, m)
            }, hour, minute, true).show()
        }

        // Recurrence dropdown
        val recurrenceOptions = listOf("One-time", "Daily", "Weekly")
        val recurrenceAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, recurrenceOptions)
        binding.recurrenceSpinner.adapter = recurrenceAdapter

        // Pre-select recurrence
        originalRecurrence?.let {
            val index = recurrenceOptions.indexOf(it)
            if (index >= 0) binding.recurrenceSpinner.setSelection(index)
        }


        binding.recurrenceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                binding.weeklyDaysGroup.visibility = if (position == 2) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        binding.saveButton.setOnClickListener {
            val selectedTime = binding.timeSelected.text.toString()
            val recurrence = binding.recurrenceSpinner.selectedItem.toString()

            val selectedDays = mutableListOf<String>()
            if (recurrence == "Weekly") {
                if (binding.monCheck.isChecked) selectedDays.add("Mon")
                if (binding.tueCheck.isChecked) selectedDays.add("Tue")
                if (binding.wedCheck.isChecked) selectedDays.add("Wed")
                if (binding.thuCheck.isChecked) selectedDays.add("Thu")
                if (binding.friCheck.isChecked) selectedDays.add("Fri")
                if (binding.satCheck.isChecked) selectedDays.add("Sat")
                if (binding.sunCheck.isChecked) selectedDays.add("Sun")
            }
            val daysString = if (recurrence == "Weekly") selectedDays.joinToString(",") else ""

            val db = AppDatabase.getInstance(this)
            val dao = db.scheduleDao()

            lifecycleScope.launch {
                val isSame = isEditMode &&
                        selectedTime == originalTime &&
                        recurrence == originalRecurrence &&
                        daysString == originalDays

                if (isSame) {
                    Toast.makeText(this@ScheduleActivity, "No changes detected", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }

                val conflicts = dao.checkTimeConflict(packageName.toString(), selectedTime, recurrence, daysString)
                    .filterNot { it.id == originalScheduleId }

                if (conflicts.isNotEmpty()) {
                    Toast.makeText(this@ScheduleActivity, "Already added schedule for this time", Toast.LENGTH_SHORT).show()
                } else {
                    if (isEditMode) {
                        originalScheduleId?.let { dao.deleteById(it) }
                    }

                    dao.insert(
                        ScheduleEntity(
                            packageName = packageName.toString(),
                            appLabel = appLabel.toString(),
                            time = selectedTime,
                            recurrence = recurrence,
                            days = daysString
                        )
                    )
                    Toast.makeText(this@ScheduleActivity, "Schedule saved", Toast.LENGTH_SHORT).show()

                    val colorDao = db.packageColorDao()
                    if (colorDao.getColorForPackage(packageName.toString()) == null) {
                        val rnd = Random(System.currentTimeMillis())
                        val colorHex = String.format("#%02X%02X%02X", 100 + rnd.nextInt(156), 100 + rnd.nextInt(156), 100 + rnd.nextInt(156))
                        colorDao.insertColor(PackageColor(packageName.toString(), colorHex))
                    }

                    // Alarm setup
                    val calendar = Calendar.getInstance().apply {
                        val (h, m) = selectedTime.split(":").map { it.toInt() }
                        set(Calendar.HOUR_OF_DAY, h)
                        set(Calendar.MINUTE, m)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
                    }

                    val intent = Intent(this@ScheduleActivity, AlarmReceiver::class.java).apply {
                        putExtra("packageName", packageName)
                    }

                    val pendingIntent = PendingIntent.getBroadcast(
                        this@ScheduleActivity,
                        (packageName + selectedTime).hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    (getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
                    )

                    finish()
                }
            }
        }
        refreshSchedules()
    }

    private fun refreshSchedules() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@ScheduleActivity)
            val dao = db.scheduleDao()
            val colorDao = db.packageColorDao()

            val packageColorHex = colorDao.getColorForPackage(selectedApp.packageName)?.colorHex
            val colorInt = packageColorHex?.let { Color.parseColor(it) } ?: Color.GRAY

            val schedules = dao.getSchedulesForApp(selectedApp.packageName).toMutableList()

            if (schedules.isNotEmpty()) {
                binding.schedulesCardView.visibility = View.VISIBLE
                scheduleAdapter = PackageScheduleListAdapter(
                    schedules,
                    colorInt,
                    onDelete = { schedule ->
                        lifecycleScope.launch {
                            dao.delete(schedule)
                            Toast.makeText(this@ScheduleActivity, "Schedule deleted", Toast.LENGTH_SHORT).show()
                            refreshSchedules()
                        }
                    },
                    onEdit = { schedule ->
                        val intent = Intent(this@ScheduleActivity, ScheduleActivity::class.java).apply {
                            putExtra("id", schedule.id)
                            putExtra("packageName", schedule.packageName)
                            putExtra("appLabel", schedule.appLabel)
                            putExtra("time", schedule.time)
                            putExtra("recurrence", schedule.recurrence)
                            putExtra("days", schedule.days)
                        }
                        startActivity(intent)
                    }
                )
                binding.schedulesRecyclerView.layoutManager = LinearLayoutManager(this@ScheduleActivity)
                binding.schedulesRecyclerView.adapter = scheduleAdapter
            } else {
                binding.schedulesCardView.visibility = View.GONE
            }
        }
    }
}
