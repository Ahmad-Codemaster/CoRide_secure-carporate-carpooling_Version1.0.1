package com.indrive.clone.ui.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {

    private var locationCallback: LocationCallback? = null
    private var fusedClient: FusedLocationProviderClient? = null

    fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks if system-level location settings (GPS) are satisfied.
     * If not, invokes onResolutionRequired with the exception so the fragment can show the system dialog.
     */
    fun checkLocationSettings(
        context: Context,
        onSuccess: () -> Unit,
        onResolutionRequired: (ResolvableApiException) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .build()
        
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { onSuccess() }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                onResolutionRequired(exception)
            } else {
                onFailure(exception)
            }
        }
    }

    /**
     * Single-shot high-accuracy location fetch.
     * Returns null if permissions are not granted.
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, onLocationResult: (Location?) -> Unit) {
        if (!hasLocationPermission(context)) {
            onLocationResult(null)
            return
        }

        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        client.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cts.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                onLocationResult(location)
            } else {
                // Fallback to last known
                client.lastLocation.addOnSuccessListener { lastLoc ->
                    onLocationResult(lastLoc)
                }.addOnFailureListener {
                    onLocationResult(null)
                }
            }
        }.addOnFailureListener {
            client.lastLocation.addOnSuccessListener { lastLoc ->
                onLocationResult(lastLoc)
            }.addOnFailureListener {
                onLocationResult(null)
            }
        }
    }

    /**
     * Start continuous location updates for live tracking.
     * Call stopLocationUpdates() when done.
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates(context: Context, intervalMs: Long = 3000, onUpdate: (Location) -> Unit) {
        if (!hasLocationPermission(context)) return

        val client = LocationServices.getFusedLocationProviderClient(context)
        fusedClient = client

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setWaitForAccurateLocation(false)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onUpdate(it) }
            }
        }

        client.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
    }

    /**
     * Stop continuous location updates.
     */
    fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        fusedClient = null
    }
}
