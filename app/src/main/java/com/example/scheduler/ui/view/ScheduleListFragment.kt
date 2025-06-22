package com.example.scheduler.ui.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.databinding.FragmentScheduleListBinding
import com.example.scheduler.ui.adapter.ScheduleListAdapter
import kotlinx.coroutines.launch
import androidx.core.graphics.toColorInt
import com.example.scheduler.data.local.PackageColor
import com.example.scheduler.data.local.ScheduleEntity
import kotlin.random.Random

class ScheduleListFragment : Fragment() {

    private var _binding: FragmentScheduleListBinding? = null
    private val binding get() = _binding!!
    private lateinit var scheduleListAdapter: ScheduleListAdapter
    private val scheduleList = mutableListOf<ScheduleEntity>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("Schedule App","ScheduleListFragment - > onCreateView")

        _binding = FragmentScheduleListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Schedule App","ScheduleListFragment - > onViewCreated")

        binding.scheduleRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        loadSchedulesWithColors()
    }

    private fun loadSchedulesWithColors() {
        val db = AppDatabase.getInstance(requireContext())
        val scheduleDao = db.scheduleDao()
        val colorDao = db.packageColorDao()

        viewLifecycleOwner.lifecycleScope.launch {
            val schedules = scheduleDao.getAll()
            val colorMap = mutableMapOf<String, Int>()

            for (schedule in schedules) {
                val existingColor = colorDao.getColorForPackage(schedule.packageName)
                val colorHex =
                    existingColor?.colorHex ?: generateAndStoreColor(schedule.packageName, colorDao)
                colorMap[schedule.packageName] = colorHex.toColorInt()
            }

            scheduleListAdapter = ScheduleListAdapter(
                schedules = scheduleList,
                packageColors = colorMap,
                onDelete = { schedule ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        scheduleDao.delete(schedule)
                        Toast.makeText(requireContext(), "Schedule deleted", Toast.LENGTH_SHORT)
                            .show()
                        loadSchedulesWithColors()
                    }
                },
                onEdit = { schedule ->
                    val intent = Intent(requireContext(), ScheduleActivity::class.java).apply {
                        putExtra("id", schedule.id)
                        putExtra("packageName", schedule.packageName)
                        putExtra("appLabel", schedule.appLabel)
                        putExtra("time", schedule.time)
                        putExtra("recurrence", schedule.recurrence)
                        putExtra("days", schedule.days)
                    }
                    startActivity(intent)
                }
            )

            scheduleList.clear()
            scheduleList.addAll(schedules)

            binding.scheduleRecyclerView.adapter = scheduleListAdapter
            scheduleListAdapter.updateList(schedules)
        }
    }

    private suspend fun generateAndStoreColor(
        packageName: String,
        dao: com.example.scheduler.data.local.PackageColorDao
    ): String {
        val color = generateColorHex()
        dao.insertColor(PackageColor(packageName = packageName, colorHex = color))
        return color
    }

    private fun generateColorHex(): String {
        val base = 200
        val r = (base + Random.nextInt(0, 56)).coerceAtMost(255)
        val g = (base + Random.nextInt(0, 56)).coerceAtMost(255)
        val b = (base + Random.nextInt(0, 56)).coerceAtMost(255)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    override fun onResume() {
        super.onResume()
        Log.d("Schedule App","ScheduleListFragment - > onResume")

        loadSchedulesWithColors() // refresh every time user returns to this screen
    }

    override fun onDestroyView() {
        Log.d("Schedule App","ScheduleListFragment - > onDestroy")
        super.onDestroyView()
        _binding = null
    }
}