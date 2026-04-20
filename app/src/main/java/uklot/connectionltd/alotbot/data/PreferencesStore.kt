package uklot.connectionltd.alotbot.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var isOnboardingShown: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_SHOWN, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_SHOWN, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS, false)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS, value).apply()

    var historyJson: String
        get() = prefs.getString(KEY_HISTORY_JSON, "[]") ?: "[]"
        set(value) = prefs.edit().putString(KEY_HISTORY_JSON, value).apply()

    var deviceUuid: String
        get() = prefs.getString(KEY_DEVICE_UUID, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DEVICE_UUID, value).apply()

    companion object {
        private const val PREFS_NAME = "melball_prefs"
        private const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
        private const val KEY_NOTIFICATIONS = "notifications_enabled"
        private const val KEY_HISTORY_JSON = "history_json"
        private const val KEY_DEVICE_UUID = "device_uuid"
    }
}
