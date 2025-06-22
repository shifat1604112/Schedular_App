package com.example.scheduler.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.databinding.ItemPackageScheduleBinding

class PackageScheduleListAdapter(
    private val schedules: MutableList<ScheduleEntity>,
    private val packageColor: Int,
    private val onDelete: (ScheduleEntity) -> Unit,
) : RecyclerView.Adapter<PackageScheduleListAdapter.PackageScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageScheduleViewHolder {
        val binding = ItemPackageScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.appName.text = schedule.appLabel
        holder.binding.scheduleInfo.text = "${schedule.time} ${schedule.recurrence} ${schedule.days}"
        (holder.binding.root as CardView).setCardBackgroundColor(packageColor)

        holder.binding.deleteButton.setOnClickListener { onDelete(schedule) }
    }

    override fun getItemCount(): Int = schedules.size

    inner class PackageScheduleViewHolder(val binding: ItemPackageScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)
}