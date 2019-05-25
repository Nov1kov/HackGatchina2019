package ru.tudimsudim.hackgatchina

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import ru.tudimsudim.hackgatchina.model.Issue
import java.util.stream.Collectors

class GeoMaster(private var activity: NearestIssuesActivity) {

    var radiusGeo = 100f

    private var location: Location? = null

    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val locationRequest = LocationRequest.create()?.apply {
        interval = 10000
        fastestInterval = 5000
        priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }


    fun init() {
        setupPermissions()

        geofencingClient = LocationServices.getGeofencingClient(activity)
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    this@GeoMaster.location = location
                }
            }
        }
        getUpdatedLocations()
    }

    @SuppressLint("MissingPermission")
    fun getUpdatedLocations(): Location? {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
        return location
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("MissingPermission")
    fun updateGeofence(fences: List<Issue>) {
        geofencingClient.removeGeofences(geofencePendingIntent)

        var collect = fences.stream().map { s ->
            Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(s.id + ": " + s.title + "! " + s.text)

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
        }.collect(Collectors.toList())

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
        val intent = Intent(activity, GeofenceTransitionsIntentService::class.java)
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getService(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun setupPermissions() {
        val permission1 = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permission2 = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (permission1 != PackageManager.PERMISSION_GRANTED || permission2 != PackageManager.PERMISSION_GRANTED) {
            Log.i("PermissionDemo", "Permission to record denied")
            makeRequest()
        }
    }


    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1
        )
    }

    @SuppressLint("MissingPermission")
    private fun createLocationRequest() {

        val builder = locationRequest?.let {
            LocationSettingsRequest.Builder()
                .addLocationRequest(it)
        }
        val client: SettingsClient = LocationServices.getSettingsClient(activity)
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
                    exception.startResolutionForResult(activity, LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                this.location = location!!
                println(location.latitude)
                println(location.longitude)
            }
        fusedLocationClient.lastLocation
            .addOnFailureListener { exception ->
                try {
                    print(exception)
                } catch (sendEx: IntentSender.SendIntentException) {
                }
            }
    }

}