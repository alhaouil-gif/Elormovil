plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    kotlin("kapt")
}

android {
    namespace = "com.example.elormovil"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.elormovil"
        minSdk = 36
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Retrofit y Gson Converter
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)
    implementation(libs.play.services.maps)
    implementation(libs.androidx.preference.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


    implementation(libs.gif.drawable)

    implementation(libs.glide)
    kapt(libs.glide.compiler)

    implementation(libs.okhttp3)
    implementation(libs.okhttp3.logging.interceptor)

    implementation(libs.androidx.recyclerview)


    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)


    implementation(libs.gifdrawable)

    implementation(libs.osmdroid.android)

}
