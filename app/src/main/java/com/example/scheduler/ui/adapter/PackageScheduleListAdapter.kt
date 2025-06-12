package com.example.scheduler.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.databinding.ItemScheduleBinding

class PackageScheduleListAdapter(
    private val schedules: MutableList<ScheduleEntity>,
    private val packageColor: Int,
    private val onDelete: (ScheduleEntity) -> Unit,
    private val onEdit: (ScheduleEntity) -> Unit
) : RecyclerView.Adapter<PackageScheduleListAdapter.PackageScheduleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PackageScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PackageScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PackageScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.appName.text = schedule.appLabel
        holder.binding.scheduleInfo.text = "${schedule.time} ${schedule.recurrence} ${schedule.days}"
        holder.binding.root.background = packageColor.toDrawable()

        holder.binding.editButton.setOnClickListener { onEdit(schedule) }
        holder.binding.deleteButton.setOnClickListener { onDelete(schedule) }
    }

    override fun getItemCount(): Int = schedules.size

    inner class PackageScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    fun updateList(newList: List<ScheduleEntity>) {
        schedules.clear()
        schedules.addAll(newList)
        notifyDataSetChanged()
    }
}