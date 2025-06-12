package com.example.scheduler.ui.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.scheduler.databinding.ActivityMainBinding
import com.example.scheduler.ui.adapter.ViewPagerAdapter
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
        }

        binding.viewPager.adapter = _adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = _adapter.getTitle(position)
        }.attach()
    }
}