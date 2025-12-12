package com.naptune.lullabyandstory.utils

object BillingConstants {
    // Default product IDs - These should be replaced with actual IDs from Google Play Console
    const val MONTHLY_SUBSCRIPTION_ID = "monthly_premium_subscription"
    const val YEARLY_SUBSCRIPTION_ID = "yearly_premium_subscription"
    const val LIFETIME_PRODUCT_ID = "lifetime_premium_access"
    
    // Helper function to update product IDs at runtime
    class ProductIdManager {
        companion object {
            @Volatile
            private var monthlyId: String = MONTHLY_SUBSCRIPTION_ID
            @Volatile
            private var yearlyId: String = YEARLY_SUBSCRIPTION_ID
            @Volatile
            private var lifetimeId: String = LIFETIME_PRODUCT_ID
            
            fun updateProductIds(
                monthly: String,
                yearly: String,
                lifetime: String
            ) {
                monthlyId = monthly
                yearlyId = yearly
                lifetimeId = lifetime
            }
            
            fun getMonthlyId(): String = monthlyId
            fun getYearlyId(): String = yearlyId
            fun getLifetimeId(): String = lifetimeId
        }
    }
}

/**
 * Usage Instructions:
 * 
 * 1. Get your product IDs from Google Play Console:
 *    - Go to Google Play Console
 *    - Select your app
 *    - Go to Monetize > Products > In-app products (for lifetime)
 *    - Go to Monetize > Products > Subscriptions (for monthly/yearly)
 * 
 * 2. Replace the default IDs in BillingConstants or use:
 *    BillingConstants.ProductIdManager.updateProductIds(
 *        monthly = "your_monthly_sub_id",
 *        yearly = "your_yearly_sub_id", 
 *        lifetime = "your_lifetime_product_id"
 *    )
 * 
 * 3. Call this in your Application class or before initializing billing
 */