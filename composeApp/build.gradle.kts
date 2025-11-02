import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    id("app.cash.sqldelight") version "2.1.0"
    id("kotlin-parcelize")
}

kotlin {

    targets.all {
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions{
                    freeCompilerArgs.add("-Xexpect-actual-classes")
                }
            }
        }
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_15)
        }
    }



    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose   )
            implementation(libs.sqldelight.android)
            implementation(libs.androidx.compose.ui.text.android)
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.coil.compose)


            // Ktor
            implementation(libs.ktor.client.android)
            implementation(libs.ktor.client.cio)
            implementation(libs.jmdns)
            implementation(libs.ktor.server.cio.android)
            implementation(compose.ui)

            // Voyager
            implementation(libs.voyager.hilt)

            //Media
            implementation(libs.androidx.media3.exoplayer)
            implementation(libs.androidx.media3.ui)
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
            implementation(libs.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.sqldelight.coroutines)
            implementation(compose.material)
            implementation(compose.materialIconsExtended)

            // Ktor
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.websockets)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.logging)

            implementation(libs.ktor.server.core)
            implementation(libs.ktor.server.websockets)
            implementation(libs.ktor.server.content.negotiation)

            // Web Driver (selenium)
            implementation(libs.seleniumhq.selenium)
            implementation(libs.jsoup)

            // Resources
            implementation(compose.components.resources)
            implementation(libs.coil3.coil.compose)


            //Voyager
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.transitions)
            implementation(libs.voyager.screenModel)
            implementation(libs.voyager.kodein)
            implementation(libs.voyager.tabNavigator)

            //Media
            implementation(libs.compose.media.player)
            implementation(libs.qdsfdhvh.image.loader)
            implementation(libs.compose.connectivity.monitor)
            implementation(libs.sdp.ssp.compose.multiplatform)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit4)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jvm)
            implementation(libs.jmdns)
            implementation(libs.slf4j.simple)

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

            //Media
//            implementation(libs.compose.media.player)
        }

    }

    sqldelight {
        databases {
            create("LLData") {
                packageName = "com.db"
                generateAsync = false
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
        sourceCompatibility = JavaVersion.VERSION_15
        targetCompatibility = JavaVersion.VERSION_15
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.qdsfdhvh.image.loader)
    implementation(libs.sdp.ssp.compose.multiplatform)
    implementation(libs.gluegen.rt)
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
        buildTypes.release.proguard {
            configurationFiles.from("compose-desktop.pro")
        }
    }
}

//repositories {
//    google()
//    mavenCentral()
//}
