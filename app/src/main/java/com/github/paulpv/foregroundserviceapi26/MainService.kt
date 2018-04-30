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

/**
 * Repro and workaround for https://issuetracker.google.com/issues/76112072
 *
 * @link https://developer.android.com/about/versions/oreo/background
 *
 */
class MainService : Service() {
    companion object {
        private const val TAG = "MainService"

        const val EXTRA_NOTIFICATION_REQUEST_CODE = "EXTRA_NOTIFICATION_REQUEST_CODE"
        const val EXTRA_NOTIFICATION = "EXTRA_NOTIFICATION"

        fun showNotification(context: Context, requestCode: Int, notification: Notification, repro: Boolean, workaround: Boolean): Boolean {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(EXTRA_NOTIFICATION_REQUEST_CODE, requestCode)
            intent.putExtra(EXTRA_NOTIFICATION, notification)
            intent.putExtra("repro", repro)
            return startService(context, intent, repro, workaround)
        }

        fun stop(context: Context, workaround: Boolean) {
            val intent = Intent(context, MainService::class.java)
            stopService(context, intent, workaround)
        }

        /**
         * @see android.support.v4.content.ContextCompat#startForegroundService(android.content.Context, android.content.Intent)
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun startService(context: Context, intent: Intent, repro: Boolean, workaround: Boolean): Boolean {

            if (repro) {
                HANDLER.postDelayed({
                    Log.w(TAG, "startService will repro https://issuetracker.google.com/issues/76112072 by stopping service before it can call startForeground")
                    stop(context, workaround)
                }, 500)
            }

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

        /**
         * Per https://developer.android.com/reference/android/content/Context#startForegroundService(android.content.Intent)
         * "The service is given an amount of time comparable to the ANR interval to do this, otherwise the system will
         * automatically stop the service and declare the app ANR."
         *
         * Per https://developer.android.com/training/articles/perf-anr#anr
         * "In Android, application responsiveness is monitored by the Activity Manager and Window Manager system services.
         * Android will display the ANR dialog for a particular application when it detects one of the following conditions:
         *   * No response to an input event (such as key press or screen touch events) within 5 seconds."
         */
        @Suppress("MemberVisibilityCanBePrivate")
        const val APPLICATION_NOT_RESPONDING_TIMEOUT_MILLIS = 5000

        private val PENDING_STOP_SERVICE = mutableMapOf<Intent, Runnable>()
        private val HANDLER = Handler()

        @Suppress("MemberVisibilityCanBePrivate")
        fun stopService(context: Context, intent: Intent, workaround: Boolean) {
            if (workaround) {
                var runnable = PENDING_STOP_SERVICE[intent]
                if (runnable != null) {
                    HANDLER.removeCallbacks(runnable)
                }
                runnable = Runnable { context.stopService(intent) }
                PENDING_STOP_SERVICE[intent] = runnable
                HANDLER.postDelayed(runnable, (APPLICATION_NOT_RESPONDING_TIMEOUT_MILLIS + 250).toLong())
            } else {
                context.stopService(intent)
            }
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
                                val repro = extras.getBoolean("repro")
                                if (repro) {
                                    Log.w(TAG, "onStartCommand will repro https://issuetracker.google.com/issues/76112072 by delaying startForeground")
                                    HANDLER.postDelayed({
                                        Log.w(TAG, "onStartCommand delayed startForeground")
                                        startForeground(requestCode, notification)
                                    }, 3000)
                                } else {
                                    startForeground(requestCode, notification)
                                }
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