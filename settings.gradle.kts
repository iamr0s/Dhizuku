pluginManagement {
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        gradlePluginPortal()
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        maven { setUrl("https://maven.aliyun.com/repository/public/") }
//        maven { setUrl("https://jitpack.io") }
        google()
        mavenCentral()
        maven { setUrl("https://maven.scijava.org/content/repositories/public/") }
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
