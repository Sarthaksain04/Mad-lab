plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cscorner.stories"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.cscorner.stories"
        minSdk = 21
        //noinspection ExpiredTargetSdkVersion
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.gson)

    // OkHttp for network requests
    implementation(libs.okhttp)

    // Play Services (if needed for authentication or ML features in the future)
    implementation(libs.play.services.auth)

    // AndroidX Profile Installer
    implementation(libs.profileinstaller)

    // AndroidX Startup
    implementation(libs.startup.runtime)

    // AndroidX Core Runtime
    implementation(libs.core.runtime)

    // AndroidX Annotation Experimental
    implementation(libs.annotation.experimental)
}