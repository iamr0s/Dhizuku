import java.io.FileInputStream
import java.util.*

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    id("kotlin-kapt")
}

val keystoreProps = Properties().apply {
    load(FileInputStream(rootProject.file("keystore/r0s.properties")))
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = 33

    defaultConfig {
        // 你如果根据InstallerX的源码进行打包成apk或其他安装包格式
        // 请换一个applicationId，不要和官方的任何发布版本产生冲突。
        // If you use InstallerX source code, package it into apk or other installation package format
        // Please change the applicationId to one that does not conflict with any official release.
        applicationId = "com.rosan.dhizuku"
        minSdk = 21
        targetSdk = 33
        versionCode = 2
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        getByName("debug") {
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
            storeFile = file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            enableV1Signing = true
            enableV2Signing = true
        }

        create("release") {
            keyAlias = keystoreProps.getProperty("keyAlias")
            keyPassword = keystoreProps.getProperty("keyPassword")
            storeFile = file(keystoreProps.getProperty("storeFile"))
            storePassword = keystoreProps.getProperty("storePassword")
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        jvmToolchain(11)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
//        kotlinCompilerExtensionVersion = libs.versions.compose.get()
        kotlinCompilerExtensionVersion = "1.4.4"
    }

    packagingOptions {
        resources {
            excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        }
    }
}

dependencies {
    implementation(project(":dhizuku-aidl"))
    implementation(project(":dhizuku-shared"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.uiToolingPreview)

    implementation(libs.compose.navigation)
    implementation(libs.compose.materialIcons)

    implementation(libs.room.runtime)
    kapt(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.accompanist)
    implementation(libs.accompanist.navigationAnimation)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.xxpermissions)

    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)
}