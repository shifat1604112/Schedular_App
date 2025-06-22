package com.example.scheduler.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.scheduler.R
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.AppLaunchEntity
import com.example.scheduler.ui.adapter.ScheduledAppAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduledAppFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ScheduledAppAdapter
    private val appList = mutableListOf<AppLaunchEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_scheduled_app, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewScheduledApps)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ScheduledAppAdapter(appList)
        recyclerView.adapter = adapter
        loadAppLaunches()
        return view
    }

    private fun loadAppLaunches() {
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext()).appLaunchDao().getAll()
            }
            appList.clear()
            appList.addAll(data)
            adapter.notifyDataSetChanged()
        }
    }
}