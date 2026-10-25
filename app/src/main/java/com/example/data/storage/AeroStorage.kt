package com.example.data.storage

import android.content.Context
import android.content.SharedPreferences
import com.example.data.firebase.FirebaseConfig
import com.example.data.firebase.FirebaseSession
import com.example.data.model.PassengerProfile
import com.example.data.model.SimulationStage

class AeroStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("aeroguide_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "key_is_logged_in"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_PHONE = "key_user_phone"
        private const val KEY_USER_FF = "key_user_ff"
        private const val KEY_USER_UID = "key_user_uid"
        private const val KEY_CURRENT_BOOKING = "key_current_booking"
        private const val KEY_CURRENT_STAGE = "key_current_stage"
        private const val KEY_PREF_THEME_DARK = "key_pref_theme_dark"
        private const val KEY_PREF_BOARDING = "key_pref_boarding"
        private const val KEY_PREF_GATE_CHANGE = "key_pref_gate_change"
        private const val KEY_PREF_FINAL_CALL = "key_pref_final_call"
        private const val KEY_PREF_DELAY = "key_pref_delay"

        // Firebase Configuration Keys
        private const val KEY_FIREBASE_API_KEY = "key_firebase_api_key"
        private const val KEY_FIREBASE_PROJECT_ID = "key_firebase_project_id"
        private const val KEY_FIREBASE_APP_ID = "key_firebase_app_id"
        private const val KEY_FIREBASE_ACCESS_TOKEN = "key_firebase_access_token"
        private const val KEY_FIREBASE_REFRESH_TOKEN = "key_firebase_refresh_token"
    }

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_PREF_THEME_DARK, true)

    fun setDarkTheme(isDark: Boolean) {
        prefs.edit().putBoolean(KEY_PREF_THEME_DARK, isDark).apply()
    }

    fun getUserName(email: String): String {
        val cleanEmail = email.trim().lowercase()
        val defaultName = cleanEmail.substringBefore("@").replace(".", " ")
            .split(" ")
            .joinToString(" ") { part -> part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } }
            .ifBlank { "Passenger" }
        return prefs.getString("user_name_$cleanEmail", defaultName) ?: defaultName
    }

    fun setLoggedIn(isLoggedIn: Boolean, email: String? = null, name: String? = null, uid: String? = null) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (email != null) editor.putString(KEY_USER_EMAIL, email)
        if (name != null) {
            editor.putString(KEY_USER_NAME, name)
            if (email != null) editor.putString("user_name_${email.trim().lowercase()}", name)
        }
        if (uid != null) editor.putString(KEY_USER_UID, uid)
        if (!isLoggedIn) {
            editor.remove(KEY_FIREBASE_ACCESS_TOKEN)
            editor.remove(KEY_FIREBASE_REFRESH_TOKEN)
            editor.remove(KEY_USER_UID)
        }
        editor.apply()
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""

    fun getUserUid(): String = prefs.getString(KEY_USER_UID, "usr_demo") ?: "usr_demo"

    fun getFirebaseConfig(): FirebaseConfig {
        val defaultKey = try { com.example.BuildConfig.FIREBASE_API_KEY } catch (_: Exception) { "" }
        val defaultProj = try { com.example.BuildConfig.FIREBASE_PROJECT_ID } catch (_: Exception) { "" }
        val defaultAppId = try { com.example.BuildConfig.FIREBASE_APP_ID } catch (_: Exception) { "" }

        val storedKey = prefs.getString(KEY_FIREBASE_API_KEY, null)
        val storedProj = prefs.getString(KEY_FIREBASE_PROJECT_ID, null)
        val storedAppId = prefs.getString(KEY_FIREBASE_APP_ID, null)

        return FirebaseConfig(
            apiKey = storedKey?.takeIf { it.isNotBlank() } ?: defaultKey,
            projectId = storedProj?.takeIf { it.isNotBlank() } ?: defaultProj,
            appId = storedAppId?.takeIf { it.isNotBlank() } ?: defaultAppId
        )
    }

    fun saveFirebaseConfig(apiKey: String, projectId: String, appId: String = "") {
        prefs.edit()
            .putString(KEY_FIREBASE_API_KEY, apiKey.trim())
            .putString(KEY_FIREBASE_PROJECT_ID, projectId.trim())
            .putString(KEY_FIREBASE_APP_ID, appId.trim())
            .apply()
    }

    fun saveFirebaseSession(session: FirebaseSession) {
        prefs.edit()
            .putString(KEY_FIREBASE_ACCESS_TOKEN, session.accessToken)
            .putString(KEY_FIREBASE_REFRESH_TOKEN, session.refreshToken)
            .putString(KEY_USER_UID, session.user.uid)
            .putString(KEY_USER_EMAIL, session.user.email)
            .apply()

        session.user.fullName?.let { name ->
            prefs.edit().putString(KEY_USER_NAME, name).apply()
        }
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_FIREBASE_ACCESS_TOKEN)
            .remove(KEY_FIREBASE_REFRESH_TOKEN)
            .remove(KEY_USER_UID)
            .apply()
    }

    fun getProfile(): PassengerProfile {
        val storedEmail = getUserEmail()
        val defaultName = if (storedEmail.isNotBlank()) getUserName(storedEmail) else "Passenger"
        return PassengerProfile(
            id = getUserUid(),
            fullName = prefs.getString(KEY_USER_NAME, defaultName) ?: defaultName,
            email = storedEmail,
            phone = prefs.getString(KEY_USER_PHONE, "+91 98201 54321") ?: "+91 98201 54321",
            frequentFlyerNumber = prefs.getString(KEY_USER_FF, "AG-GOX-PLATINUM") ?: "AG-GOX-PLATINUM"
        )
    }

    fun saveProfile(profile: PassengerProfile) {
        prefs.edit()
            .putString(KEY_USER_NAME, profile.fullName)
            .putString(KEY_USER_PHONE, profile.phone)
            .putString(KEY_USER_FF, profile.frequentFlyerNumber)
            .apply()
    }

    fun getCurrentBookingReference(): String = prefs.getString(KEY_CURRENT_BOOKING, "AG7X92") ?: "AG7X92"

    fun saveCurrentBookingReference(ref: String) {
        prefs.edit().putString(KEY_CURRENT_BOOKING, ref).apply()
    }

    fun getLastSimulationStage(): SimulationStage {
        val stageName = prefs.getString(KEY_CURRENT_STAGE, SimulationStage.BOARDING.name)
        return try {
            SimulationStage.valueOf(stageName ?: SimulationStage.BOARDING.name)
        } catch (_: Exception) {
            SimulationStage.BOARDING
        }
    }

    fun saveSimulationStage(stage: SimulationStage) {
        prefs.edit().putString(KEY_CURRENT_STAGE, stage.name).apply()
    }

    fun getNotificationPreferences(): Map<String, Boolean> {
        return mapOf(
            "boarding" to prefs.getBoolean(KEY_PREF_BOARDING, true),
            "gate_change" to prefs.getBoolean(KEY_PREF_GATE_CHANGE, true),
            "final_call" to prefs.getBoolean(KEY_PREF_FINAL_CALL, true),
            "delay" to prefs.getBoolean(KEY_PREF_DELAY, true)
        )
    }

    fun saveNotificationPreferences(boarding: Boolean, gateChange: Boolean, finalCall: Boolean, delay: Boolean) {
        prefs.edit()
            .putBoolean(KEY_PREF_BOARDING, boarding)
            .putBoolean(KEY_PREF_GATE_CHANGE, gateChange)
            .putBoolean(KEY_PREF_FINAL_CALL, finalCall)
            .putBoolean(KEY_PREF_DELAY, delay)
            .apply()
    }
}
