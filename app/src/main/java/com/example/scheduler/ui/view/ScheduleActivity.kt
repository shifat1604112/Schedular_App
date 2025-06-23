package com.example.scheduler.ui.view

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityScheduleBinding
import java.util.Calendar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.receiver.AlarmReceiver
import com.example.scheduler.ui.adapter.PackageScheduleListAdapter
import com.example.scheduler.ui.viewModel.ScheduleViewModel
import androidx.core.graphics.toColorInt

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private val viewModel: ScheduleViewModel by viewModels()

    private lateinit var selectedApp: ApplicationInfo
    private lateinit var scheduleAdapter: PackageScheduleListAdapter

    private var isEditMode = false
    private var originalScheduleId: Int? = null
    private var originalTime: String? = null
    private var originalRecurrence: String? = null
    private var originalDays: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Schedule App", "ScheduleActivity - > onCreate")

        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra("packageName") ?: return
        selectedApp = packageManager.getApplicationInfo(packageName, 0)
        val appLabel = intent.getStringExtra("appLabel")
        binding.appName.text = "Schedule: $appLabel"

        originalScheduleId = intent.getIntExtra("id", -1)
        originalTime = intent.getStringExtra("time")
        originalRecurrence = intent.getStringExtra("recurrence")
        originalDays = intent.getStringExtra("days")
        isEditMode = originalScheduleId != -1

        originalTime?.let { binding.timeSelected.text = it }
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

        val recurrenceOptions = listOf("One-time", "Daily", "Weekly")
        val recurrenceAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, recurrenceOptions)
        binding.recurrenceSpinner.adapter = recurrenceAdapter
        originalRecurrence?.let {
            val index = recurrenceOptions.indexOf(it)
            if (index >= 0) binding.recurrenceSpinner.setSelection(index)
        }

        binding.recurrenceSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View?, position: Int, id: Long
                ) {
                    binding.weeklyDaysGroup.visibility =
                        if (position == 2) View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        binding.pickTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val amPm = if (hourOfDay >= 12) "PM" else "AM"
                val formattedHour = if (hourOfDay % 12 == 0) 12 else hourOfDay % 12
                binding.timeSelected.text =
                    String.format("%02d:%02d %s", formattedHour, minute, amPm)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }

        binding.saveButton.setOnClickListener {
            val selectedTime = binding.timeSelected.text.toString()
            val recurrence = binding.recurrenceSpinner.selectedItem.toString()
            if (selectedTime.isBlank() || selectedTime == "No time selected") {
                Toast.makeText(this, "Please select a valid time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            val isSame =
                isEditMode && selectedTime == originalTime && recurrence == originalRecurrence && daysString == originalDays

            if (isSame) {
                Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListener
            }

            viewModel.checkTimeConflict(
                packageName, selectedTime, recurrence, daysString, { conflictExists ->
                    if (conflictExists) {
                        Toast.makeText(this, "Schedule conflict detected", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        if (isEditMode) originalScheduleId?.let {
                            viewModel.deleteById(
                                it, packageName
                            )
                        }

                        viewModel.insertSchedule(
                            ScheduleEntity(
                                packageName = packageName,
                                appLabel = appLabel.toString(),
                                time = selectedTime,
                                recurrence = recurrence,
                                days = daysString
                            )
                        )

                        viewModel.ensureColorExists(packageName)
                        setupAlarm(packageName, selectedTime, recurrence, daysString)
                        Toast.makeText(this, "Schedule saved", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }, originalScheduleId
            )
        }

        viewModel.scheduleAndColor.observe(this) { scheduleWithColor ->
            scheduleAdapter = PackageScheduleListAdapter(
                scheduleWithColor.schedules.toMutableList(),
                scheduleWithColor.colorHex.toColorInt(),
                onDelete = { viewModel.deleteSchedule(it) }
            )

            binding.schedulesRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.schedulesRecyclerView.adapter = scheduleAdapter
            binding.schedulesCardView.visibility =
                if (scheduleWithColor.schedules.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loadSchedules(packageName)
    }

    private fun setupAlarm(packageName: String, time: String, recurrence: String, days: String) {
        // Expecting time in format "hh:mm AM/PM"
        val timeParts = time.split(" ", ":")
        if (timeParts.size != 3) return

        val hour12 = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return
        val amPm = timeParts[2]

        // Convert to 24-hour format
        val hour24 = when {
            amPm.equals("AM", true) && hour12 == 12 -> 0
            amPm.equals("AM", true) -> hour12
            amPm.equals("PM", true) && hour12 != 12 -> hour12 + 12
            else -> hour12
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("packageName", packageName)
            putExtra("time", time)
            putExtra("recurrence", recurrence)
            putExtra("days", days)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (packageName + time).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        (getSystemService(ALARM_SERVICE) as AlarmManager).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent
        )
    }
}