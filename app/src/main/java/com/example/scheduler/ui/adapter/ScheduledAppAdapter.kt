package com.example.scheduler.ui.adapter

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.R
import com.example.scheduler.data.local.AppLaunchEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduledAppAdapter(private val data: List<AppLaunchEntity>) :
    RecyclerView.Adapter<ScheduledAppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appNameText: TextView = view.findViewById(R.id.textAppName)
        val timestampText: TextView = view.findViewById(R.id.textTimestamp)
        val appIcon: ImageView = view.findViewById(R.id.imageAppIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_scheduled_app, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        val context = holder.itemView.context

        try {
            val pm = context.packageManager
            val appInfo = pm.getApplicationInfo(item.packageName, 0)
            val appLabel = pm.getApplicationLabel(appInfo)
            val icon = pm.getApplicationIcon(appInfo)

            holder.appNameText.text = appLabel
            holder.appIcon.setImageDrawable(icon)
        } catch (e: PackageManager.NameNotFoundException) {
            holder.appNameText.text = item.packageName
            holder.appIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }

        holder.timestampText.text = SimpleDateFormat(
            "dd MMM yyyy HH:mm:ss", Locale.getDefault()
        ).format(Date(item.timestamp))
    }

}

