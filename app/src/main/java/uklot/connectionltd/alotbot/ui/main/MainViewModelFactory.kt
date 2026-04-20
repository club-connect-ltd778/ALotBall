package uklot.connectionltd.alotbot.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import uklot.connectionltd.alotbot.network.FalApiClient

class MainViewModelFactory(
    private val falApiClient: FalApiClient
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(falApiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
