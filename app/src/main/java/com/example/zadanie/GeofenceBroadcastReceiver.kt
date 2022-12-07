package com.example.zadanie

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.zadanie.workers.CheckoutWorker
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

//ked location service detekuje zmenu - vstup/vystup z geofence, posle intent
// v PendingIntent, u included in request to add geofences
//GeofenceBroadcastReceiver detekuje ze Intent bol vyvolany a moze ziskat geofenc event
//z intentu, urcit typ prechodu a urcit ktore geo ohranicienie bolo spustene
class GeofenceBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                val errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.errorCode)
                return
            }
        } else {
            return
        }

        // Get the transition type.
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
//             getne geofence ktory bol triggernuty - pripadne viacero
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            triggeringGeofences?.forEach {
                if (it.requestId.compareTo("mygeofence") == 0) {
                    context?.let { context ->
                        val uploadWorkRequest: WorkRequest =
                            OneTimeWorkRequestBuilder<CheckoutWorker>()
                                .build()
                        WorkManager
                            .getInstance(context)
                            .enqueue(uploadWorkRequest)
                    }
                }
            }
        }
    }
}