import com.android.build.gradle.AppExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}
apply { from(file("../build-variants.gradle")) }

android {
    namespace = "com.sunmi.tapro.taplink.demo"
    compileSdk = Integer.parseInt(libs.versions.compileSdk.get())

    defaultConfig {
        applicationId = "com.sunmi.tapro.taplink.demo"
        minSdk = Integer.parseInt(libs.versions.minSdk.get())
        targetSdk = Integer.parseInt(libs.versions.compileSdk.get())
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
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
        kotlinCompilerExtensionVersion = "1.5.8"
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    
    // Compose 核心库
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    
    // Android 核心库
    implementation(libs.androidx.core.ktx)
    
    // Kotlin 协程
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation("com.sunmi:taplink-sdk:1.0.0")

    // Compose 调试工具
    debugImplementation(libs.androidx.ui.tooling)
    
    // 测试
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v112)
    androidTestImplementation(libs.androidx.espresso.core.v330)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    implementation(libs.koin.androidx.compose)
    implementation (libs.androidx.navigation.compose)
    implementation (libs.androidx.runtime)
}

