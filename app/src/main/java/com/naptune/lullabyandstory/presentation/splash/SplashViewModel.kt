package com.naptune.lullabyandstory.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPreferences: AppPreferences, // ✅ Inject AppPreferences
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper // ✅ Analytics
) : ViewModel() {

    private val _state = MutableStateFlow(SplashState())
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        startSplash()
        // ✅ Track screen view
        trackScreenView()
    }

    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Splash",
            screenClass = "SplashScreen"
        )
    }

    private fun startSplash() {
        viewModelScope.launch {
            // Simple 3 second delay
            delay(2000)
            _state.value = _state.value.copy(
                isLoading = false,
                shouldNavigateToNext = true
            )
        }
    }

    // ✅ NEW: Mark splash screen as shown (separate from language first launch)
    fun markSplashShown() {
        viewModelScope.launch {
            appPreferences.setSplashScreenShown()
        }
    }
}
