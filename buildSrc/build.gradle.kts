plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        create("license") {
            id = "license"
            implementationClass = "com.rosan.dhizuku.license.LicensePlugin"
        }
    }
}
