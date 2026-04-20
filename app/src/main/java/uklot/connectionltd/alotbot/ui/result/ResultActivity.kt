package uklot.connectionltd.alotbot.ui.result

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import uklot.connectionltd.alotbot.R
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivityResultBinding
import uklot.connectionltd.alotbot.model.BallType
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall
import java.io.File
import java.io.FileOutputStream

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding
    private lateinit var savedBitmap: Bitmap
    private lateinit var prompt: String
    private lateinit var ballType: BallType
    private var currentImagePath: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding()

        val imagePath = intent.getStringExtra(EXTRA_IMAGE_PATH)
        currentImagePath = imagePath
        savedBitmap = cacheBitmap
            ?: imagePath?.let { BitmapFactory.decodeFile(it) }
            ?: run {
                finish()
                return
            }
        prompt = intent.getStringExtra(EXTRA_PROMPT).orEmpty()
        ballType = runCatching {
            BallType.valueOf(intent.getStringExtra(EXTRA_BALL_TYPE).orEmpty())
        }.getOrElse { BallType.FOOTBALL }
        val isNewGeneration = intent.getBooleanExtra(EXTRA_IS_NEW_GENERATION, true)
        cacheBitmap = null

        binding.resultImage.setImageBitmap(savedBitmap)
        if (isNewGeneration) {
            val createdRecord = AppGraph.historyRepository(this).add(savedBitmap, prompt, ballType)
            currentImagePath = createdRecord.imagePath
        }

        binding.backButton.setOnClickListener { finish() }
        binding.deleteButton.setOnClickListener {
            currentImagePath?.let { path ->
                AppGraph.historyRepository(this).deleteByImagePath(path)
            }
            finish()
        }
        binding.shareButton.setOnClickListener { shareImage() }
    }

    private fun shareImage() {
        val file = File(cacheDir, "shared_melball_${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { out ->
            savedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
    }

    companion object {
        const val EXTRA_PROMPT = "extra_prompt"
        const val EXTRA_BALL_TYPE = "extra_ball_type"
        const val EXTRA_IMAGE_PATH = "extra_image_path"
        const val EXTRA_IS_NEW_GENERATION = "extra_is_new_generation"
        var cacheBitmap: Bitmap? = null
    }
}
