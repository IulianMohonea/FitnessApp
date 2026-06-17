plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.fitnessapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fitnessapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.foundation:foundation:1.10.4")
    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.ui:ui:1.10.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.10.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.10.4")
}
