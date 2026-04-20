package uklot.connectionltd.alotbot.ui.history

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivityHistoryBinding
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall
import uklot.connectionltd.alotbot.ui.result.ResultActivity

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val adapter = HistoryAdapter { record ->
        startActivity(
            Intent(this, ResultActivity::class.java).apply {
                putExtra(ResultActivity.EXTRA_PROMPT, record.prompt)
                putExtra(ResultActivity.EXTRA_BALL_TYPE, record.ballType.name)
                putExtra(ResultActivity.EXTRA_IMAGE_PATH, record.imagePath)
                putExtra(ResultActivity.EXTRA_IS_NEW_GENERATION, false)
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding()

        binding.backButton.setOnClickListener { finish() }
        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        binding.recycler.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val data = AppGraph.historyRepository(this).getAll()
        adapter.submit(data)
    }
}
