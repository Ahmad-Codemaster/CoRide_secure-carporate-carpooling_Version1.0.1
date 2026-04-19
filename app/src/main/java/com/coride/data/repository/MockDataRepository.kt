package com.coride.data.repository

import com.coride.data.model.*
import com.coride.utils.EmailNotificationHelper
import kotlinx.coroutines.delay

object MockDataRepository {

    private var currentUser = com.coride.data.local.LocalPreferences.getUser() ?: User()
    
    // Temporary signup data
    private var pendingName: String = ""
    private var pendingEmail: String = ""
    private var pendingPhone: String = ""
    private var pendingPass: String = ""
    private var pendingOrg: String = ""
    private var pendingStudentId: String = ""
    
    // Password Reset State
    private var generatedOtp: String? = null
    private var resetTargetEmail: String? = null

    // Verification timer state
    private var verificationTimerRunning = false
    private var verificationStartTime = 0L
    private val VERIFICATION_DURATION_MS = 30 * 1000L // 30 seconds

    // ── Driver State ──
    private var isDriverOnline = false

    private val mockDrivers = listOf(
        Driver(
            id = "driver_001",
            name = "Muhammad Ali",
            phone = "+92 312 9876543",
            avatarUrl = "",
            rating = 4.9f,
            totalTrips = 1247,
            vehicle = Vehicle("Toyota", "Corolla", "White", "LEA-1234", 2021, VehicleType.CAR),
            cnicNumber = "35201-7654321-3",
            licenseNumber = "LHR-DL-2021-4567",
            verificationStatus = VerificationStatus.VERIFIED,
            organizationName = "Punjab University",
            backgroundCheckPassed = true
        ),
        Driver(
            id = "driver_002",
            name = "Hassan Khan",
            phone = "+92 333 4567890",
            avatarUrl = "",
            rating = 4.7f,
            totalTrips = 892,
            vehicle = Vehicle("Honda", "Civic", "Black", "LEB-5678", 2022, VehicleType.CAR),
            cnicNumber = "35202-8765432-1",
            licenseNumber = "LHR-DL-2020-8901",
            verificationStatus = VerificationStatus.VERIFIED,
            organizationName = "",
            backgroundCheckPassed = true
        ),
        Driver(
            id = "driver_003",
            name = "Usman Tariq",
            phone = "+92 345 6789012",
            avatarUrl = "",
            rating = 4.8f,
            totalTrips = 2103,
            vehicle = Vehicle("Road Prince", "70cc", "Red", "LEC-9012", 2020, VehicleType.BIKE),
            cnicNumber = "35203-9876543-5",
            licenseNumber = "LHR-DL-2019-2345",
            verificationStatus = VerificationStatus.VERIFIED,
            organizationName = "LUMS",
            backgroundCheckPassed = true
        ),
        Driver(
            id = "driver_004",
            name = "Bilal Ahmed",
            phone = "+92 301 2345678",
            avatarUrl = "",
            rating = 4.6f,
            totalTrips = 567,
            vehicle = Vehicle("Sazgar", "7-Seater", "Yellow", "LED-3456", 2023, VehicleType.RICKSHAW),
            cnicNumber = "35204-1234567-7",
            licenseNumber = "LHR-DL-2022-6789",
            verificationStatus = VerificationStatus.VERIFIED,
            organizationName = "",
            backgroundCheckPassed = true
        ),
        Driver(
            id = "driver_005",
            name = "Fahad Malik",
            phone = "+92 321 7890123",
            avatarUrl = "",
            rating = 4.9f,
            totalTrips = 3456,
            vehicle = Vehicle("Honda", "City", "White", "LEE-7890", 2022, VehicleType.CAR),
            cnicNumber = "35205-2345678-9",
            licenseNumber = "LHR-DL-2021-0123",
            verificationStatus = VerificationStatus.VERIFIED,
            organizationName = "FAST NUCES",
            backgroundCheckPassed = true
        )
    )

