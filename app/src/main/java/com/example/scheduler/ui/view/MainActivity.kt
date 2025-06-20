package com.example.scheduler.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.scheduler.constant.ApplicationConstant
import com.example.scheduler.databinding.ActivityMainBinding
import com.example.scheduler.ui.adapter.ViewPagerAdapter
import com.example.scheduler.utility.ApplicationUtils
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var _adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Schedule App","mainActivity- > onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _adapter = ViewPagerAdapter(this).apply {
            addFragment(AppListViewFragment(), "AppListView")
            addFragment(ScheduleListFragment(), "ScheduleList")
            addFragment(ScheduledAppFragment(), "ScheduledApp")
        }

        binding.viewPager.adapter = _adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = _adapter.getTitle(position)
        }.attach()

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    ApplicationConstant.NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == ApplicationConstant.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                // Permission denied
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS
                )

                if (showRationale) {
                    requestNotificationPermission()
                } else {
                    Toast.makeText(
                        this,
                        "Please enable notification permission from settings.",
                        Toast.LENGTH_LONG
                    ).show()
                    ApplicationUtils.openAppNotificationSettings(this)
                }
            }
        }
    }
}