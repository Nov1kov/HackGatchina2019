package ru.tudimsudim.hackgatchina

import android.app.IntentService
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class GeofenceTransitionsIntentService : IntentService("Geofence-Service") {
    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
//            val errorMessage = GeofenceErrorMessages.getErrorString(
//                this,
//                geofencingEvent.errorCode
//            )
            Log.e(TAG, (geofencingEvent.errorCode + geofencingEvent.geofenceTransition).toString())
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT
        ) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Get the transition details as a String.
            val geofenceTransitionDetails = getGeofenceTransitionDetails(
                this,
                geofenceTransition,
                triggeringGeofences
            )

            // Send notification and log the transition details.
            sendNotification(geofenceTransitionDetails)
            Log.i(TAG, geofenceTransitionDetails)
        } else {
            // Log the error.
            Log.e(TAG, "Alarma! Geifencing errore: $geofenceTransition")
        }
    }

    private fun sendNotification(geofenceTransitionDetails: String?) {

        val intent = Intent(this, NearestIssuesActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var builder = NotificationCompat.Builder(this, "main_id")
            .setSmallIcon(R.mipmap.icons8_fire_truck_48)
            .setContentTitle("Рядом есть проблема")
            .setContentText(geofenceTransitionDetails)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(geofenceTransitionDetails)
            )
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(Math.random().toInt(), builder.build())
        }
    }

    private fun getGeofenceTransitionDetails(
        geofenceTransitionsIntentService: GeofenceTransitionsIntentService,
        geofenceTransition: Int,
        triggeringGeofences: List<Geofence>
    ): String? {
        println(triggeringGeofences.get(0).requestId)
        return triggeringGeofences.get(0).requestId
    }

}
