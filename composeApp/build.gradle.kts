import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.WASM
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
//    id("app.cash.sqldelight") version "2.1.0"
//    alias(libs.plugins.kotlinAndroid)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    jvm()
    
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("composeApp")
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(rootDirPath)
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.sqldelight.android)

            // Ktor
            implementation(libs.ktor.client.android)
            implementation(libs.jmdns)
            implementation(libs.ktor.server.core)

        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.runtime) // SQLDelight runtime
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core) // Koin for DI
            implementation(libs.sqldelight.coroutines)

            // Ktor
            implementation(libs.ktor.client.core) // Already here
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation) // Already here
            implementation(libs.ktor.serialization.kotlinx.json) // Already here
            implementation(libs.ktor.logging)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jvm)

            // Ktor
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }

        wasmJsMain.dependencies {
            // ... wasmJs dependencies ...
            // If you need Ktor client for WasmJS (you likely will for consistency)
            // implementation(libs.ktor.client.js) // You'd need to add 'ktor-client-js' to toml
            // Make sure to add the content negotiation and serialization as well if needed.
        }

    }

    sqldelight{
        databases{
            create("LLData"){
                packageName = "com.db"
                generateAsync = false
            }
        }
    }
}

android {
    namespace = "org.asv.looplink"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.asv.looplink"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
//    kotlinOptions {
//        jvmTarget = "21"
//    }
}

dependencies {
    implementation(libs.core.ktx)
    debugImplementation(compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "org.asv.looplink.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.asv.looplink"
            packageVersion = "1.0.0"
        }
    }
}

repositories{
    google()
    mavenCentral()
}

