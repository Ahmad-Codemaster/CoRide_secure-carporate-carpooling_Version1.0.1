package com.indrive.clone.utils

import android.util.Log
import com.indrive.clone.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Automated Administrative Alert System for CoRide.
 * Dispatches background emails for Registration and Verification events.
 */
object EmailNotificationHelper {

    private const val TAG = "EmailNotificationHelper"
    
    // --- CONFIGURATION ---
    private const val SENDER_EMAIL = "ahmad.202406455@gcuf.edu.pk"
    private const val RECIPIENT_EMAIL = "ahmad.202406455@gcuf.edu.pk"
    
    /**
     * @IMPORTANT: ADD YOUR 16-CHARACTER APP PASSWORD HERE
     * To generate: Google Account -> Security -> 2-Step Verification -> App Passwords
     */
    private const val APP_PASSWORD = "sack sbte tubz xifv\n"

    private val smtpProps = Properties().apply {
        put("mail.smtp.host", "smtp.gmail.com")
        put("mail.smtp.socketFactory.port", "465")
        put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        put("mail.smtp.auth", "true")
        put("mail.smtp.port", "465")
    }

    /**
     * Send a real-time recovery 4-digit OTP to the user's email.
     */
    fun sendOtpEmail(toEmail: String, otp: String) {
        val subject = "🔐 CoRide Security Code: $otp"
        val body = """
            <html>
            <body style="font-family: sans-serif; background-color: #f8f9fa; padding: 20px;">
                <div style="max-width: 500px; margin: auto; background: #ffffff; border-radius: 12dp; border: 1px solid #e0e0e0; overflow: hidden;">
                    <div style="background-color: #D32F2F; color: #ffffff; padding: 24px; text-align: center;">
                        <h2 style="margin: 0; font-size: 22px;">CoRide Security</h2>
                    </div>
                    <div style="padding: 32px; text-align: center;">
                        <h3 style="color: #333333; margin-top: 0;">Password Recovery Code</h3>
                        <p style="color: #666666; font-size: 14px;">Use the following code to reset your CoRide profile password. This code will expire soon.</p>
                        
                        <div style="background-color: #f1f3f4; border-radius: 8px; padding: 20px; margin: 24px 0; display: inline-block;">
                            <span style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #D32F2F;">$otp</span>
                        </div>
                        
                        <p style="color: #999999; font-size: 12px; margin-top: 24px;">If you did not request this, please ignore this email or contact support immediately.</p>
                    </div>
                    <div style="background-color: #f1f1f1; padding: 12px; text-align: center; color: #999; font-size: 11px;">
                        &copy; 2026 CoRide Identity Protection
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        dispatchEmail(toEmail, subject, body, isAdminAlert = false)
    }

    /**
     * Send an automated alert email when a new student registers.
     */
    fun sendRegistrationAlert(user: User) {
        val subject = "🆕 New CoRide Registration: ${user.name}"
        val body = generateDataTableHtml("Student Registration Details", user)
        dispatchEmail(RECIPIENT_EMAIL, subject, body, isAdminAlert = true)
    }

    /**
     * Send an automated alert email when a student's identity is verified.
     */
    fun sendVerificationAlert(user: User) {
        val subject = "✅ Profile Verified: ${user.name}"
        val body = generateDataTableHtml("Institutional Verification Confirmed", user)
        dispatchEmail(RECIPIENT_EMAIL, subject, body, isAdminAlert = true)
    }

    /**
     * Send a critical alert email when an account is deleted.
     */
    fun sendAccountDeletionAlert(user: User) {
        val subject = "🚨 Account Deleted: ${user.name}"
        val body = generateDataTableHtml("Account & User Data Purged", user)
        dispatchEmail(RECIPIENT_EMAIL, subject, body, isAdminAlert = true)
    }

    /**
     * Send a critical SOS emergency alert to the admin with GPS coordinates.
     */
    fun sendSosAlert(user: User, lat: Double, lng: Double) {
        val subject = "🆘 EMERGENCY SOS: ${user.name}"
        val locationLink = "https://maps.google.com/?q=$lat,$lng"
        val body = """
            <html>
            <body style="font-family: sans-serif; background-color: #f8f9fa; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 12px; border: 2px solid #BA1A1A; overflow: hidden;">
                    <div style="background-color: #BA1A1A; color: #ffffff; padding: 20px; text-align: center;">
                        <h2 style="margin: 0; font-size: 22px;">🆘 EMERGENCY SOS ALERT</h2>
                    </div>
                    <div style="padding: 24px;">
                        <h3 style="color: #BA1A1A; margin-top: 0;">IMMEDIATE ATTENTION REQUIRED</h3>
                        <p style="color: #333;"><strong>${user.name}</strong> has triggered an emergency SOS during an active ride.</p>
                        
                        <table style="width: 100%; border-collapse: collapse; margin-top: 16px;">
                            <tr><td style="padding: 8px; border-bottom: 1px solid #ddd; color: #777;">Name</td><td style="padding: 8px; border-bottom: 1px solid #ddd; font-weight: bold;">${user.name}</td></tr>
                            <tr><td style="padding: 8px; border-bottom: 1px solid #ddd; color: #777;">Phone</td><td style="padding: 8px; border-bottom: 1px solid #ddd;">${user.phone}</td></tr>
                            <tr><td style="padding: 8px; border-bottom: 1px solid #ddd; color: #777;">Email</td><td style="padding: 8px; border-bottom: 1px solid #ddd;">${user.email}</td></tr>
                            <tr><td style="padding: 8px; border-bottom: 1px solid #ddd; color: #777;">Organization</td><td style="padding: 8px; border-bottom: 1px solid #ddd;">${user.organizationName}</td></tr>
                            <tr style="background-color: #FFF3F3;"><td style="padding: 8px; border-bottom: 1px solid #ddd; color: #BA1A1A; font-weight: bold;">📍 Location</td><td style="padding: 8px; border-bottom: 1px solid #ddd;"><a href="$locationLink" style="color: #BA1A1A; font-weight: bold;">TRACK ON MAP</a></td></tr>
                            <tr><td style="padding: 8px; color: #777;">Coordinates</td><td style="padding: 8px;">$lat, $lng</td></tr>
                        </table>
                    </div>
                    <div style="background-color: #FFF3F3; padding: 12px; text-align: center; color: #BA1A1A; font-size: 12px; font-weight: bold;">
                        ⚠️ Contact emergency services (15) if the user is unreachable
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
        dispatchEmail(RECIPIENT_EMAIL, subject, body, isAdminAlert = true)
    }

