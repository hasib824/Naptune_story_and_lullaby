package com.naptune.lullabyandstory.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.naptune.lullabyandstory.presentation.premium.PremiumPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context
) : PurchasesUpdatedListener, BillingClientStateListener {

    // ========================================
    // 🧪 TESTING MODE - Toggle by commenting/uncommenting
    // ========================================
    // ✅ Set to true to enable testing mode (bypass real billing)
    // ✅ Set to false for production (use real Google Play billing)
    private val TESTING_MODE_ENABLED = false

    // 🧪 Test purchase states - Set ONLY ONE to true to simulate that purchase type
    // ⚠️ Make sure only ONE is set to true at a time!
    private val TEST_MONTHLY_SUBSCRIPTION = false   // ✅ Set to true to test monthly subscription
    private val TEST_YEARLY_SUBSCRIPTION = false    // ✅ Set to true to test yearly subscription
    private val TEST_LIFETIME_PURCHASE = false      // ✅ Set to true to test lifetime purchase
    private val TEST_NO_PURCHASE = false            // ✅ Set to true to test free user (no premium)

    // ========================================
    // BILLING CLIENT & STATE MANAGEMENT
    // ========================================

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases()
        .build()

    private val _billingConnectionState = MutableStateFlow(BillingConnectionState.DISCONNECTED)
    val billingConnectionState: StateFlow<BillingConnectionState> = _billingConnectionState.asStateFlow()

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Idle)
    val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val _isPurchased = MutableStateFlow(false)
    val isPurchased : StateFlow<Boolean> = _isPurchased.asStateFlow()

    // ✅ NEW: Track current purchase type (for showing correct badge/status)
    private val _currentPurchaseType = MutableStateFlow<PurchaseType>(PurchaseType.None)
    val currentPurchaseType: StateFlow<PurchaseType> = _currentPurchaseType.asStateFlow()

    // Product IDs - These will be replaced with actual IDs from Play Console
    private val monthlySubscriptionId = "monthly_premium_subscription"
    private val yearlySubscriptionId = "yearly_premium_subscription"
    private val lifetimeProductId = "lifetime_premium_access"

    init {
        if (TESTING_MODE_ENABLED) {
            // 🧪 Testing mode: Apply test purchase immediately
            applyTestPurchase()
            Log.w("BillingManager", "🧪 TESTING MODE ENABLED - Using simulated purchases")
        } else {
            // ✅ Production mode: Connect to real Google Play Billing
            connectToBillingService()
            Log.d("BillingManager", "✅ Production mode - Connecting to Google Play Billing")
        }
    }

    private fun connectToBillingService() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    // ========================================
    // 🧪 TESTING FUNCTIONS
    // ========================================

    /**
     * Apply test purchase based on which test flag is enabled
     * This simulates a real purchase without connecting to Google Play
     */
    private fun applyTestPurchase() {
        when {
            TEST_MONTHLY_SUBSCRIPTION -> {
                _isPurchased.value = true
                _currentPurchaseType.value = PurchaseType.MonthlySubscription
                _billingConnectionState.value = BillingConnectionState.CONNECTED
                Log.d("BillingManager", "🧪 Test: Monthly subscription activated")
            }
            TEST_YEARLY_SUBSCRIPTION -> {
                _isPurchased.value = true
                _currentPurchaseType.value = PurchaseType.YearlySubscription
                _billingConnectionState.value = BillingConnectionState.CONNECTED
                Log.d("BillingManager", "🧪 Test: Yearly subscription activated")
            }
            TEST_LIFETIME_PURCHASE -> {
                _isPurchased.value = true
                _currentPurchaseType.value = PurchaseType.Lifetime
                _billingConnectionState.value = BillingConnectionState.CONNECTED
                Log.d("BillingManager", "🧪 Test: Lifetime purchase activated")
            }
            else -> {
                // TEST_NO_PURCHASE or no test flags enabled
                _isPurchased.value = false
                _currentPurchaseType.value = PurchaseType.None
                _billingConnectionState.value = BillingConnectionState.CONNECTED
                Log.d("BillingManager", "🧪 Test: No purchase (free user)")
            }
        }
    }

    /**
     * 🧪 Simulate a test purchase at runtime
     * Call this from UI to test purchase flow without real billing
     */
    fun simulatePurchase(purchaseType: PurchaseType) {
        if (!TESTING_MODE_ENABLED) {
            Log.w("BillingManager", "⚠️ simulatePurchase() called but TESTING_MODE_ENABLED is false")
            return
        }

        _purchaseState.value = PurchaseState.Loading

        // Simulate network delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            when (purchaseType) {
                PurchaseType.MonthlySubscription -> {
                    _isPurchased.value = true
                    _currentPurchaseType.value = PurchaseType.MonthlySubscription
                    _purchaseState.value = PurchaseState.Success
                    Log.d("BillingManager", "🧪 Simulated monthly subscription purchase")
                }
                PurchaseType.YearlySubscription -> {
                    _isPurchased.value = true
                    _currentPurchaseType.value = PurchaseType.YearlySubscription
                    _purchaseState.value = PurchaseState.Success
                    Log.d("BillingManager", "🧪 Simulated yearly subscription purchase")
                }
                PurchaseType.Lifetime -> {
                    _isPurchased.value = true
                    _currentPurchaseType.value = PurchaseType.Lifetime
                    _purchaseState.value = PurchaseState.Success
                    Log.d("BillingManager", "🧪 Simulated lifetime purchase")
                }
                PurchaseType.None -> {
                    _isPurchased.value = false
                    _currentPurchaseType.value = PurchaseType.None
                    _purchaseState.value = PurchaseState.Success
                    Log.d("BillingManager", "🧪 Simulated purchase removal (free user)")
                }
            }
        }, 1500) // Simulate 1.5s delay like real billing
    }

    /**
     * 🧪 Check if we're in testing mode
     */
    fun isTestingMode(): Boolean = TESTING_MODE_ENABLED

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingConnectionState.value = BillingConnectionState.CONNECTED
            queryPurchases()
        } else {
            _billingConnectionState.value = BillingConnectionState.ERROR
        }
    }

    override fun onBillingServiceDisconnected() {
        _billingConnectionState.value = BillingConnectionState.DISCONNECTED
    }

    private fun queryPurchases() {
        // Query subscriptions
        val subscriptionParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(subscriptionParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }

        // Query in-app purchases
        val inAppParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(inAppParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    private fun handlePurchases(purchases: List<Purchase>) {
        var hasValidPremium = false
        var purchaseType = PurchaseType.None

        for (purchase in purchases) {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                when {
                    purchase.products.contains(monthlySubscriptionId) -> {
                        hasValidPremium = true
                        purchaseType = PurchaseType.MonthlySubscription
                        acknowledgePurchase(purchase)
                        Log.d("BillingManager", "✅ Found valid monthly subscription")
                    }
                    purchase.products.contains(yearlySubscriptionId) -> {
                        hasValidPremium = true
                        purchaseType = PurchaseType.YearlySubscription
                        acknowledgePurchase(purchase)
                        Log.d("BillingManager", "✅ Found valid yearly subscription")
                    }
                    purchase.products.contains(lifetimeProductId) -> {
                        hasValidPremium = true
                        purchaseType = PurchaseType.Lifetime
                        acknowledgePurchase(purchase)
                        Log.d("BillingManager", "✅ Found valid lifetime purchase")
                    }
                }
            }
        }
        _isPurchased.value = hasValidPremium
        _currentPurchaseType.value = purchaseType
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Purchase acknowledged successfully
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, premiumPlan: PremiumPlan) {
        // 🧪 If in testing mode, simulate purchase instead of real billing
        if (TESTING_MODE_ENABLED) {
            val purchaseType = when (premiumPlan) {
                PremiumPlan.MONTHLY -> PurchaseType.MonthlySubscription
                PremiumPlan.YEARLY -> PurchaseType.YearlySubscription
                PremiumPlan.LIFETIME -> PurchaseType.Lifetime
            }
            simulatePurchase(purchaseType)
            return
        }

        // ✅ Production mode: Real billing flow
        if (_billingConnectionState.value != BillingConnectionState.CONNECTED) {
            _purchaseState.value = PurchaseState.Error("Billing service not connected")
            return
        }

        val productId = when (premiumPlan) {
            PremiumPlan.MONTHLY -> monthlySubscriptionId
            PremiumPlan.YEARLY -> yearlySubscriptionId
            PremiumPlan.LIFETIME -> lifetimeProductId
        }

        val productType = when (premiumPlan) {
            PremiumPlan.LIFETIME -> BillingClient.ProductType.INAPP
            else -> BillingClient.ProductType.SUBS
        }

        val productDetailsParams = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(productId)
            .setProductType(productType)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(productDetailsParams))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && 
                productDetailsList.isNotEmpty()) {
                
                val productDetails = productDetailsList[0]
                val billingFlowParams = when (premiumPlan) {
                    PremiumPlan.LIFETIME -> {
                        // One-time purchase
                        BillingFlowParams.newBuilder()
                            .setProductDetailsParamsList(
                                listOf(
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(productDetails)
                                        .build()
                                )
                            )
                            .build()
                    }
                    else -> {
                        // Subscription
                        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                        if (offerToken != null) {
                            BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .setOfferToken(offerToken)
                                            .build()
                                    )
                                )
                                .build()
                        } else {
                            _purchaseState.value = PurchaseState.Error("No subscription offer available")
                            return@queryProductDetailsAsync
                        }
                    }
                }

                _purchaseState.value = PurchaseState.Loading
                val response = billingClient.launchBillingFlow(activity, billingFlowParams)
                
                if (response.responseCode != BillingClient.BillingResponseCode.OK) {
                    _purchaseState.value = PurchaseState.Error("Failed to launch purchase flow")
                }
            } else {
                _purchaseState.value = PurchaseState.Error("Product not available")
                Log.e("Billing", "Product not available")
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                purchases?.let { handlePurchases(it) }
                _purchaseState.value = PurchaseState.Success
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _purchaseState.value = PurchaseState.Cancelled
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _purchaseState.value = PurchaseState.AlreadyOwned
                queryPurchases() // Refresh premium status
            }
            else -> {
                _purchaseState.value = PurchaseState.Error("Purchase failed: ${billingResult.debugMessage}")
            }
        }
    }

    fun updateProductIds(monthlyId: String, yearlyId: String, lifetimeId: String) {
        // This function allows updating product IDs from Play Console
        // You can call this method to set the actual product IDs from your Play Store listing
    }

    fun restorePurchases() {
        queryPurchases()
    }

    fun endConnection() {
        billingClient.endConnection()
    }
}

enum class BillingConnectionState {
    DISCONNECTED,
    CONNECTED,
    ERROR
}

sealed class PurchaseState {
    object Idle : PurchaseState()
    object Loading : PurchaseState()
    object Success : PurchaseState()
    object Cancelled : PurchaseState()
    object AlreadyOwned : PurchaseState()
    data class Error(val message: String) : PurchaseState()
}

/**
 * ✅ Represents the type of premium purchase/subscription user has
 * Used for showing correct badge and determining premium features access
 */
enum class PurchaseType {
    None,                   // Free user - shows ads
    MonthlySubscription,    // Monthly subscriber - no ads, PRO badge
    YearlySubscription,     // Yearly subscriber - no ads, PRO badge
    Lifetime                // Lifetime owner - no ads, PRO badge
}