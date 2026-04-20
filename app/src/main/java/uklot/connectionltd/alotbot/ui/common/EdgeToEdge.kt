package uklot.connectionltd.alotbot.ui.common

import android.app.Activity
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

fun Activity.enableEdgeToEdgeForMelBall() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

fun View.applySystemBarsPadding(
    applyTop: Boolean = true,
    applyBottom: Boolean = true
) {
    val initialTop = paddingTop
    val initialBottom = paddingBottom
    val initialStart = paddingStart
    val initialEnd = paddingEnd

    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars: Insets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        v.setPadding(
            initialStart,
            initialTop + if (applyTop) bars.top else 0,
            initialEnd,
            initialBottom + if (applyBottom) bars.bottom else 0
        )
        insets
    }
    ViewCompat.requestApplyInsets(this)
}
