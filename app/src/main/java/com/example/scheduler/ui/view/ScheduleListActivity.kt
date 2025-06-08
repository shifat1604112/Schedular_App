package com.example.scheduler.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.databinding.ActivityScheduleListBinding
import com.example.scheduler.databinding.ItemScheduleBinding
import kotlinx.coroutines.launch

class ScheduleListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScheduleListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScheduleListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = AppDatabase.getInstance(this).scheduleDao()

        lifecycleScope.launch {
            val schedules = dao.getAll()
            binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(this@ScheduleListActivity)
            binding.scheduleRecyclerView.adapter = object : RecyclerView.Adapter<ScheduleViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
                    val itemBinding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                    return ScheduleViewHolder(itemBinding)
                }

                override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
                    val item = schedules[position]
                    holder.binding.scheduleInfo.text = "${item.appLabel}\n${item.time} ${item.recurrence} ${item.days}"

                    holder.binding.editButton.setOnClickListener {
                        val intent = Intent(this@ScheduleListActivity, ScheduleActivity::class.java).apply {
                            putExtra("id", item.id)
                            putExtra("packageName", item.packageName)
                            putExtra("appLabel", item.appLabel)
                            putExtra("time", item.time)
                            putExtra("recurrence", item.recurrence)
                            putExtra("days", item.days)
                        }
                        startActivity(intent)
                    }

                    holder.binding.deleteButton.setOnClickListener {
                        lifecycleScope.launch {
                            dao.delete(item)
                            Toast.makeText(this@ScheduleListActivity, "Schedule deleted", Toast.LENGTH_SHORT).show()
                            recreate()
                        }
                    }
                }

                override fun getItemCount(): Int = schedules.size
            }
        }
    }

    class ScheduleViewHolder(val binding: ItemScheduleBinding) : RecyclerView.ViewHolder(binding.root)
}