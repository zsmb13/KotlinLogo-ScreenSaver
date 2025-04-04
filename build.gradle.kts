import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    val xcf = XCFramework("KotlinLogo")

    val targets = listOf(macosX64(), macosArm64())
    targets.forEach { target ->
        target.binaries.framework {
            binaryOption("bundleId", "co.zsmb.KotlinLogos")
            baseName = "KotlinLogo"
            isStatic = true
            xcf.add(this)
        }
    }

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
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
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
