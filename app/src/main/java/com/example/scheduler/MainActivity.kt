package com.example.scheduler

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityApplicationChooserBinding
import android.content.pm.PackageManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.data.AppInfo


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationChooserBinding
    private lateinit var appAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val packageManager = packageManager
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .map {
                AppInfo(
                    name = packageManager.getApplicationLabel(it).toString(),
                    packageName = it.packageName,
                    icon = packageManager.getApplicationIcon(it)
                )
            }

        appAdapter = AppListAdapter(installedApps) { selectedApp ->
            val intent = Intent(this, ScheduleActivity::class.java)
            intent.putExtra("packageName", selectedApp.packageName)
            startActivity(intent)
        }

        binding.appRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appRecyclerView.adapter = appAdapter
    }
}