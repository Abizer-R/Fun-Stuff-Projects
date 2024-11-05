package com.example.funstuff01.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.funstuff01.R

object NotificationUtil {
    fun createDownloadCompleteNotification(
        context: Context,
        title: String,
        body: String,
        fileUri: Uri,
        dataType: String,
        notificationId: Int = 1
    ) {

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, dataType)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        val channelId = context.getString(R.string.download_complete_notification_channel_id)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelId, importance)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.chatwise_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}