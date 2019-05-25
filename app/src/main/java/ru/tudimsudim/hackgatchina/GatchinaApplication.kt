package ru.tudimsudim.hackgatchina

import android.annotation.SuppressLint
import android.app.Application

class GatchinaApplication : Application() {

    companion object {
        lateinit var instance: GatchinaApplication
            private set
        lateinit var geoMaster : GeoMaster
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        geoMaster = GeoMaster(this)
        geoMaster.init()
    }

}