    private var mockPlaces = mutableListOf(
        Place("place_home", "Home", "House 42, Street 7, DHA Phase 5, Lahore", 31.4804, 74.3587, PlaceType.HOME),
        Place("place_work", "Work", "Arfa Software Technology Park, Lahore", 31.5204, 74.3587, PlaceType.WORK),
        Place("place_1", "Packages Mall", "Walton Road, Lahore", 31.5159, 74.3436, PlaceType.SAVED),
        Place("place_2", "Allama Iqbal International Airport", "Airport Road, Lahore", 31.5216, 74.4036, PlaceType.RECENT),
        Place("place_3", "Emporium Mall", "Abdul Haque Road, Johar Town", 31.4697, 74.2728, PlaceType.RECENT),
        Place("place_4", "University of Punjab", "Canal Bank Road, Lahore", 31.5007, 74.3032, PlaceType.RECENT),
        Place("place_5", "Liberty Market", "Gulberg III, Lahore", 31.5138, 74.3445, PlaceType.SAVED),
        Place("place_6", "Fortress Stadium", "Cantt, Lahore", 31.5225, 74.3683, PlaceType.RECENT),
        Place("place_7", "Model Town Park", "Model Town, Lahore", 31.4814, 74.3187, PlaceType.RECENT),
        Place("place_8", "Shaukat Khanum Hospital", "Johar Town, Lahore", 31.4685, 74.2635, PlaceType.RECENT)
    )

    private val rideHistory = mutableListOf(
        Ride(
            id = "ride_001",
            pickup = mockPlaces[0],
            destination = mockPlaces[1],
            driver = mockDrivers[0],
            status = RideStatus.COMPLETED,
            requestedFare = 350.0,
            finalFare = 380.0,
            distance = 12.5,
            duration = 25,
            rideType = VehicleType.CAR,
            driverRating = 5f,
            date = "Today, 8:30 AM"
        ),
        Ride(
            id = "ride_002",
            pickup = mockPlaces[1],
            destination = mockPlaces[2],
            driver = mockDrivers[1],
            status = RideStatus.COMPLETED,
            requestedFare = 250.0,
            finalFare = 250.0,
            distance = 8.3,
            duration = 18,
            rideType = VehicleType.CAR,
            driverRating = 4f,
            date = "Yesterday, 6:15 PM"
        ),
        Ride(
            id = "ride_003",
            pickup = mockPlaces[3],
            destination = mockPlaces[0],
            driver = mockDrivers[2],
            status = RideStatus.COMPLETED,
            requestedFare = 500.0,
            finalFare = 550.0,
            distance = 18.7,
            duration = 35,
            rideType = VehicleType.BIKE,
            driverRating = 5f,
            date = "Mar 10, 2:45 PM"
        ),
        Ride(
            id = "ride_004",
            pickup = mockPlaces[0],
            destination = mockPlaces[4],
            driver = mockDrivers[3],
            status = RideStatus.CANCELLED,
            requestedFare = 400.0,
            finalFare = 0.0,
            distance = 15.2,
            duration = 0,
            rideType = VehicleType.CAR,
            date = "Mar 8, 11:00 AM"
        ),
        Ride(
            id = "ride_005",
            pickup = mockPlaces[5],
            destination = mockPlaces[6],
            driver = mockDrivers[4],
            status = RideStatus.COMPLETED,
            requestedFare = 200.0,
            finalFare = 220.0,
            distance = 6.1,
            duration = 14,
            rideType = VehicleType.CAR,
            driverRating = 5f,
            date = "Mar 5, 9:20 AM"
        )
    )

    fun getCurrentUser(): User = currentUser

    fun updateUser(user: User) {
        currentUser = user
        com.coride.data.local.LocalPreferences.saveUser(user)
    }

    fun isUserVerified(): Boolean = currentUser.verificationStatus == VerificationStatus.VERIFIED

    // ── Verification Flow ──
    fun startVerificationTimer() {
        verificationTimerRunning = true
        verificationStartTime = System.currentTimeMillis()
    }

    fun getVerificationRemainingMs(): Long {
        if (!verificationTimerRunning) return VERIFICATION_DURATION_MS
        val elapsed = System.currentTimeMillis() - verificationStartTime
        return (VERIFICATION_DURATION_MS - elapsed).coerceAtLeast(0)
    }

    fun isVerificationTimerRunning(): Boolean = verificationTimerRunning

    fun checkAndCompleteVerification(): Boolean {
        if (verificationTimerRunning && getVerificationRemainingMs() <= 0) {
            completeVerification()
            return true
        }
        return false
    }

    fun completeVerification() {
        verificationTimerRunning = false
        currentUser = currentUser.copy(verificationStatus = VerificationStatus.VERIFIED)
        com.coride.data.local.LocalPreferences.saveUser(currentUser)
        
        // Automated Administrative Alert
        EmailNotificationHelper.sendVerificationAlert(currentUser)
    }

