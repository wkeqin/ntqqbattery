import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.ksp)
}

android {
    namespace = "com.wkeqin.ntqqbattery"
    compileSdk = 37
    androidResources.additionalParameters += listOf("--allow-reserved-package-id", "--package-id", "0x64")

    defaultConfig {
        applicationId = "com.wkeqin.ntqqbattery"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.7.4"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    val props = Properties()
    val propsFile = rootProject.file("local.properties")
    if (propsFile.exists()) {
        props.load(propsFile.inputStream())
    }

    signingConfigs {
        create("release") {
            storeFile = file("release.jks")
            storePassword = props.getProperty("signing.storePassword") ?: ""
            keyAlias = props.getProperty("signing.keyAlias") ?: ""
            keyPassword = props.getProperty("signing.keyPassword") ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }

    lint {
        checkReleaseBuilds = false
    }
}

dependencies {
    compileOnly(libs.xposed.api)
    implementation(libs.yukihookapi)
    ksp(libs.yukihookapi.ksp.xposed)
    implementation(libs.kavaref.core)
    implementation(libs.kavaref.android)
    implementation(libs.kavaref.extension)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}
