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
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.scheduler.R
import androidx.core.graphics.createBitmap

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra("packageName") ?: return

        val pm = context.packageManager
        val launchIntent = pm.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (launchIntent != null) {
            // Send a notification instead of launching app directly
            showLaunchNotification(context, packageName, launchIntent)
        } else {
            Toast.makeText(context, "App not found: $packageName", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLaunchNotification(
        context: Context, packageName: String, launchIntent: Intent
    ) {
        val channelId = "scheduled_app_launch"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel (for Android O+)
        val channel = NotificationChannel(
            channelId, "Scheduled App Launches", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifies when it's time to launch an app"
        }
        notificationManager.createNotificationChannel(channel)

        val packageManager = context.packageManager

        var appName: String
        var appIcon: Drawable?
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appName = packageManager.getApplicationLabel(appInfo).toString()
            appIcon = packageManager.getApplicationIcon(appInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            appName = packageName
            appIcon = null
        }

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

        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle("$appName Launch").setContentText("Tap to open $appName")
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true).build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
