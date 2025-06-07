package com.example.scheduler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityApplicationChooserBinding
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityApplicationChooserBinding
    private lateinit var appAdapter: AppListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApplicationChooserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }

        appAdapter = AppListAdapter(installedApps) { selectedApp ->
            val label = pm.getApplicationLabel(selectedApp).toString()
            Toast.makeText(this, "Clicked: $label", Toast.LENGTH_SHORT).show()
        }

        binding.appRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.appRecyclerView.adapter = appAdapter
    }
}