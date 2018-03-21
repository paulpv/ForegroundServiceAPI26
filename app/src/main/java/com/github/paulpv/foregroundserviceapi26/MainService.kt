package com.github.paulpv.foregroundserviceapi26

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Parcelable
import android.support.annotation.RequiresApi
import android.util.Log

class MainService : Service() {
    companion object {
        private const val TAG = "MainService"

        const val EXTRA_NOTIFICATION_REQUEST_CODE = "EXTRA_NOTIFICATION_REQUEST_CODE"
        const val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"

        fun showNotification(context: Context, requestCode: Int, notification: Notification): Boolean {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(EXTRA_NOTIFICATION_REQUEST_CODE, requestCode)
            intent.putExtra(EXTRA_NOTIFICATION, notification)
            Handler().postDelayed({ stop(context) }, 500)
            return startService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MainService::class.java)
            context.stopService(intent)
        }

        private fun startService(context: Context, intent: Intent): Boolean {
            //
            // Similar to ContextCompat.startForegroundService(context, intent)
            //
            val componentName: ComponentName? = if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            return componentName != null
        }

        @RequiresApi(api = 26)
        fun createNotificationChannel(context: Context,
                                      id: String, name: String, importance: Int,
                                      description: String) {
            val channel = NotificationChannel(id, name, importance)
            channel.description = description
            createNotificationChannel(context, channel)
        }

        @Suppress("MemberVisibilityCanBePrivate")
        @RequiresApi(api = 26)
        fun createNotificationChannel(context: Context,
                                      channel: NotificationChannel) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        Log.d(TAG, "+onCreate()")
        super.onCreate()
        Log.d(TAG, "-onCreate()")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            Log.d(TAG, "+onStartCommand(...)")
            if (intent != null) {
                val extras = intent.extras
                if (extras != null) {
                    if (extras.containsKey(EXTRA_NOTIFICATION)) {
                        val notification = extras.getParcelable<Parcelable>(EXTRA_NOTIFICATION)
                        if (notification is Notification) {
                            if (extras.containsKey(EXTRA_NOTIFICATION_REQUEST_CODE)) {
                                val requestCode = extras.getInt(EXTRA_NOTIFICATION_REQUEST_CODE)
                                Handler().postDelayed({ startForeground(requestCode, notification) }, 3000)
                            }
                        }
                    }
                }
            }
            return START_NOT_STICKY
        } finally {
            Log.d(TAG, "-onStartCommand(...)")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "+onDestroy()")
        //PbLog.s(TAG, PbStringUtils.separateCamelCaseWords("onDestroy"));
        super.onDestroy()
        stopForeground(true)
        Log.d(TAG, "-onDestroy()")
    }

    override fun onBind(intent: Intent?): IBinder? {
        try {
            Log.d(TAG, "+onBind(...)")
            return null
        } finally {
            Log.d(TAG, "-onBind(...)")
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        try {
            Log.d(TAG, "+onUnbind(...)")
            return true
        } finally {
            Log.d(TAG, "-onUnbind(...)")
        }
    }
}