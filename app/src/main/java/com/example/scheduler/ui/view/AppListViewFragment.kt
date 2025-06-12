package com.example.scheduler.ui.view

import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.ui.adapter.AppListAdapter
import com.example.scheduler.data.model.AppInfo
import com.example.scheduler.databinding.FragmentAppListBinding

class AppListViewFragment : Fragment() {

    private var _binding: FragmentAppListBinding? = null
    private val binding get() = _binding!!
    private lateinit var appAdapter: AppListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Schedule App","AppListViewFragment - > onCreateView")
        _binding = FragmentAppListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Schedule App","AppListViewFragment - > onViewCreated")

        val packageManager = requireContext().packageManager
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
            // Use requireActivity() or requireContext() to start activities
            val scheduleIntent = Intent(requireContext(), ScheduleActivity::class.java).apply {
                putExtra("packageName", selectedApp.packageName)
                putExtra("appLabel", selectedApp.name)
            }
            startActivity(scheduleIntent)
        }

        binding.appListRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.appListRecyclerView.adapter = appAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Schedule App","AppListViewFragment - > onDestroyView")
        _binding = null
    }
}