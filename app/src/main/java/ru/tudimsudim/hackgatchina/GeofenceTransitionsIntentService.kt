package ru.tudimsudim.hackgatchina

import android.app.IntentService
import android.app.PendingIntent
import android.content.ContentValues.TAG
import android.content.Intent
import android.preference.PreferenceManager
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import ru.tudimsudim.hackgatchina.model.Issue
import ru.tudimsudim.hackgatchina.presenter.HttpClient

class GeofenceTransitionsIntentService : IntentService("Geofence-Service") {

    override fun onHandleIntent(intent: Intent?) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent.hasError()) {
            Log.e(TAG, (geofencingEvent.errorCode + geofencingEvent.geofenceTransition).toString())
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            val triggeringGeofences = geofencingEvent.triggeringGeofences

            // Send notification and log the transition details.
            val uid = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                .getString("uid", "")
            for (i in 0 until triggeringGeofences.size) {
                var issue = HttpClient.getIssueById(triggeringGeofences[i].requestId)
                if (!issue.users_like.contains(uid)) {
                    sendNotification(issue)
                    return
                }
            }
        } else {
            // Log the error.
            Log.e(TAG, "Alarma! Geifencing errore: $geofenceTransition")
        }
    }

    private fun sendNotification(issue: Issue) {

        val intent = Intent(this, IssueActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra(IssueActivity.ID_ISSUE_KEY, issue.id)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        var builder = NotificationCompat.Builder(this, "main_id")
            .setSmallIcon(R.mipmap.icons8_fire_truck_48)
            .setContentTitle("Рядом есть проблема! " + issue.title)
            .setContentText(issue.text + " - " + issue.address)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(issue.text + " - " + issue.address)
            )
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            notify(Math.random().toInt(), builder.build())
        }
    }

}
