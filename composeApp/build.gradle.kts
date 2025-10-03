import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.util.Properties


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose   )
            implementation(libs.sqldelight.android)
            implementation(libs.androidx.compose.ui.text.android)


            // Ktor
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.jmdns)
            implementation(libs.ktor.server.cio.android)
            implementation(compose.ui)

            // Voyager
            implementation(libs.voyager.hilt)
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
            implementation(compose.material)
            implementation(compose.materialIconsExtended)


            // Ktor
            implementation(libs.ktor.client.core) // Already here
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation) // Already here
            implementation(libs.ktor.serialization.kotlinx.json) // Already here
            implementation(libs.ktor.logging)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.content.negotiation)

            // Web Driver (selenium)
            implementation(libs.seleniumhq.selenium)
            implementation(libs.jsoup)

            // Resources
            implementation(compose.components.resources)

            //Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.kodein)
            implementation(libs.voyager.tabNavigator)

        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jvm)
            implementation(libs.jmdns)

            // Ktor
            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.netty)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.server.cio.jvm)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.logging)
        }

    }

    sqldelight {
        databases {
            create("LLData") {
                packageName = "com.db"
                generateAsync = false
                version = 1.0
            }
        }
    }
}

android {
    namespace = "org.asv.looplink"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "org.asv.looplink"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val props = Properties()
        props.load(project.rootProject.file("local.properties").reader())
        val ocrKey = props.getProperty("ocrSpaceAPIKEY")
        buildConfigField("String", "ocrSpaceAPIKEY", "\"$ocrKey\"")
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
}

dependencies {
    implementation(libs.core.ktx)
    debugImplementation(compose.uiTooling)
}

compose.resources {
    publicResClass = true
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "org.asv.looplink.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Looplink"
            packageVersion = "1.0.6"
            windows {
                iconFile.set(project.file("D:\\Work\\College\\Projects\\Log Chat\\LoopLink\\LoopLink\\composeApp\\src\\commonMain\\composeResources\\drawable\\icon.ico"))
                includeAllModules = true
            }
        }
    }
}

repositories {
    google()
    mavenCentral()
}

