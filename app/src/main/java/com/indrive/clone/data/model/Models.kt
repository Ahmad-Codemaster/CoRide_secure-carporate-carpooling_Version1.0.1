package com.indrive.clone.data.model

// ── CoRide Identity Enums ──
enum class VerificationStatus { PENDING, UNDER_REVIEW, VERIFIED, REJECTED }
enum class UserRole { STUDENT, EMPLOYEE }
enum class VerificationDocType { ORGANIZATION_CARD, CNIC_CARD }

data class TrustedContact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val phone: String,
    val relation: String = "Guardian"
)

data class User(
    val id: String = "user_001",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val avatarUrl: String = "",
    val rating: Float = 5.0f,
    val totalRides: Int = 0,
    val memberSince: String = "March 2024",
    // ── CoRide identity fields ──
    val role: UserRole = UserRole.STUDENT,
    val organizationName: String = "",
    val organizationAddress: String = "",
    val homeAddress: String = "",
    val cnicNumber: String = "",
    val idCardImagePath: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val trustedContacts: List<TrustedContact> = emptyList(),
    // ── Driver Persona Fields ──
    val isDriverMode: Boolean = false,
    val isRegisteredDriver: Boolean = false,
    val driverDetails: Driver? = null
)

data class Driver(
    val id: String,
    val name: String,
    val phone: String,
    val avatarUrl: String,
    val rating: Float,
    val totalTrips: Int,
    val vehicle: Vehicle,
    val isAvailable: Boolean = true,
    // ── CoRide verification fields ──
    val cnicNumber: String = "",
    val licenseNumber: String = "",
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val organizationName: String = "",
    val backgroundCheckPassed: Boolean = true
)

data class Vehicle(
    val make: String,
    val model: String,
    val color: String,
    val plateNumber: String,
    val year: Int,
    val type: VehicleType = VehicleType.ECONOMY
)

enum class VehicleType {
    ECONOMY, COMFORT, XL
}

data class Place(
    val id: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val type: PlaceType = PlaceType.RECENT
)

enum class PlaceType {
    HOME, WORK, RECENT, SAVED, SEARCH_RESULT
}

data class DriverOffer(
    val id: String,
    val driver: Driver,
    val offeredPrice: Double,
    val estimatedArrival: Int, // minutes
    val distance: Double, // km to pickup
    val driverLat: Double = 0.0,
    val driverLng: Double = 0.0
)

data class Ride(
    val id: String,
    val pickup: Place,
    val destination: Place,
    val driver: Driver? = null,
    val status: RideStatus = RideStatus.IDLE,
    val requestedFare: Double = 0.0,
    val finalFare: Double = 0.0,
    val distance: Double = 0.0, // km
    val duration: Int = 0, // minutes
    val rideType: VehicleType = VehicleType.ECONOMY,
    val driverRating: Float = 0f,
    val date: String = "",
    val comment: String = ""
)

enum class RideStatus {
    IDLE,
    SEARCHING,
    OFFERS_AVAILABLE,
    DRIVER_MATCHED,
    DRIVER_ARRIVING,
    DRIVER_ARRIVED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

// ── CoRide Safety Alert ──
data class SafetyAlert(
    val id: String,
    val rideId: String,
    val userId: String,
    val driverId: String,
    val alertType: AlertType,
    val timestamp: String,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

enum class AlertType { SOS_PANIC, ROUTE_DEVIATION, RIDE_SHARED }

// ── CoRide Weather Model ──
data class WeatherDay(
    val day: String,
    val temp: String,
    val iconRes: Int,
    val condition: String
)
