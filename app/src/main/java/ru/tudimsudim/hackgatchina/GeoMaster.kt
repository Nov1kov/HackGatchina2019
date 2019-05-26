package ru.tudimsudim.hackgatchina

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import ru.tudimsudim.hackgatchina.model.Issue

class GeoMaster(val context: Context) {

    var radiusGeo = 100f

    private var location: Location? = null

    private val geofencingClient: GeofencingClient by lazy {
        LocationServices.getGeofencingClient(context)
    }
    private val locationCallback: LocationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                    this@GeoMaster.location = locationResult.lastLocation
            }
        }
    }
    private var fusedLocationClient: FusedLocationProviderClient? = null

    val locationRequest = LocationRequest.create()?.apply {
        interval = 5000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    fun getCoordinates():List<Double>{
        if (location != null){
            return listOf(location!!.latitude, location!!.longitude)
        }
        return emptyList()
    }

    @SuppressLint("MissingPermission")
    fun init() {
        if (GeoMasterHelper.shouldAskPermission(context) || fusedLocationClient != null){
            return
        }
        createLocationRequest()
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    @SuppressLint("MissingPermission")
    fun updateGeofence(fences: List<Issue>) {
        geofencingClient.removeGeofences(geofencePendingIntent)
        val collect = fences.filter {
            it.coordinate.count() == 2
        }.map { s ->
            Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(s.id)

                // Set the circular region of this geofence.
                .setCircularRegion(s.latitude, s.longitude, radiusGeo)

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                .setExpirationDuration(-1)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build()
        }

        if (collect.count() == 0) {
            return
        }

        geofencingClient.addGeofences(getGeofencingRequest(collect), geofencePendingIntent)?.run {
            addOnSuccessListener {
                // Geofences added
                // ...
                println("GeoFences added success")
            }
            addOnFailureListener {
                // Failed to add geofences
                // ...
                println("GeoFences added bad. Alarma!!!")
            }
        }

    }

    private fun getGeofencingRequest(fences: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(fences)
        }.build()
    }

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder?.build())

        task.addOnSuccessListener { response ->
            println(response.locationSettingsStates)
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    //exception.startResolutionForResult(activity, LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient?.lastLocation?.addOnSuccessListener { location: Location? ->
            this.location = location!!
            println(location.latitude)
            println(location.longitude)
        }
        fusedLocationClient?.lastLocation?.addOnFailureListener { exception ->
            try {
                print(exception)
            } catch (sendEx: IntentSender.SendIntentException) {
            }
        }
    }
}

object GeoMasterHelper{
    fun shouldAskPermission(context: Context): Boolean {
        val permission1 = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permission2 = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        return permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED
    }
}