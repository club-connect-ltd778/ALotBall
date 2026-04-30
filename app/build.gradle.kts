plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "uklot.connectionltd.alotbot"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "uklot.connectionltd.alotbot"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "1.2.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            val keystoreFile = project.findProperty("RELEASE_STORE_FILE") as String?
            val keystorePassword = project.findProperty("RELEASE_STORE_PASSWORD") as String?
            val releaseKeyAlias = project.findProperty("RELEASE_KEY_ALIAS") as String?
            val releaseKeyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as String?
    
            println("=== Signing Config Debug ===")
            println("RELEASE_STORE_FILE: ${if (keystoreFile != null) "SET (${keystoreFile})" else "NULL"}")
            println("RELEASE_STORE_PASSWORD: ${if (keystorePassword != null) "SET" else "NULL"}")
            println("RELEASE_KEY_ALIAS: ${if (releaseKeyAlias != null) "SET ($releaseKeyAlias)" else "NULL"}")
            println("RELEASE_KEY_PASSWORD: ${if (releaseKeyPassword != null) "SET" else "NULL"}")
    
            if (keystoreFile != null && keystorePassword != null && releaseKeyAlias != null && releaseKeyPassword != null) {
                val keystoreFileObj = file(keystoreFile)
                if (keystoreFileObj.exists()) {
                    storeFile = keystoreFileObj
                    storePassword = keystorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                    println("Signing config applied successfully")
                } else {
                    println("ERROR: Keystore file not found: $keystoreFile")
                }
            } else {
                println("WARNING: Release signing config not set. Check RELEASE_* properties.")
            }
        }
    }
    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
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
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)
    implementation(libs.gson)
    implementation(libs.coil)
    implementation(libs.lottie)
    implementation(libs.androidx.browser)
    implementation(libs.onesignal)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
