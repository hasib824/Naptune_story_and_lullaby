package com.naptune.lullabyandstory.presentation.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naptune.lullabyandstory.data.billing.BillingManager
import com.naptune.lullabyandstory.data.billing.PurchaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val billingManager: BillingManager,
    // ✅ Analytics
    private val analyticsHelper: com.naptune.lullabyandstory.utils.analytics.AnalyticsHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(PremiumUiState())
    val uiState: StateFlow<PremiumUiState> = _uiState.asStateFlow()

    init {
        // ✅ Track screen view
        trackScreenView()
        observeBillingState()
    }

    /**
     * ✅ Track screen view
     */
    private fun trackScreenView() {
        analyticsHelper.logScreenView(
            screenName = "Premium",
            screenClass = "PremiumScreen"
        )
    }

    private fun observeBillingState() {
        viewModelScope.launch {
            combine(
                billingManager.billingConnectionState,
                billingManager.purchaseState,
                billingManager.isPurchased
            ) { connectionState, purchaseState, isPremium ->
                _uiState.value = _uiState.value.copy(
                    isLoading = purchaseState is PurchaseState.Loading,
                    isPremium = isPremium,
                    errorMessage = when (purchaseState) {
                        is PurchaseState.Error -> purchaseState.message
                        else -> null
                    },
                    isConnectionError = connectionState == com.naptune.lullabyandstory.data.billing.BillingConnectionState.ERROR
                )

                // Handle successful purchase
                if (purchaseState is PurchaseState.Success) {
                    _uiState.value = _uiState.value.copy(
                        showSuccessMessage = true
                    )
                }

                // Handle already owned
                if (purchaseState is PurchaseState.AlreadyOwned) {
                    _uiState.value = _uiState.value.copy(
                        showAlreadyOwnedMessage = true
                    )
                }
            }.collect {}
        }
    }

    fun purchasePremium(activity: Activity, plan: PremiumPlan) {
        billingManager.launchPurchaseFlow(activity, plan)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
        _uiState.value = _uiState.value.copy(
            showRestoreMessage = "Checking for previous purchases..."
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            showSuccessMessage = false,
            showAlreadyOwnedMessage = false,
            showRestoreMessage = null
        )
    }

    fun updateProductIds(monthlyId: String, yearlyId: String, lifetimeId: String) {
        billingManager.updateProductIds(monthlyId, yearlyId, lifetimeId)
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
    }
}

data class PremiumUiState(
    val isLoading: Boolean = false,
    val isPremium: Boolean = false,
    val errorMessage: String? = null,
    val showSuccessMessage: Boolean = false,
    val showAlreadyOwnedMessage: Boolean = false,
    val showRestoreMessage: String? = null,
    val isConnectionError: Boolean = false
)