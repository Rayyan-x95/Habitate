package com.ninety5.habitate.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ninety5.habitate.MainActivity
import com.ninety5.habitate.R
import com.ninety5.habitate.data.local.SecurePreferences
import com.ninety5.habitate.worker.SyncWorker
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var securePreferences: SecurePreferences

    override fun onNewToken(token: String) {
        Timber.d("Refreshed FCM token")
        securePreferences.fcmToken = token
        // Token will be uploaded to server on next sync
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Timber.d("From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Timber.d("Message data payload received")

            when (remoteMessage.data["type"]) {
                "sync" -> scheduleJob()
                "post", "comment", "like", "follow", "mention" -> {
                    val deepLink = remoteMessage.data["deep_link"]
                    showNotification(
                        title = remoteMessage.data["title"] ?: "Habitate",
                        body = remoteMessage.data["body"] ?: "You have a new notification",
                        deepLink = deepLink
                    )
                }
            }
        }

        // Check if message contains a notification payload (foreground)
        remoteMessage.notification?.let { notification ->
            Timber.d("Message Notification Body: ${notification.body}")
            showNotification(
                title = notification.title ?: "Habitate",
                body = notification.body ?: "",
                deepLink = remoteMessage.data["deep_link"]
            )
        }
    }

    private fun showNotification(title: String, body: String, deepLink: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Habitate Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from Habitate"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            deepLink?.let { putExtra("deep_link", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun scheduleJob() {
        val work = OneTimeWorkRequestBuilder<SyncWorker>().build()
        WorkManager.getInstance(this).enqueue(work)
    }

    companion object {
        private const val CHANNEL_ID = "habitate_notifications"
    }
}
