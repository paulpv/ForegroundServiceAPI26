package com.github.paulpv.foregroundserviceapi26

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.support.annotation.RequiresApi
import android.util.Log

/**
 * Repro and workaround for https://issuetracker.google.com/issues/76112072
 *
 * @link https://developer.android.com/about/versions/oreo/background
 *
 */
class MainService : Service() {
    companion object {
        private const val TAG = "MainService"

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

    private val binder = MainServiceBinder()

    inner class MainServiceBinder : Binder() {
        fun getService() : MainService {
            return this@MainService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        try {
            Log.d(TAG, "+onBind(...)")
            return binder
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