package com.example.scheduler

import android.R
import android.app.TimePickerDialog
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

class ScheduleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleBinding
    private lateinit var selectedApp: ApplicationInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageName = intent.getStringExtra("packageName")
        selectedApp = packageManager.getApplicationInfo(packageName ?: "", 0)
        val appLabel = packageManager.getApplicationLabel(selectedApp).toString()
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


        /*binding.saveButton.setOnClickListener {
            val selectedTime = binding.timeSelected.text.toString()
            val recurrence = binding.recurrenceSpinner.selectedItem.toString()
            Toast.makeText(this, "Scheduled $appLabel at $selectedTime ($recurrence)", Toast.LENGTH_SHORT).show()

            // TODO: Save schedule, trigger AlarmManager/WorkManager
        }*/

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

            Toast.makeText(
                this,
                "Scheduled at $selectedTime ($recurrence) ${if (selectedDays.isNotEmpty()) "\nDays: $selectedDays" else ""}",
                Toast.LENGTH_LONG
            ).show()

            // TODO: Save and schedule logic here
        }


    }

}