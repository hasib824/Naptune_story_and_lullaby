// build.gradle.kts (Module Level)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp) // Using KSP instead of KAPT for better performance
    id("kotlin-parcelize")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.naptune.lullabyandstory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.naptune.lullabyandstory"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Add supported locales to reduce APK size (using newer API)
        resourceConfigurations += listOf("en", "es", "fr", "de", "pt", "hi", "ar")

        // 🆕 FCM Server Configuration (BuildConfig fields)
        buildConfigField("String", "FCM_SERVER_URL", "\"https://notifier.appswave.xyz/\"")
        buildConfigField("String", "FCM_APP_SECRET", "\"ABCDEFGHIJ\"")
        buildConfigField("String", "FCM_APP_REF", "\"APP#fe0b4e1ee9ee5e20\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // AppCompat for runtime language switching
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material Components (required for AppCompatActivity themes)
    implementation("com.google.android.material:material:1.12.0")

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Compose Navigation
    implementation(libs.navigation.compose)
    implementation(libs.androidx.navigation.compose.v293)

    // ViewModel
    implementation(libs.lifecycle.viewmodel.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.foundation.layout)
    implementation(libs.androidx.foundation)
    ksp(libs.hilt.compiler) // Using KSP instead of kapt

    // Room Database - Using your TOML bundle
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler) // Using KSP for Room compiler

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Appwrite
    implementation(libs.sdk.appwrite)

    // Image Loading (Coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)
    implementation(libs.coil.video)


    // For bottom shett Scaffold
    /*implementation(libs.compose.material)   // Material2
    implementation(libs.compose.material3)  // Material3*/

    // System UI Controller
    implementation(libs.accompanist.systemuicontroller)

    // Constraint Layout for Compose
    implementation(libs.constraintlayout.compose)

    // Downloader
    implementation(libs.prdownloader)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // ExoPlayer Bundle for audio playback
    implementation(libs.bundles.exoplayer)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Timer wheelpicker
   // implementation(libs.wheelpicker)
    
    // AdMob
    implementation(libs.play.services.ads)
    
    // Google Play Billing
    implementation(libs.billing.ktx)

    // Firebase Analytics KTX (latest stable version)
    implementation("com.google.firebase:firebase-analytics-ktx:22.1.2")

    // 🆕 Firebase Cloud Messaging KTX (latest stable version)
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.2")

    // 🆕 Retrofit 3.0.0 (Kotlin rewrite with native coroutine support)
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // 🆕 OkHttp 4.12.0 (compatible with Retrofit 3.0.0)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // ✅ Coil already included via coil-compose (lines 122-126)
    // No need to add io.coil-kt:coil:2.7.0 separately

    // 🆕 WorkManager 2.11.0 (reliable background token upload)
    implementation("androidx.work:work-runtime-ktx:2.11.0")

    // 🆕 Hilt Work 1.2.0 (WorkManager + Hilt integration)
    // Note: Annotation processing handled by ksp(libs.hilt.compiler) at line 107
    implementation("androidx.hilt:hilt-work:1.2.0")
}

// KSP Configuration for Room
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}