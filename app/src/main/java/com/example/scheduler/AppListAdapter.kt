package com.example.scheduler

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.ViewGroup
import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.data.AppInfo
import com.example.scheduler.databinding.AppItemBinding

class AppListAdapter(
    private val apps: List<AppInfo>,
    private val onAppClicked: (AppInfo) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = AppItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        //val pm: PackageManager = holder.binding.root.context.packageManager

        holder.binding.appName.text = app.name
        holder.binding.appIcon.setImageDrawable(app.icon)

        holder.binding.root.setOnClickListener {
            onAppClicked(app)
        }
    }

    override fun getItemCount() = apps.size

    inner class AppViewHolder(val binding: AppItemBinding) :
        RecyclerView.ViewHolder(binding.root)
}
