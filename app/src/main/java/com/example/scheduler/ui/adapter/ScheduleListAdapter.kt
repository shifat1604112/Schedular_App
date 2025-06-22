package com.example.scheduler.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.toDrawable
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.data.local.ScheduleEntity
import com.example.scheduler.databinding.ItemScheduleBinding

class ScheduleListAdapter(
    private val schedules: MutableList<ScheduleEntity>,
    private val packageColors: Map<String, Int>,
    private val onDelete: (ScheduleEntity) -> Unit,
    private val onEdit: (ScheduleEntity) -> Unit
) : RecyclerView.Adapter<ScheduleListAdapter.ScheduleViewHolder>() {

    inner class ScheduleViewHolder(val binding: ItemScheduleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val binding = ItemScheduleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ScheduleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val schedule = schedules[position]
        holder.binding.appName.text = schedule.appLabel
        holder.binding.scheduleInfo.text = "${schedule.time} ${schedule.recurrence} ${schedule.days}"

        val color = packageColors[schedule.packageName] ?: 0xFFDDDDDD.toInt()
        (holder.binding.root as CardView).setCardBackgroundColor(color)

        holder.binding.editButton.setOnClickListener { onEdit(schedule) }
        holder.binding.deleteButton.setOnClickListener { onDelete(schedule) }
    }

    override fun getItemCount(): Int = schedules.size

    fun updateList(newList: List<ScheduleEntity>) {
        schedules.clear()
        schedules.addAll(newList)
        notifyDataSetChanged()
    }
}