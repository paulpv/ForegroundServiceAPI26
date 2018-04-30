package com.github.paulpv.foregroundserviceapi26

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        fun getMainApplication(context: Context): MainApplication {
            return MainApplication.getMainApplication(context)
        }
    }

    private lateinit var mainApplication: MainApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainApplication = getMainApplication(this)

        setContentView(R.layout.activity_main)
        switchMainService.setOnCheckedChangeListener { _, isChecked ->
            mainApplication.showNotification(isChecked)
        }
        checkRepro.setOnCheckedChangeListener { _, isChecked ->
            mainApplication.isRepro = isChecked
        }
        checkWorkaround.setOnCheckedChangeListener { _, isChecked ->
            mainApplication.isWorkaround = isChecked
        }
    }

    override fun onResume() {
        super.onResume()
        switchMainService.isChecked = mainApplication.isNotificationShowing
    }
}
