package uklot.connectionltd.alotbot.ui.splash

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.onesignal.OneSignal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import uklot.connectionltd.alotbot.core.AppConfig
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivitySplashBinding
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall
import uklot.connectionltd.alotbot.ui.main.MainActivity
import uklot.connectionltd.alotbot.ui.onboarding.OnboardingActivity
import java.time.OffsetDateTime
import java.util.Locale
import java.util.UUID

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding()

        lifecycleScope.launch {
            val splashStartMs = System.currentTimeMillis()
            requestPushPermission()

            val webUrl = runCatching {
                val prefs = AppGraph.prefs(this@SplashActivity)
                val uuid = prefs.deviceUuid.ifBlank {
                    UUID.randomUUID().toString().also { prefs.deviceUuid = it }
                }
                AppGraph.startupRuleClient().fetchSecondRule(
                    simLocale = getSimLocale(),
                    locale = getDeviceLanguage(),
                    uuid = uuid,
                    deviceNameAndModel = getDeviceNameAndModel(),
                    timezone = getCurrentTimezoneOffset()
                )
            }.getOrNull()

            // Keep short splash display to avoid abrupt flash on fast networks.
            val elapsed = System.currentTimeMillis() - splashStartMs
            if (elapsed < SPLASH_MIN_DURATION_MS) {
                delay(SPLASH_MIN_DURATION_MS - elapsed)
            }

            if (!webUrl.isNullOrBlank()) {
                startActivity(WebGateActivity.createIntent(this@SplashActivity, webUrl))
                finish()
                return@launch
            }

            openDefaultFlow()
        }
    }

    private fun openDefaultFlow() {
        val prefs = AppGraph.prefs(this)
        val destination = if (prefs.isOnboardingShown) {
            MainActivity::class.java
        } else {
            OnboardingActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }

    private suspend fun requestPushPermission() {
        runCatching {
            OneSignal.Notifications.requestPermission(true)
        }
    }

    private fun getSimLocale(): String {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as? TelephonyManager
        val iso = telephonyManager?.simCountryIso.orEmpty().trim()
        return iso.take(3).uppercase(Locale.ROOT)
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().language.take(3).lowercase(Locale.ROOT)
    }

    private fun getDeviceNameAndModel(): String {
        val manufacturer = Build.MANUFACTURER.orEmpty().trim()
        val model = Build.MODEL.orEmpty().trim()
        return "$manufacturer $model".trim()
    }

    private fun getCurrentTimezoneOffset(): String {
        val offset = OffsetDateTime.now().offset.id
        return if (offset == "Z") "+00:00" else offset
    }

    companion object {
        private const val SPLASH_MIN_DURATION_MS = 1300L
    }
}
