package ru.tudimsudim.hackgatchina

import android.annotation.SuppressLint
import android.app.Application
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
        instance = this
        geoMaster = GeoMaster(this)
        geoMaster.init()
    }

}