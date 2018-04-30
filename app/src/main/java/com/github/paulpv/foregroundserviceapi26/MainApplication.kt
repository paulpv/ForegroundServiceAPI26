package com.github.paulpv.foregroundserviceapi26

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat.Builder
import android.util.Log


class MainApplication : Application() {
    companion object {
        private const val TAG = "MainApplication"

        fun getMainApplication(context: Context): MainApplication {
            return context.applicationContext as MainApplication
        }

        private const val NOTIFICATION_REQUEST_CODE = 100
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
    }

    private lateinit var notification: Notification

    var isNotificationShowing: Boolean = false
        private set

    override fun onCreate() {
        Log.d(TAG, "+onCreate()")
        super.onCreate()

        if (Build.VERSION.SDK_INT >= 26) {
            val appName = getString(R.string.app_name)
            val channelName = "$appName channel name"
            val channelImportance = NotificationManager.IMPORTANCE_LOW
            val channelDescription = "$appName channel description"

            MainService.createNotificationChannel(this,
                    NOTIFICATION_CHANNEL_ID,
                    channelName,
                    channelImportance,
                    channelDescription)
        }

        notification = createOngoingNotification(NOTIFICATION_REQUEST_CODE, R.drawable.ic_notification, "Content Text")

        val intent = Intent(this, MainService::class.java)
        bindService(intent, mainServiceConnection, Context.BIND_AUTO_CREATE)

        Log.d(TAG, "-onCreate()")
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

    private var mainService: MainService? = null

    private val mainServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder?) {
            val binder = service as MainService.MainServiceBinder
            mainService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mainService = null
        }
    }

    fun showNotification(show: Boolean) {
        if (show) {
            mainService?.startForeground(NOTIFICATION_REQUEST_CODE, notification)
            isNotificationShowing = true
        } else {
            isNotificationShowing = false
            mainService?.stopForeground(true)
        }
    }
}