    // ── Driver Interface Logic ──
    fun setDriverMode(enabled: Boolean) {
        currentUser = currentUser.copy(isDriverMode = enabled)
        updateUser(currentUser)
        if (!enabled) isDriverOnline = false
    }

    fun isDriverOnline(): Boolean = isDriverOnline

    fun setDriverOnline(online: Boolean) {
        isDriverOnline = online
    }

    fun registerDriverDetails(make: String, model: String, plate: String, license: String) {
        val vehicle = Vehicle(make, model, "White", plate, 2022)
        val driverDetails = Driver(
            id = "driver_${currentUser.id}",
            name = currentUser.name,
            phone = currentUser.phone,
            avatarUrl = currentUser.avatarUrl,
            rating = 5.0f,
            totalTrips = 0,
            vehicle = vehicle,
            licenseNumber = license
        )
        currentUser = currentUser.copy(
            isRegisteredDriver = true,
            driverDetails = driverDetails
        )
        updateUser(currentUser)
    }

    fun submitDocument(docType: VerificationDocType) {
        currentUser = currentUser.copy(
            verificationStatus = VerificationStatus.UNDER_REVIEW,
            idCardImagePath = "mock_${docType.name.lowercase()}_photo.jpg"
        )
        com.coride.data.local.LocalPreferences.saveUser(currentUser)
        startVerificationTimer()
    }

    // ── Safety / SOS / Trusted Contacts ──
    fun getTrustedContacts(): List<TrustedContact> = currentUser.trustedContacts

    fun addTrustedContact(contact: TrustedContact) {
        val newList = currentUser.trustedContacts.toMutableList().apply { add(contact) }
        updateUser(currentUser.copy(trustedContacts = newList))
    }

    fun removeTrustedContact(contactId: String) {
        val newList = currentUser.trustedContacts.filter { it.id != contactId }
        updateUser(currentUser.copy(trustedContacts = newList))
    }

    fun triggerSOS(): SafetyAlert {
        return SafetyAlert(
            id = "alert_${System.currentTimeMillis()}",
            rideId = "ride_active",
            userId = currentUser.id,
            driverId = "driver_active",
            alertType = AlertType.SOS_PANIC,
            timestamp = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
        )
    }

    fun getSosMessageTemplate(): String {
        return "🆘 EMERGENCY! I need help. I am on a CoRide trip. Live Location: https://coride.pk/track/LIVE_RIDE_ID"
    }

    fun getPlaces(): List<Place> = mockPlaces

    fun getRecentPlaces(): List<Place> = mockPlaces.filter { it.type == PlaceType.RECENT }

    fun getSavedPlaces(): List<Place> = mockPlaces.filter { it.type == PlaceType.SAVED || it.type == PlaceType.HOME || it.type == PlaceType.WORK }

    fun addSavedPlace(place: Place) {
        mockPlaces.add(place)
    }

