pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven {
            name = "jitpack.io"
            url = uri("https://jitpack.io")
        }
    }
}

rootProject.name = "Dhizuku"
include("app", "hidden-api")
