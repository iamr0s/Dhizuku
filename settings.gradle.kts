pluginManagement {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
        maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        mavenLocal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
        maven { setUrl("https://androidx.dev/storage/compose-compiler/repository/") }
    }
}

rootProject.name = "Dhizuku"
include(
    ":app",
    ":hidden-api",
    ":dhizuku-aidl",
    ":dhizuku-shared",
)
project(":dhizuku-aidl").projectDir = file("${rootDir.path}/api/dhizuku-aidl")
project(":dhizuku-shared").projectDir = file("${rootDir.path}/api/dhizuku-shared")