    fun searchPlaces(query: String): List<Place> {
        return mockPlaces.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
        }
    }

    fun getRideHistory(): List<Ride> = rideHistory.sortedByDescending { it.id }

    fun addRide(ride: Ride) {
        rideHistory.add(ride)
    }

    fun deleteRide(ride: Ride) {
        rideHistory.removeIf { it.id == ride.id }
    }

    fun getDrivers(): List<Driver> = mockDrivers

    suspend fun generateDriverOffers(requestedFare: Double): List<DriverOffer> {
        return generateDriverOffers(requestedFare, 0.0, 0.0)
    }

    /**
     * Smart driver matching: places drivers at realistic coordinates near pickup point.
     * Drivers are sorted nearest-first. Closest driver gets best price.
     */
    suspend fun generateDriverOffers(requestedFare: Double, pickupLat: Double, pickupLng: Double): List<DriverOffer> {
        delay(2500) // Simulate realistic search time
        val shuffled = mockDrivers.shuffled()
        val useRealCoords = pickupLat != 0.0 && pickupLng != 0.0

        data class DriverWithCoords(val driver: Driver, val lat: Double, val lng: Double, val distKm: Double)

        val driversWithCoords = shuffled.take(4).map { driver ->
            // Place drivers at 0.5-3km from pickup in random direction
            val angle = Math.random() * 2 * Math.PI
            val radiusKm = 0.5 + Math.random() * 2.5
            val radiusDeg = radiusKm / 111.0 // ~111km per degree

            val driverLat = if (useRealCoords) pickupLat + radiusDeg * Math.cos(angle) else 0.0
            val driverLng = if (useRealCoords) pickupLng + radiusDeg * Math.sin(angle) else 0.0

            DriverWithCoords(driver, driverLat, driverLng, radiusKm)
        }.sortedBy { it.distKm } // Nearest first

        return driversWithCoords.take(3).mapIndexed { index, dwc ->
            val priceVariation = when (index) {
                0 -> requestedFare
                1 -> requestedFare + (requestedFare * 0.08)
                2 -> requestedFare + (requestedFare * 0.15)
                else -> requestedFare
            }
            // ETA: assume ~30 km/h city speed
            val etaMinutes = maxOf(2, (dwc.distKm / 0.5).toInt()) // ~2 min per km

            DriverOffer(
                id = "offer_${index}",
                driver = dwc.driver,
                offeredPrice = Math.round(priceVariation / 10.0) * 10.0,
                estimatedArrival = etaMinutes,
                distance = dwc.distKm,
                driverLat = dwc.lat,
                driverLng = dwc.lng
            )
        }
    }

    fun getRecommendedFare(distance: Double, vehicleType: VehicleType): Double {
        val baseRate = when (vehicleType) {
            VehicleType.BIKE -> 15.0
            VehicleType.RICKSHAW -> 22.0
            VehicleType.CAR -> 35.0
        }
        val fare = 80 + (distance * baseRate)
        return Math.round(fare / 10.0) * 10.0
    }

    suspend fun simulateDriverArrival(onUpdate: (Int) -> Unit) {
        for (i in 5 downTo 0) {
            onUpdate(i)
            delay(2000) // 2 sec per "minute" for demo
        }
    }

    suspend fun simulateRideProgress(totalMinutes: Int, onUpdate: (Int, Double) -> Unit) {
        for (i in 0..totalMinutes) {
            val progress = i.toDouble() / totalMinutes
            onUpdate(totalMinutes - i, progress)
            delay(1500) // 1.5 sec per "minute" for demo
        }
    }

    fun verifyOtp(otp: String): Boolean = otp == "1234"

    fun registerPending(name: String, email: String, phone: String, pass: String, org: String, studentId: String) {
        pendingName = name
        pendingEmail = email
        pendingPhone = phone
        pendingPass = pass
        pendingOrg = org
        pendingStudentId = studentId
    }

    fun completeRegistration(otp: String): Boolean {
        if (verifyOtp(otp)) {
            com.coride.data.local.LocalPreferences.saveRegistrationData(pendingEmail, pendingPhone, pendingPass, pendingStudentId)
            val newUser = User(
                id = "user_${System.currentTimeMillis()}",
                name = pendingName,
                email = pendingEmail,
                phone = pendingPhone,
                role = UserRole.STUDENT,
                organizationName = pendingOrg,
                cnicNumber = pendingStudentId,
                verificationStatus = VerificationStatus.PENDING
            )
            updateUser(newUser)
            rideHistory.clear()
            
            // Automated Administrative Alert
            EmailNotificationHelper.sendRegistrationAlert(newUser)
            
            return true
        }
        return false
    }

    fun login(loginId: String, pass: String): Boolean {
        val regEmail = com.coride.data.local.LocalPreferences.getRegisteredEmail()
        val regPhone = com.coride.data.local.LocalPreferences.getRegisteredPhone()
        val regPass = com.coride.data.local.LocalPreferences.getRegisteredPassword()

        val loginTrimmed = loginId.trim().lowercase()
        val emailMatch = regEmail?.trim()?.lowercase() == loginTrimmed
        val phoneMatch = regPhone?.trim() == loginId.trim()
        
        if ((emailMatch || phoneMatch) && regPass == pass) {
            // Re-enable the isLoggedIn flag so getUser() works
            val prefs = com.coride.data.local.LocalPreferences
            // Save isLoggedIn flag first, then getUser will find the stored profile
            prefs.setLoggedIn(true)
            var cachedUser = prefs.getUser()
            if (cachedUser == null || cachedUser.name.isEmpty()) {
                cachedUser = User(
                    id = "user_${System.currentTimeMillis()}",
                    name = "Verified User",
                    phone = if (regPhone?.isNotEmpty() == true) regPhone else loginId,
                    email = regEmail ?: "",
                    role = UserRole.STUDENT,
                    verificationStatus = VerificationStatus.VERIFIED
                )
            }
            updateUser(cachedUser)
            return true
        }
        return false
    }

    fun isLoggedIn(): Boolean = com.coride.data.local.LocalPreferences.getUser() != null

    fun logout() {
        verificationTimerRunning = false
        com.coride.data.local.LocalPreferences.clearUser()
        currentUser = User()
    }

    fun deleteAccount() {
        // Notify admin before wiping data
        EmailNotificationHelper.sendAccountDeletionAlert(currentUser)
        
        // Wipe all local data
        com.coride.data.local.LocalPreferences.clearAllData()
        
        // Reset state
        verificationTimerRunning = false
        currentUser = User()
    }

    // ── Password Recovery (SharedPrefs) ──
    fun sendResetOtp(identity: String): Boolean {
        val regEmail = com.coride.data.local.LocalPreferences.getRegisteredEmail()
        val regStudentId = com.coride.data.local.LocalPreferences.getRegisteredStudentId()
        
        val idTrimmed = identity.trim().lowercase()
        val isMatch = (regEmail?.lowercase() == idTrimmed) || 
                      (regStudentId?.lowercase() == idTrimmed)

        if (isMatch && regEmail != null) {
            // Generate real 4-digit OTP
            val otp = String.format("%04d", java.util.Random().nextInt(10000))
            generatedOtp = otp
            resetTargetEmail = regEmail
            
            // Dispatch real email via SMTP
            EmailNotificationHelper.sendOtpEmail(regEmail, otp)
            return true
        }
        return false
    }

    fun verifyResetOtp(otp: String): Boolean {
        return otp == generatedOtp
    }

    fun finalizePasswordReset(newPass: String) {
        val regEmail = com.coride.data.local.LocalPreferences.getRegisteredEmail() ?: ""
        val regPhone = com.coride.data.local.LocalPreferences.getRegisteredPhone() ?: ""
        
        // Save new credentials
        com.coride.data.local.LocalPreferences.saveRegistrationData(regEmail, regPhone, newPass)
        
        // Security: Disable biometrics after password reset
        com.coride.data.local.LocalPreferences.setBiometricEnabled(false)
        
        // Clear reset state
        generatedOtp = null
        resetTargetEmail = null
    }

    // ── Weather Forecast ──
    private val apiService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            })
            .build()
        
        retrofit2.Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(com.coride.data.api.WeatherApiService::class.java)
    }

    private const val WEATHER_API_KEY = "ce322ec056de4f4a79817ad721da71c7"

    suspend fun getLiveWeather(lat: Double, lon: Double): List<WeatherDay> {
        try {
            val response = apiService.getForecast(lat, lon, WEATHER_API_KEY)
            if (response.isSuccessful) {
                val list = response.body()?.list ?: return getWeatherForecast()
                
                // Group by day (3-hour slots) and pick one representative slot per day
                return list.filterIndexed { index, _ -> index % 8 == 0 } // Roughly 1 entry per day
                    .take(7)
                    .map { item ->
                        val date = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                            .format(java.util.Date(item.dt * 1000))
                        
                        val iconRes = when (item.weather.firstOrNull()?.icon?.take(2)) {
                            "01" -> com.coride.R.drawable.ic_sunny
                            "02", "03", "04" -> com.coride.R.drawable.ic_cloudy
                            "09", "10", "11" -> com.coride.R.drawable.ic_rainy
                            else -> com.coride.R.drawable.ic_cloudy
                        }
                        
                        WeatherDay(
                            day = date,
                            temp = "${Math.round(item.main.temp)}°C",
                            iconRes = iconRes,
                            condition = item.weather.firstOrNull()?.main ?: "Clear"
                        )
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return getWeatherForecast()
    }

    fun getWeatherForecast(): List<WeatherDay> {
        return listOf(
            WeatherDay("Mon", "28°C", com.coride.R.drawable.ic_sunny, "Sunny"),
            WeatherDay("Tue", "26°C", com.coride.R.drawable.ic_cloudy, "Cloudy"),
            WeatherDay("Wed", "24°C", com.coride.R.drawable.ic_rainy, "Rainy"),
            WeatherDay("Thu", "25°C", com.coride.R.drawable.ic_cloudy, "Partly Cloudy"),
            WeatherDay("Fri", "27°C", com.coride.R.drawable.ic_sunny, "Sunny"),
            WeatherDay("Sat", "29°C", com.coride.R.drawable.ic_sunny, "Hot"),
            WeatherDay("Sun", "22°C", com.coride.R.drawable.ic_rainy, "Stormy")
        )
    }
}

