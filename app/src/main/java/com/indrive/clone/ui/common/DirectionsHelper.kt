package com.indrive.clone.ui.common

import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

/**
 * Generates a realistic curved polyline between two points using quadratic Bezier interpolation.
 * No Directions API required — works 100% offline.
 *
 * Also calculates road-realistic distance (applies a 1.3x winding factor to straight-line distance)
 * and estimates duration based on average city driving speed.
 */
object DirectionsHelper {

    data class RouteResult(
        val polylinePoints: List<LatLng>,
        val distanceMeters: Int,
        val durationSeconds: Int
    )

    /**
     * Generate a curved route between two coordinates.
     * Uses quadratic Bezier with a perpendicular control point offset.
     *
     * @param origin      Start coordinate
     * @param destination End coordinate
     * @param numPoints   Number of interpolation points (more = smoother)
     * @return RouteResult with polyline, distance, and duration
     */
    fun generateRoute(origin: LatLng, destination: LatLng, numPoints: Int = 60): RouteResult {
        // Calculate a control point perpendicular to the midpoint for a natural curve
        val midLat = (origin.latitude + destination.latitude) / 2.0
        val midLng = (origin.longitude + destination.longitude) / 2.0

        val dLat = destination.latitude - origin.latitude
        val dLng = destination.longitude - origin.longitude
        val dist = sqrt(dLat * dLat + dLng * dLng)

        // Offset perpendicular to the line, proportional to distance (creates a natural arc)
        val offsetMagnitude = dist * 0.15  // 15% curve
        val perpLat = -dLng / dist * offsetMagnitude
        val perpLng = dLat / dist * offsetMagnitude

        val controlLat = midLat + perpLat
        val controlLng = midLng + perpLng

        // Generate Bezier curve points
        val points = mutableListOf<LatLng>()
        for (i in 0..numPoints) {
            val t = i.toFloat() / numPoints
            val lat = quadraticBezier(origin.latitude, controlLat, destination.latitude, t.toDouble())
            val lng = quadraticBezier(origin.longitude, controlLng, destination.longitude, t.toDouble())
            points.add(LatLng(lat, lng))
        }

        // Calculate total polyline distance
        var totalDistMeters = 0.0
        for (i in 1 until points.size) {
            totalDistMeters += haversineDistance(points[i - 1], points[i])
        }

        // Apply winding factor for city roads (straight-line × 1.3)
        val roadDistance = (totalDistMeters * 1.3).toInt()

        // Average city speed: ~30 km/h → 8.33 m/s
        val durationSec = (roadDistance / 8.33).toInt()

        return RouteResult(
            polylinePoints = points,
            distanceMeters = roadDistance,
            durationSeconds = durationSec
        )
    }

    /**
     * Generate a set of intermediate waypoints for driver approach
     * (from driver position to pickup, with slight curve)
     */
    fun generateApproachPath(driverPos: LatLng, pickup: LatLng, numPoints: Int = 40): List<LatLng> {
        return generateRoute(driverPos, pickup, numPoints).polylinePoints
    }

    private fun quadraticBezier(p0: Double, p1: Double, p2: Double, t: Double): Double {
        val oneMinusT = 1.0 - t
        return oneMinusT * oneMinusT * p0 + 2 * oneMinusT * t * p1 + t * t * p2
    }

    /**
     * Haversine distance between two LatLng points in meters.
     */
    fun haversineDistance(a: LatLng, b: LatLng): Double {
        val R = 6371000.0 // Earth radius in meters
        val dLat = Math.toRadians(b.latitude - a.latitude)
        val dLng = Math.toRadians(b.longitude - a.longitude)
        val sinLat = sin(dLat / 2)
        val sinLng = sin(dLng / 2)
        val aVal = sinLat * sinLat + cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sinLng * sinLng
        return R * 2 * atan2(sqrt(aVal), sqrt(1 - aVal))
    }

    /**
     * Format meters into human-readable distance string.
     */
    fun formatDistance(meters: Int): String {
        return if (meters >= 1000) {
            String.format("%.1f km", meters / 1000.0)
        } else {
            "$meters m"
        }
    }

    /**
     * Format seconds into human-readable duration string.
     */
    fun formatDuration(seconds: Int): String {
        val mins = seconds / 60
        return if (mins < 1) "< 1 min" else "$mins min"
    }
}
