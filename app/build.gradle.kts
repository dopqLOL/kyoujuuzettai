import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.contentful_javasilver"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.contentful_javasilver"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // local.properties を読み込む
        // local.properties ファイルからプロパティを読み込む
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localPropertiesFile.inputStream().use { stream ->
                localProperties.load(stream)
            }
        }

        // APIキーを BuildConfig に追加（defaultConfig 内）
        buildConfigField("String", "CONTENTFUL_API_KEY", "\"${localProperties.getProperty("CONTENTFUL_API_KEY")}\"")
        buildConfigField("String", "CONTENTFUL_SPACE_ID", "\"${localProperties.getProperty("CONTENTFUL_SPACE_ID")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.contentful.java:java-sdk:10.5.21")
    implementation ("com.google.android.material:material:1.8.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation(libs.core.ktx)


    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}