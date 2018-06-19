package com.github.paulpv.foregroundserviceapi26

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class BoundService : Service() {
    private val binder = NotificationServiceBinder()

    inner class NotificationServiceBinder : Binder() {
        fun getService(): BoundService {
            return this@BoundService
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }
}