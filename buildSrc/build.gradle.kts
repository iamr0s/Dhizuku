plugins {
    `kotlin-dsl`
}

repositories {
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
