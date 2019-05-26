package ru.tudimsudim.hackgatchina

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import ru.tudimsudim.hackgatchina.model.Data

class GatchinaApplication : Application() {

    companion object {
        lateinit var instance: GatchinaApplication
            private set
        lateinit var geoMaster : GeoMaster
        var data = Data
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        instance = this
        geoMaster = GeoMaster(this)
        geoMaster.init()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("main_id", "Main", importance).apply {
                description = "Main channel"
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}