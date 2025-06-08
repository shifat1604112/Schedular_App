package com.example.scheduler.ui.view

import android.R
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityScheduleBinding
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.receiver.AlarmReceiver
import kotlinx.coroutines.launch


class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var selectedApp: ApplicationInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra("packageName")
        selectedApp = packageManager.getApplicationInfo(packageName ?: "", 0)
        val appLabel = intent.getStringExtra("appLabel")
        binding.appName.text = "Schedule: $appLabel"

        // Time Picker
        binding.pickTimeButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _: TimePicker, h: Int, m: Int ->
                binding.timeSelected.text = String.format("%02d:%02d", h, m)
            }, hour, minute, true)

            timePickerDialog.show()
        }

        // Recurrence dropdown
        val options = listOf("One-time", "Daily", "Weekly")
        val adapter = ArrayAdapter(this, R.layout.simple_spinner_item, options)
        binding.recurrenceSpinner.adapter = adapter

        binding.recurrenceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = parent?.getItemAtPosition(position).toString()
                binding.weeklyDaysGroup.visibility = if (selected == "Weekly") View.VISIBLE else View.GONE
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

            /*Toast.makeText(
                this,
                "Scheduled at $selectedTime ($recurrence) ${if (selectedDays.isNotEmpty()) "\nDays: $selectedDays" else ""}",
                Toast.LENGTH_LONG
            ).show()*/


            val db = AppDatabase.getInstance(this)
            val dao = db.scheduleDao()

            val daysString = if (recurrence == "Weekly") selectedDays.joinToString(",") else ""

            val schedule = ScheduleEntity(
                packageName = selectedApp.packageName,
                appLabel = appLabel.toString(),
                time = selectedTime,
                recurrence = recurrence,
                days = daysString
            )

            lifecycleScope.launch {
                dao.insert(schedule)
                Toast.makeText(this@ScheduleActivity, "Schedule saved", Toast.LENGTH_SHORT).show()
            }

            lifecycleScope.launch {
                val conflicts = dao.checkTimeConflict(packageName.toString(), selectedTime, recurrence, daysString)
                if (conflicts.isNotEmpty()) {
                    Toast.makeText(this@ScheduleActivity, "Already added schedule for this time", Toast.LENGTH_SHORT).show()
                } else {
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
                    finish()
                }
            }

            val calendar = Calendar.getInstance().apply {
                val timeParts = selectedTime.split(":")
                set(Calendar.HOUR_OF_DAY, timeParts[0].toInt())
                set(Calendar.MINUTE, timeParts[1].toInt())
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
            }

            val intent = Intent(this, AlarmReceiver::class.java).apply {
                putExtra("packageName", packageName)
            }

            val requestCode = (packageName + selectedTime).hashCode()  // Unique ID
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)


        }


    }

}