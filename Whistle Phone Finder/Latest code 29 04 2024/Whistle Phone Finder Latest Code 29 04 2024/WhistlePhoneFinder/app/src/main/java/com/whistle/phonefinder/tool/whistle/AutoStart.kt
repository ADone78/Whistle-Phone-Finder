package com.whistle.phonefinder.tool.whistle

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.whistle.phonefinder.tool.R
import com.whistle.phonefinder.tool.MainActivity


class AutoStart : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val channleId = "11"
            val builder = NotificationCompat.Builder(context, channleId.toString())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Security Alert")
                .setContentText("Please start service from the app if you want to find your phone by whistle!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("Please start service from the app if you want to find your phone by whistle!")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setChannelId(channleId.toString())
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(false)
                .setAutoCancel(true)
                .setOngoing(false)


            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channleId.toString(),
                    "Default channel",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                manager.createNotificationChannel(channel)
            }
            manager.notify(channleId.toInt(), builder.build())


            val sharedPreferences = context.getSharedPreferences("sharedprefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.apply {
                putBoolean("StateCheck", false)
            }.apply()

        }
    }
}