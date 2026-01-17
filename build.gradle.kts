import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    val xcf = XCFramework("KotlinLogo")

    applyDefaultHierarchyTemplate()

    val targets = listOf(macosX64(), macosArm64())
    targets.forEach { target ->
        target.binaries.framework {
            binaryOption("bundleId", "co.zsmb.KotlinLogos")
            baseName = "KotlinLogo"
            isStatic = true
            xcf.add(this)
        }
    }

    androidTarget()

    jvm {
        binaries {
            executable {
                mainClass.set("MainKt")
                val folder: Any? = properties["customfolder"]
                if (folder is String) {
                    applicationDefaultJvmArgs = listOf("-Dcustomfolder=$folder")
                }
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)

            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.coroutines.core)
        }

        val nonAndroidMain by creating {
            dependsOn(commonMain.get())
        }
        jvmMain.get().dependsOn(nonAndroidMain)
        appleMain.get().dependsOn(nonAndroidMain)

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        androidMain.dependencies {
            implementation(libs.mozartwallpapers)
        }
        nativeMain.dependencies {
            implementation(libs.kotlin.reflect)
        }
    }

    targets.forEach {
        it.binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}

android {
    namespace = "co.zsmb.kotlinlogos"
    compileSdk = 36

    defaultConfig {
        applicationId = "co.zsmb.kotlinlogos"
        targetSdk = 36
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
