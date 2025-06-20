package com.example.scheduler.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object ApplicationUtils {
    fun openAppNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }
}