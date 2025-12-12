package com.naptune.lullabyandstory.presentation.fcm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.domain.usecase.fcm.GetFcmTokenUseCase
import com.naptune.lullabyandstory.domain.usecase.fcm.InitializeFcmUseCase
import com.naptune.lullabyandstory.domain.usecase.fcm.RefreshFcmTokenUseCase
import com.naptune.lullabyandstory.domain.usecase.fcm.SubscribeToTopicUseCase
import com.naptune.lullabyandstory.domain.usecase.fcm.UnsubscribeFromTopicUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel for FCM/Notifications
 * Processes user intents and emits UI state
 * Follows MVI pattern with Clean Architecture
 */
@HiltViewModel
class FcmViewModel @Inject constructor(
    private val initializeFcmUseCase: InitializeFcmUseCase,
    private val getFcmTokenUseCase: GetFcmTokenUseCase,
    private val refreshFcmTokenUseCase: RefreshFcmTokenUseCase,
    private val subscribeToTopicUseCase: SubscribeToTopicUseCase,
    private val unsubscribeFromTopicUseCase: UnsubscribeFromTopicUseCase
) : ViewModel() {

    private val TAG = "FcmViewModel"

    // MVI State
    private val _uiState = MutableStateFlow(FcmUiState())
    val uiState: StateFlow<FcmUiState> = _uiState.asStateFlow()

    /**
     * Process user intents - MVI pattern
     */
    fun processIntent(intent: FcmIntent) {
        when (intent) {
            is FcmIntent.InitializeFcm -> initializeFcm()
            is FcmIntent.RequestNotificationPermission -> requestNotificationPermission()
            is FcmIntent.RefreshToken -> refreshToken()
            is FcmIntent.SetNotificationsEnabled -> setNotificationsEnabled(intent.enabled)
            is FcmIntent.SubscribeToTopic -> subscribeToTopic(intent.topic)
            is FcmIntent.UnsubscribeFromTopic -> unsubscribeFromTopic(intent.topic)
            is FcmIntent.ClearError -> clearError()
        }
    }

    /**
     * Initialize FCM - Get token and register with server
     */
    private fun initializeFcm() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            Log.d(TAG, "🚀 Starting FCM initialization process...")

            initializeFcmUseCase().fold(
                onSuccess = { result ->
                    _uiState.update {
                        it.copy(
                            isInitialized = true,
                            isLoading = false,
                            fcmToken = result.token,
                            deviceId = result.deviceId,
                            isTokenRegistered = true,
                            successMessage = "FCM initialized successfully"
                        )
                    }

                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, "✅ FCM INITIALIZATION SUCCESSFUL")
                    Log.d(TAG, "════════════════════════════════════════")
                    Log.d(TAG, "🔑 Token (first 30 chars): ${result.token.take(30)}...")
                    Log.d(TAG, "📱 Device ID: ${result.deviceId}")
                    Log.d(TAG, "📡 Token registered: ${result.isRegistered}")
                    Log.d(TAG, "════════════════════════════════════════")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to initialize FCM"
                        )
                    }
                    Log.e(TAG, "════════════════════════════════════════")
                    Log.e(TAG, "❌ FCM INITIALIZATION FAILED")
                    Log.e(TAG, "════════════════════════════════════════")
                    Log.e(TAG, "Error: ${error.message}", error)
                    Log.e(TAG, "════════════════════════════════════════")
                }
            )
        }
    }

    /**
     * Request notification permission (Android 13+)
     * Note: Actual permission request happens in MainActivity
     * This just updates the state
     */
    private fun requestNotificationPermission() {
        // Permission request is handled in MainActivity
        // This is just a state update
        _uiState.update {
            it.copy(successMessage = "Please grant notification permission")
        }
    }

    /**
     * Update notification permission status
     * Called from MainActivity after permission result
     */
    fun updateNotificationPermission(granted: Boolean) {
        _uiState.update {
            it.copy(hasNotificationPermission = granted)
        }
        if (!granted) {
            _uiState.update {
                it.copy(error = "Notification permission denied")
            }
        }
    }

    /**
     * Refresh FCM token
     */
    private fun refreshToken() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            refreshFcmTokenUseCase().fold(
                onSuccess = { newToken ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            fcmToken = newToken,
                            successMessage = "Token refreshed successfully"
                        )
                    }
                    Log.d(TAG, "Token refreshed: ${newToken.take(20)}...")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to refresh token"
                        )
                    }
                    Log.e(TAG, "Token refresh failed", error)
                }
            )
        }
    }

    /**
     * Enable/Disable notifications
     */
    private fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.update {
            it.copy(notificationsEnabled = enabled)
        }
        Log.d(TAG, "Notifications ${if (enabled) "enabled" else "disabled"}")
    }

    /**
     * Subscribe to FCM topic
     */
    private fun subscribeToTopic(topic: String) {
        viewModelScope.launch {
            subscribeToTopicUseCase(topic).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            subscribedTopics = it.subscribedTopics + topic,
                            successMessage = "Subscribed to $topic"
                        )
                    }
                    Log.d(TAG, "Subscribed to topic: $topic")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Failed to subscribe: ${error.message}")
                    }
                    Log.e(TAG, "Subscribe failed", error)
                }
            )
        }
    }

    /**
     * Unsubscribe from FCM topic
     */
    private fun unsubscribeFromTopic(topic: String) {
        viewModelScope.launch {
            unsubscribeFromTopicUseCase(topic).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            subscribedTopics = it.subscribedTopics - topic,
                            successMessage = "Unsubscribed from $topic"
                        )
                    }
                    Log.d(TAG, "Unsubscribed from topic: $topic")
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = "Failed to unsubscribe: ${error.message}")
                    }
                    Log.e(TAG, "Unsubscribe failed", error)
                }
            )
        }
    }

    /**
     * Clear error state
     */
    private fun clearError() {
        _uiState.update {
            it.copy(error = null, successMessage = null)
        }
    }

    /**
     * Load FCM token from storage on init
     */
    init {
        viewModelScope.launch {
            getFcmTokenUseCase().collect { token ->
                token?.let {
                    _uiState.update { state ->
                        state.copy(fcmToken = it)
                    }
                }
            }
        }
    }
}
