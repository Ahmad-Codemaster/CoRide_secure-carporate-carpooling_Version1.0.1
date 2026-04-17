package com.coride.data.model

/**
 * State-driven ride lifecycle.
 * Each state carries only the data relevant to that phase.
 */
sealed class RideState {
    object SearchingDrivers : RideState()
    data class DriverAssigned(val driverName: String, val eta: Int) : RideState()
    data class DriverArriving(val distanceKm: Float, val etaMin: Int) : RideState()
    object DriverArrived : RideState()
    data class RideInProgress(val progress: Float, val etaMin: Int) : RideState()
    object RideCompleted : RideState()
}

