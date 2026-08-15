plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }
    namespace = "com.bearguard.mobile"
    // matt/2026-08-15: minSdk 30 (Android 11) deliberately -- AccessibilityService.takeScreenshot()
    // needs API 30+. Both MuMu (API 35) and the ordered Galaxy Tab A16 are well above this, so no
    // MediaProjection fallback path is needed; every capture goes through the AccessibilityService
    // API, which never shows a repeating "start recording?" permission dialog the way
    // MediaProjection does.
    compileSdk = 35

    defaultConfig {
        applicationId = "com.bearguard.mobile"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")

    // matt/2026-08-15: first ported routine (Get Giftcodes) needs HTTP -- java.net.http.HttpClient
    // isn't available below API 34, and minSdk here is 30, so OkHttp instead of raising minSdk.
    // JSON parsing uses the built-in org.json (both response shapes here are trivial -- a "codes"
    // string array and a "msg" field) rather than pulling in a whole JSON library for that.
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // matt/2026-08-15: second foundational piece -- almost every remaining module needs to read
    // text off the game screen. tess4j (Bearguard-Win's Tesseract binding) is desktop-only; ML Kit
    // Text Recognition is the standard on-device Android equivalent (bundled model, no network
    // call per read, unlike a cloud OCR API).
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
