plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.3" apply false
    id("app.cash.sqldelight") version "2.1.0" apply false
//    id("kotlin-parcelize")
//    alias(libs.plugins.kotlinxSerialization)
//    alias(libs.plugins.sqldelight)
}