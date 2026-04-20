package uklot.connectionltd.alotbot

import android.app.Application
import com.onesignal.OneSignal
import uklot.connectionltd.alotbot.core.AppConfig

class MelBallApp : Application() {
    override fun onCreate() {
        super.onCreate()
        if (AppConfig.oneSignalAppId.isNotBlank() &&
            AppConfig.oneSignalAppId != "REPLACE_WITH_ONESIGNAL_APP_ID"
        ) {
            OneSignal.initWithContext(this, AppConfig.oneSignalAppId)
        }
    }
}
