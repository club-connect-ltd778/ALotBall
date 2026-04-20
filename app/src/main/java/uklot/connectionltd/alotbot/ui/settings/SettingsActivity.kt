package uklot.connectionltd.alotbot.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uklot.connectionltd.alotbot.R
import uklot.connectionltd.alotbot.core.AppConfig
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivitySettingsBinding
import uklot.connectionltd.alotbot.ui.common.InAppBrowser
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding()

        val prefs = AppGraph.prefs(this)
        binding.backButton.setOnClickListener { finish() }
        binding.notificationsSwitch.isChecked = prefs.notificationsEnabled
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.notificationsEnabled = isChecked
        }

        binding.clearHistoryRow.setOnClickListener {
            AppGraph.historyRepository(this).clear()
            Toast.makeText(this, R.string.history_cleared, Toast.LENGTH_SHORT).show()
        }
        binding.helpCenterRow.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:${AppConfig.supportEmail}")
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support))
            }
            runCatching { startActivity(emailIntent) }
        }
        binding.feedbackRow.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            runCatching { startActivity(intent) }
        }
        binding.privacyRow.setOnClickListener {
            runCatching { InAppBrowser.open(this, AppConfig.privacyUrl) }
        }
    }
}
