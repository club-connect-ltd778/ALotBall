package uklot.connectionltd.alotbot.ui.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivityOnboardingBinding
import uklot.connectionltd.alotbot.model.OnboardingPage
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall
import uklot.connectionltd.alotbot.ui.main.MainActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnboardingBinding
    private val pages = OnboardingPage.pages
    private lateinit var adapter: OnboardingPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding(applyTop = false, applyBottom = true)

        adapter = OnboardingPagerAdapter(pages)
        binding.pager.adapter = adapter
        binding.pager.isUserInputEnabled = false
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButton(position)
            }
        })

        binding.actionButton.setOnClickListener {
            val current = binding.pager.currentItem
            if (current == pages.lastIndex) {
                AppGraph.prefs(this).isOnboardingShown = true
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                binding.pager.currentItem = current + 1
            }
        }

        updateButton(0)
    }

    private fun updateButton(position: Int) {
        binding.actionButton.setText(pages[position].buttonTextRes)
    }
}