    private fun dispatchEmail(toEmail: String, subject: String, htmlBody: String, isAdminAlert: Boolean) {
        if (APP_PASSWORD == "ENTER_YOUR_APP_PASSWORD_HERE") {
            Log.w(TAG, "Email Dispatch skipped: App Password not configured.")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val session = Session.getInstance(smtpProps, object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication {
                        return PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD.trim())
                    }
                })

                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(SENDER_EMAIL))
                    addRecipient(Message.RecipientType.TO, InternetAddress(toEmail))
                    setSubject(subject)
                    setContent(htmlBody, "text/html; charset=utf-8")
                }

                Transport.send(message)
                Log.d(TAG, "Email sent successfully to $toEmail")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to dispatch email: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Generates a professional HTML table for the administrative alert.
     */
    private fun generateDataTableHtml(title: String, user: User): String {
        return """
            <html>
            <body style="font-family: sans-serif; background-color: #f8f9fa; padding: 20px;">
                <div style="max-width: 600px; margin: auto; background: #ffffff; border-radius: 12px; border: 1px solid #e0e0e0; overflow: hidden;">
                    <div style="background-color: #D32F2F; color: #ffffff; padding: 20px; text-align: center;">
                        <h2 style="margin: 0; font-size: 20px;">CoRide Admin Oversight</h2>
                    </div>
                    <div style="padding: 24px;">
                        <h3 style="color: #333333; margin-top: 0;">$title</h3>
                        <p style="color: #666666; font-size: 14px;">The following student information has been captured within the CoRide platform:</p>
                        
                        <table style="width: 100%; border-collapse: collapse; margin-top: 16px;">
                            <tr style="background-color: #f2f2f2;">
                                <th style="text-align: left; padding: 12px; border-bottom: 1px solid #ddd; width: 40%;">Field</th>
                                <th style="text-align: left; padding: 12px; border-bottom: 1px solid #ddd;">Data Value</th>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Full Name</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; font-weight: bold;">${user.name}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Organization</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd;">${user.organizationName}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Registration ID</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd;">${user.cnicNumber}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Mobile Number</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd;">${user.phone}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Email Address</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd;">${user.email}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Home Address</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd;">${user.homeAddress}</td>
                            </tr>
                            <tr>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #777;">Identity Status</td>
                                <td style="padding: 12px; border-bottom: 1px solid #ddd; color: #00796B; font-weight: bold;">${user.verificationStatus.name}</td>
                            </tr>
                        </table>
                    </div>
                    <div style="background-color: #f1f1f1; padding: 12px; text-align: center; color: #999; font-size: 11px;">
                        &copy; 2026 CoRide Verification System | University Community Safety
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}
