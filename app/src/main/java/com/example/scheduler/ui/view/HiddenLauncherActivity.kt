package com.example.scheduler.ui.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.AppLaunchEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HiddenLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val targetPackage = intent.getStringExtra("targetApp")
        val time = intent.getStringExtra("time") ?: ""
        val recurrence = intent.getStringExtra("recurrence") ?: ""
        val days = intent.getStringExtra("days") ?: ""

        targetPackage?.let {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val entity =
                        AppLaunchEntity(packageName = it, timestamp = System.currentTimeMillis())
                    AppDatabase.getInstance(applicationContext).appLaunchDao().insert(entity)

                    if (recurrence == "One-time") {
                        AppDatabase.getInstance(applicationContext).scheduleDao()
                            .deleteSpecific(it, time, "One-time", days)
                    }
                }

                try {
                    val launchIntent = packageManager.getLaunchIntentForPackage(it)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(launchIntent)
                    } else {
                        Toast.makeText(
                            this@HiddenLauncherActivity, "App not found: $it", Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                finishAndRemoveTask()
            }
        }
    }
}