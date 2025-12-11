plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.gametrack"
    compileSdk = 34

    signingConfigs {
        create("release") {
            storeFile = file("../keystore/gametrack.jks")  // ✅ CORRECTO  // ← ruta a tu .jks
            storePassword = "GameTrack2025"        // ← contraseña del keystore
            keyAlias = "gametrack"                     // ← alias que usaste
            keyPassword = "GameTrack2025"            // ← contraseña de la clave
        }
    }

    defaultConfig {
        applicationId = "com.example.gametrack"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17  // ⬅️ CAMBIA a 17
        targetCompatibility = JavaVersion.VERSION_17  // ⬅️ CAMBIA a 17
    }

    kotlinOptions {
        jvmTarget = "17"  // ⬅️ CAMBIA a 17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"  // ⬅️ VERSIÓN CORRECTA
    }

    packaging {
        resources.excludes.add("META-INF/*")
    }
}

dependencies {
    // ⚠️ VERSIONES COMPATIBLES - NO MEZCLES VERSIONES

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.0")

    // ⭐ COMPOSE BOM (Controla todas las versiones)
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation Compose (compatible con BOM)
    implementation("androidx.navigation:navigation-compose:2.7.5")

    implementation("at.favre.lib:bcrypt:0.10.2")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.0")
    implementation("androidx.room:room-ktx:2.6.0")
    kapt("androidx.room:room-compiler:2.6.0")

    implementation("com.google.code.gson:gson:2.10.1")
    

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil para imágenes
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Lottie (opcional - si lo quieres mantener)
    implementation("com.airbnb.android:lottie-compose:6.1.0")
}