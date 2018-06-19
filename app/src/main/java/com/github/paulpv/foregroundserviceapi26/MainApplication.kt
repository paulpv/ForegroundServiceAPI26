package com.github.paulpv.foregroundserviceapi26

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat.Builder


class MainApplication : Application() {
    companion object {
        fun getMainApplication(context: Context): MainApplication {
            return context.applicationContext as MainApplication
        }

        private const val NOTIFICATION_REQUEST_CODE = 100
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"

        @RequiresApi(api = 26)
        private fun createNotificationChannel(context: Context,
                                              id: String,
                                              name: String,
                                              importance: Int,
                                              description: String) {
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            createNotificationChannel(context, channel)
        }

        @RequiresApi(api = 26)
        private fun createNotificationChannel(context: Context,
                                              channel: NotificationChannel) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private var notification: Notification? = null
    private var boundService: Service? = null
    val isNotificationShowing
        get() = notification != null

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            val appName = getString(R.string.app_name)
            val channelName = "$appName channel name"
            val channelImportance = NotificationManager.IMPORTANCE_LOW
            val channelDescription = "$appName channel description"

            createNotificationChannel(this,
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    channelImportance,
                    channelDescription)
        }

        bindService(Intent(this, BoundService::class.java), object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder?) {
                val binder = service as BoundService.NotificationServiceBinder
                boundService = binder.getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                showNotification(false)
                boundService = null
            }
        }, Context.BIND_AUTO_CREATE)
    }

    private fun createOngoingNotification(requestCode: Int, icon: Int, text: String): Notification {

        val context: Context = this

        val contentIntent = Intent(context, MainActivity::class.java)
                .setAction(Intent.ACTION_MAIN)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        val contentPendingIntent = PendingIntent.getActivity(context, requestCode, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        return Builder(context, NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)
                .setSmallIcon(icon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(text)
                .setContentIntent(contentPendingIntent)
                .build()
    }

    fun showNotification(show: Boolean) {
        if (show == isNotificationShowing) {
            return
        }
        if (show) {
            notification = createOngoingNotification(NOTIFICATION_REQUEST_CODE, R.drawable.ic_notification, "Content Text")
            boundService?.startForeground(NOTIFICATION_REQUEST_CODE, notification)
        } else {
            boundService?.stopForeground(true)
            notification = null
        }
    }
}