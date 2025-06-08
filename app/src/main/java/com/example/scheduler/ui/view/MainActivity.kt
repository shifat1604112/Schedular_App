package com.example.scheduler.ui.view

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityApplicationChooserBinding
import android.content.pm.PackageManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.ui.adapter.AppListAdapter
import com.example.scheduler.data.model.AppInfo


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationChooserBinding
    private lateinit var appAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageManager = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val installedApps = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .map {
                AppInfo(
                    name = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    icon = it.loadIcon(packageManager)
                )
            }.sortedBy { it.name.lowercase() }

        appAdapter = AppListAdapter(installedApps) { selectedApp ->
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("packageName", selectedApp.packageName)
            intent.putExtra("appLabel", selectedApp.name)
            startActivity(intent)
        }

        binding.appListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appListRecyclerView.adapter = appAdapter


        binding.viewSchedulesButton.setOnClickListener {
            startActivity(Intent(this, ScheduleListActivity::class.java))
        }
    }
}