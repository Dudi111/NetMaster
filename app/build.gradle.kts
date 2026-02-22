plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias (libs.plugins.hiltPlugin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
}

val appVersion: String by project

android {
    namespace = "com.smartnet.analyzer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.smartnet.analyzer"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = appVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        buildConfig = true
        compose = true
    }
}

dependencies {

    implementation(libs.google.accompanist)
    //dagger hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    //navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation)

    //material
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    //core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    // test cases
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    debugImplementation(libs.androidx.ui.tooling)

    // vico charts
    implementation(libs.vico.compose)
    implementation(libs.vico.core)
    implementation(libs.vico.compose.m3)
    //firebase
    implementation(platform(libs.google.firebase))
    implementation(libs.firebase.storage)
    implementation(libs.firebase.analytics)

    // Retrofit
    implementation(libs.squareup.retrofit2)
    implementation(libs.com.squareup.okhttp3)

    //Custom log
    implementation(libs.logfeast)
}