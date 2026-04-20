package uklot.connectionltd.alotbot.ui.splash

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import uklot.connectionltd.alotbot.ui.common.InAppBrowser

class WebGateActivity : AppCompatActivity() {
    private var customTabOpened = false
    private var appWentToBackground = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (url.isBlank()) {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (customTabOpened) return

        val url = intent.getStringExtra(EXTRA_URL).orEmpty()
        if (url.isBlank()) {
            finish()
            return
        }

        customTabOpened = true
        InAppBrowser.open(this, url)
    }

    override fun onStop() {
        super.onStop()
        if (customTabOpened) {
            appWentToBackground = true
        }
    }

    override fun onResume() {
        super.onResume()
        if (customTabOpened && appWentToBackground) {
            finishAffinity()
        }
    }

    companion object {
        private const val EXTRA_URL = "extra.url"

        fun createIntent(context: Context, url: String): Intent {
            return Intent(context, WebGateActivity::class.java)
                .putExtra(EXTRA_URL, url)
        }
    }
}
