package uklot.connectionltd.alotbot.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import uklot.connectionltd.alotbot.R

data class OnboardingPage(
    @DrawableRes val backgroundRes: Int,
    @StringRes val buttonTextRes: Int
) {
    companion object {
        val pages = listOf(
            OnboardingPage(
                backgroundRes = R.drawable.ios_onboarding_first,
                buttonTextRes = R.string.go_uppercase
            ),
            OnboardingPage(
                backgroundRes = R.drawable.ios_onboarding_second,
                buttonTextRes = R.string.lets_get_started_uppercase
            )
        )
    }
}
