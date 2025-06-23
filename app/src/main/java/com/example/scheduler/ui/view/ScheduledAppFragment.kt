package com.example.scheduler.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.AppLaunchEntity
import com.example.scheduler.databinding.FragmentScheduledAppBinding
import com.example.scheduler.ui.adapter.ScheduledAppAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScheduledAppFragment : Fragment() {

    private var _binding: FragmentScheduledAppBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ScheduledAppAdapter
    private val appList = mutableListOf<AppLaunchEntity>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduledAppBinding.inflate(inflater, container, false)

        adapter = ScheduledAppAdapter(appList)
        binding.recyclerViewScheduledApps.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewScheduledApps.adapter = adapter

        binding.clearAllButton.setOnClickListener {
            confirmAndClearAll()
        }

        loadAppLaunches()
        return binding.root
    }

    private fun loadAppLaunches() {
        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                AppDatabase.getInstance(requireContext()).appLaunchDao().getAll()
            }
            appList.clear()
            appList.addAll(data)
            adapter.notifyDataSetChanged()

            binding.clearAllButton.visibility = if (appList.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun confirmAndClearAll() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Delete")
            .setMessage("Are you sure you want to delete all scheduled apps?")
            .setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getInstance(requireContext()).appLaunchDao().deleteAll()
                    }
                    loadAppLaunches()
                    Toast.makeText(requireContext(), "Cleared all scheduled apps", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}