import org.jetbrains.kotlin.konan.properties.loadProperties
import java.util.Properties

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.kotlin)
    id("kotlin-kapt")
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
}

val keystoreDir = "$rootDir/keystore"

val keystoreProps = Properties()
for (name in arrayOf("r0s.properties", "debug.properties")) {
    val f = file("$keystoreDir/$name")
    if (!f.exists()) continue
    keystoreProps.load(f.inputStream())
    break
}

android {
    namespace = "com.rosan.dhizuku"

    compileSdk = 35

    defaultConfig {
        minSdk = 21
        targetSdk = compileSdk
        ndkVersion = "27.2.12479018"

        val versionProps = loadProperties("$rootDir/version.properties")
        versionCode = versionProps.getProperty("versionCode").toInt()
        versionName = versionProps.getProperty("versionName")

        vectorDrawables {
            useSupportLibrary = true
        }
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }

    signingConfigs {
        val keyAlias = keystoreProps.getProperty("keyAlias")
        val keyPassword = keystoreProps.getProperty("keyPassword")
        val storeFile = file("$keystoreDir/${keystoreProps.getProperty("storeFile")}")
        val storePassword = keystoreProps.getProperty("storePassword")
        getByName("debug") {
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
            this.storeFile = storeFile
            this.storePassword = storePassword
            enableV1Signing = true
            enableV2Signing = true
        }
        create("release") {
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
            this.storeFile = storeFile
            this.storePassword = storePassword
            enableV1Signing = true
            enableV2Signing = true
            enableV3Signing = true
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlin {
        jvmToolchain(21)
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    androidResources {
        @Suppress("UnstableApiUsage")
        generateLocaleConfig = true
    }

    packaging {
        //jniLibs.keepDebugSymbols.add("lib/*/libandroidx.graphics.path.so")
        resources.excludes.addAll(arrayOf("META-INF/**", "DebugProbesKt.bin", "kotlin-tooling-metadata.json", "Kotlin/**"))
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    compileOnly(project(":hidden-api"))

    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.activity.compose)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling)
    implementation(libs.compose.navigation)
    implementation(libs.compose.materialIcons)
    implementation(libs.compose.material3)

    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)

    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.navigationAnimation)
    implementation(libs.accompanist.flowlayout)
    implementation(libs.accompanist.drawablepainter)
    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.lsposed.hiddenapibypass)

    implementation(libs.rikka.shizuku.api)
    implementation(libs.rikka.shizuku.provider)

    implementation(libs.iamr0s.dhizuku.api)
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(arrayOf("-Xlint:deprecation", "-Xlint:unchecked"))
}
