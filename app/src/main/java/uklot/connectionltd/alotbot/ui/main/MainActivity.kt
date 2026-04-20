package uklot.connectionltd.alotbot.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.launch
import uklot.connectionltd.alotbot.R
import uklot.connectionltd.alotbot.core.AppGraph
import uklot.connectionltd.alotbot.databinding.ActivityMainBinding
import uklot.connectionltd.alotbot.model.BallType
import uklot.connectionltd.alotbot.ui.common.applySystemBarsPadding
import uklot.connectionltd.alotbot.ui.common.enableEdgeToEdgeForMelBall
import uklot.connectionltd.alotbot.ui.history.HistoryActivity
import uklot.connectionltd.alotbot.ui.result.ResultActivity
import uklot.connectionltd.alotbot.ui.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MelBall/MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(AppGraph.falApiClient())
    }

    private var selectedBallType: BallType? = null
    private var selectedBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            contentResolver.openInputStream(uri)?.use { input ->
                selectedBitmap = android.graphics.BitmapFactory.decodeStream(input)
            }
            updateAttachedPreview()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            takePictureLauncher.launch(null)
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        selectedBitmap = bitmap
        updateAttachedPreview()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdgeForMelBall()
        binding.root.applySystemBarsPadding()

        setupBallTypes()
        setupActions()
        updateAttachedPreview()
        setupLoadingAnimation()
        observeState()
    }

    private fun setupBallTypes() {
        val adapter = BallTypeAdapter { type ->
            selectedBallType = type
            runGeneration()
        }
        binding.ballsRecycler.layoutManager = GridLayoutManager(this, 3)
        binding.ballsRecycler.adapter = adapter
    }

    private fun setupActions() {
        binding.settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        binding.historyButton.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.paperclipButton.setOnClickListener {
            showAttachDialog()
        }
        binding.clearAttachedImageButton.setOnClickListener {
            selectedBitmap = null
            updateAttachedPreview()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    MainViewModel.UiState.Idle -> setLoadingVisible(false)

                    MainViewModel.UiState.Loading -> setLoadingVisible(true)

                    is MainViewModel.UiState.Success -> {
                        setLoadingVisible(false)
                        val intent = Intent(this@MainActivity, ResultActivity::class.java).apply {
                            putExtra(ResultActivity.EXTRA_PROMPT, state.prompt)
                            putExtra(ResultActivity.EXTRA_BALL_TYPE, state.ballType.name)
                            putExtra(ResultActivity.EXTRA_IS_NEW_GENERATION, true)
                        }
                        ResultActivity.cacheBitmap = state.bitmap
                        startActivity(intent)
                        viewModel.consumeState()
                    }

                    is MainViewModel.UiState.Error -> {
                        setLoadingVisible(false)
                        Log.e(TAG, "Generation failed: ${state.message}")
                        Toast.makeText(this@MainActivity, state.message, Toast.LENGTH_LONG).show()
                        viewModel.consumeState()
                    }
                }
            }
        }
    }

    private fun showAttachDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.attach_photo_question))
            .setPositiveButton(getString(R.string.camera)) { _, _ -> handleCamera() }
            .setNegativeButton(getString(R.string.add_from_photos)) { _, _ -> handleGallery() }
            .setNeutralButton(getString(R.string.cancel)) { _, _ ->
            }
            .show()
    }

    private fun handleCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            takePictureLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleGallery() {
        pickImageLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    private fun runGeneration() {
        val ballType = selectedBallType ?: return
        val prompt = binding.promptInput.text?.toString().orEmpty()
        Log.d(TAG, "Run generation: type=${ballType.name}, withImage=${selectedBitmap != null}, promptLength=${prompt.length}")
        viewModel.generate(
            ballType = ballType,
            userInput = prompt,
            sourceBitmap = selectedBitmap
        )
    }

    private fun updateAttachedPreview() {
        val bitmap = selectedBitmap
        if (bitmap == null) {
            binding.attachedImageCard.visibility = View.GONE
            binding.attachedImagePreview.setImageDrawable(null)
        } else {
            binding.attachedImageCard.visibility = View.VISIBLE
            binding.attachedImagePreview.setImageBitmap(bitmap)
        }
    }

    private fun setupLoadingAnimation() {
        val rawFields = runCatching {
            Class.forName("uklot.connectionltd.alotbot.R\$raw").fields.toList()
        }.getOrElse {
            emptyList()
        }
        if (rawFields.isEmpty()) {
            Log.w(TAG, "No raw animation files found. Falling back to ProgressBar.")
            binding.loadingGroup.loadingAnimation.visibility = View.GONE
            binding.loadingGroup.loadingFallback.visibility = View.VISIBLE
            return
        }

        val firstRaw = rawFields.firstOrNull { it.type == Int::class.javaPrimitiveType }
        val rawResId = firstRaw?.getInt(null)
        if (rawResId == null || rawResId == 0) {
            Log.w(TAG, "Cannot resolve raw resource id. Falling back to ProgressBar.")
            binding.loadingGroup.loadingAnimation.visibility = View.GONE
            binding.loadingGroup.loadingFallback.visibility = View.VISIBLE
            return
        }

        Log.d(TAG, "Using raw animation: ${firstRaw.name} ($rawResId)")
        binding.loadingGroup.loadingAnimation.setFailureListener { error ->
            Log.e(TAG, "Loading animation failed: ${error.message}", error)
            binding.loadingGroup.loadingAnimation.visibility = View.GONE
            binding.loadingGroup.loadingFallback.visibility = View.VISIBLE
        }
        binding.loadingGroup.loadingAnimation.setAnimation(rawResId)
        binding.loadingGroup.loadingAnimation.repeatCount = com.airbnb.lottie.LottieDrawable.INFINITE
        binding.loadingGroup.loadingAnimation.visibility = View.VISIBLE
        binding.loadingGroup.loadingFallback.visibility = View.GONE
    }

    private fun setLoadingVisible(visible: Boolean) {
        binding.loadingGroup.root.visibility = if (visible) View.VISIBLE else View.GONE
        if (binding.loadingGroup.loadingAnimation.visibility == View.VISIBLE) {
            if (visible) {
                binding.loadingGroup.loadingAnimation.playAnimation()
            } else {
                binding.loadingGroup.loadingAnimation.cancelAnimation()
            }
        }
    }
}
