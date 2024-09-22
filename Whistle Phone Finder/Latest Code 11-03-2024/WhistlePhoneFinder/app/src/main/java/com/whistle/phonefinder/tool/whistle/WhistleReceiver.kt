package com.whistle.phonefinder.tool.whistle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.whistle.phonefinder.tool.R

class WhistleReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "off_action") {
            val notificationLayout = RemoteViews(context?.packageName, R.layout.notification_layout)
            notificationLayout.setViewVisibility(R.id.notificationSwitchOn, View.GONE)
            Thread.sleep(100)
            notificationLayout.setViewVisibility(R.id.notificationSwitchOff, View.VISIBLE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val CHANNELID = "Foreground Service ID"
                val channel = NotificationChannel(
                    CHANNELID,
                    CHANNELID,
                    NotificationManager.IMPORTANCE_LOW
                )
                context!!.getSystemService(NotificationManager::class.java)
                    .createNotificationChannel(channel)

                val notification = NotificationCompat.Builder(context, CHANNELID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setCustomBigContentView(notificationLayout)
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.notify(1001, notification.build())

                Thread.sleep(500)
                val startIntent =
                    Intent(context, WhistleBackgroundService::class.java)
                startIntent.action = "turnOff"
                context.startService(startIntent)
            }
        }
    }
}