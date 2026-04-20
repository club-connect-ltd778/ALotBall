package uklot.connectionltd.alotbot.ui.main

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import uklot.connectionltd.alotbot.core.PromptFactory
import uklot.connectionltd.alotbot.model.BallType
import uklot.connectionltd.alotbot.network.FalApiClient

class MainViewModel(
    private val falApiClient: FalApiClient
) : ViewModel() {
    companion object {
        private const val TAG = "MelBall/MainViewModel"
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state: StateFlow<UiState> = _state.asStateFlow()

    var pendingBitmap: Bitmap? = null
    var selectedBallType: BallType? = null
    var userText: String = ""

    fun generate(ballType: BallType, userInput: String, sourceBitmap: Bitmap?) {
        selectedBallType = ballType
        userText = userInput
        pendingBitmap = sourceBitmap

        val prompt = PromptFactory.build(ballType, userInput)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                _state.value = UiState.Loading
                val bitmap = falApiClient.generate(prompt, sourceBitmap)
                UiState.Success(bitmap = bitmap, prompt = prompt, ballType = ballType)
            }.onFailure { error ->
                Log.e(TAG, "Generation exception: ${error.message}", error)
                _state.value = UiState.Error(error.localizedMessage ?: "Unknown error")
            }.onSuccess { success ->
                _state.value = success
            }
        }
    }

    fun consumeState() {
        _state.value = UiState.Idle
    }

    sealed class UiState {
        data object Idle : UiState()
        data object Loading : UiState()
        data class Success(
            val bitmap: Bitmap,
            val prompt: String,
            val ballType: BallType
        ) : UiState()

        data class Error(val message: String) : UiState()
    }
}
