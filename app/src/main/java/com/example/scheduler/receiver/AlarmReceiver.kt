package com.example.scheduler.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.graphics.createBitmap
import com.example.scheduler.R
import com.example.scheduler.data.local.AppDatabase
import com.example.scheduler.data.local.AppLaunchEntity
import com.example.scheduler.ui.view.HiddenLauncherActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("packageName") ?: return
        val time = intent.getStringExtra("time") ?: return
        val recurrence = intent.getStringExtra("recurrence") ?: return
        val days = intent.getStringExtra("days") ?: ""

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (launchIntent == null) {
            Toast.makeText(context, "App not found: $packageName", Toast.LENGTH_SHORT).show()
            return
        }

        launchScheduledApplication(context, packageName, launchIntent, time, recurrence, days)
    }

    private fun launchScheduledApplication(
        context: Context,
        packageName: String,
        launchIntent: Intent,
        time: String,
        recurrence: String,
        days: String
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            context.startActivity(launchIntent)

            // Direct launch for lower Android versions
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getInstance(context).appLaunchDao().insert(
                    AppLaunchEntity(
                        packageName = packageName, timestamp = System.currentTimeMillis()
                    )
                )

                if (recurrence == "One-time") {
                    AppDatabase.getInstance(context).scheduleDao()
                        .deleteSpecific(packageName, time, "One-time", days)
                }
            }
        } else {
            // Show notification for Android 10+
            showLaunchNotification(context, packageName, time, recurrence, days)
        }
    }

    private fun showLaunchNotification(
        context: Context, packageName: String, time: String, recurrence: String, days: String
    ) {
        val channelId = "scheduled_app_launch"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            channelId, "Scheduled App Launches", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when it's time to launch an app"
        }
        notificationManager.createNotificationChannel(channel)

        val packageManager = context.packageManager

        val appInfo = try {
            packageManager.getApplicationInfo(packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        val appName =
            appInfo?.let { packageManager.getApplicationLabel(it).toString() } ?: packageName
        val appIcon = appInfo?.let { packageManager.getApplicationIcon(it) }

        val largeIconBitmap = appIcon?.let { drawable ->
            if (drawable is BitmapDrawable) {
                drawable.bitmap
            } else {
                // Create bitmap from drawable
                val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }
        }

        val bridgeIntent = Intent(context, HiddenLauncherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("targetApp", packageName)
            putExtra("time", time)
            putExtra("recurrence", recurrence)
            putExtra("days", days)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            bridgeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setLargeIcon(largeIconBitmap)
            .setContentTitle("$appName Launch").setContentText("Tap to open $appName")
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
