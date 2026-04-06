package com.indrive.clone.data.local

import android.content.Context
import android.content.SharedPreferences
import com.indrive.clone.data.model.User
import com.indrive.clone.data.model.UserRole
import com.indrive.clone.data.model.VerificationStatus

import com.indrive.clone.data.model.TrustedContact

object LocalPreferences {
    private const val PREFS_NAME = "coride_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun serializeContacts(contacts: List<TrustedContact>): String {
        return contacts.joinToString("|") { "${it.id}#${it.name}#${it.phone}#${it.relation}" }
    }

    private fun deserializeContacts(raw: String): List<TrustedContact> {
        if (raw.isEmpty()) return emptyList()
        return raw.split("|").mapNotNull {
            val parts = it.split("#")
            if (parts.size == 4) {
                TrustedContact(parts[0], parts[1], parts[2], parts[3])
            } else null
        }
    }

    fun saveUser(user: User) {
        prefs.edit()
            .putString("id", user.id)
            .putString("name", user.name)
            .putString("phone", user.phone)
            .putString("email", user.email)
            .putFloat("rating", user.rating)
            .putInt("totalRides", user.totalRides)
            .putString("memberSince", user.memberSince)
            .putString("role", user.role.name)
            .putString("organizationName", user.organizationName)
            .putString("organizationAddress", user.organizationAddress)
            .putString("homeAddress", user.homeAddress)
            .putString("cnicNumber", user.cnicNumber)
            .putString("verificationStatus", user.verificationStatus.name)
            .putString("emergencyContactName", user.emergencyContactName)
            .putString("emergencyContactPhone", user.emergencyContactPhone)
            .putString("idCardImagePath", user.idCardImagePath)
            .putString("trustedContacts", serializeContacts(user.trustedContacts))
            .putBoolean("isLoggedIn", user.name.isNotEmpty())
            .apply()
    }

    fun saveRegistrationData(email: String, phone: String, pass: String, studentId: String = "") {
        prefs.edit()
            .putString("registeredEmail", email)
            .putString("registeredPhone", phone)
            .putString("registeredPassword", pass)
            .putString("registeredStudentId", studentId)
            .apply()
    }

    fun getRegisteredEmail(): String? = prefs.getString("registeredEmail", null)
    fun getRegisteredPhone(): String? = prefs.getString("registeredPhone", null)
    fun getRegisteredPassword(): String? = prefs.getString("registeredPassword", null)
    fun getRegisteredStudentId(): String? = prefs.getString("registeredStudentId", null)

    fun getUser(): User? {
        if (!prefs.getBoolean("isLoggedIn", false)) return null

        return User(
            id = prefs.getString("id", "") ?: "",
            name = prefs.getString("name", "") ?: "",
            phone = prefs.getString("phone", "") ?: "",
            email = prefs.getString("email", "") ?: "",
            rating = prefs.getFloat("rating", 5.0f),
            totalRides = prefs.getInt("totalRides", 0),
            memberSince = prefs.getString("memberSince", "Just now") ?: "Just now",
            role = UserRole.valueOf(prefs.getString("role", UserRole.STUDENT.name) ?: UserRole.STUDENT.name),
            organizationName = prefs.getString("organizationName", "") ?: "",
            organizationAddress = prefs.getString("organizationAddress", "") ?: "",
            homeAddress = prefs.getString("homeAddress", "") ?: "",
            cnicNumber = prefs.getString("cnicNumber", "") ?: "",
            verificationStatus = VerificationStatus.valueOf(prefs.getString("verificationStatus", VerificationStatus.PENDING.name) ?: VerificationStatus.PENDING.name),
            emergencyContactName = prefs.getString("emergencyContactName", "") ?: "",
            emergencyContactPhone = prefs.getString("emergencyContactPhone", "") ?: "",
            idCardImagePath = prefs.getString("idCardImagePath", "") ?: "",
            trustedContacts = deserializeContacts(prefs.getString("trustedContacts", "") ?: "")
        )
    }

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", value).apply()
    }

    fun isBiometricEnabled(): Boolean = prefs.getBoolean("isBiometricEnabled", false)

    fun setBiometricEnabled(value: Boolean) {
        prefs.edit().putBoolean("isBiometricEnabled", value).apply()
    }

    fun clearUser() {
        prefs.edit().putBoolean("isLoggedIn", false).apply()
    }

    /**
     * Complete wipe of all app preferences for account deletion.
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
