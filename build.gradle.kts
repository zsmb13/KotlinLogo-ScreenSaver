import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.1.10"
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    val xcf = XCFramework("KotlinLogo")

//    val targets = listOf(macosX64(), macosArm64())
    val target = macosArm64()
    target.let { target ->
        target.binaries.framework {
            binaryOption("bundleId", "co.zsmb.KotlinLogos")
            baseName = "KotlinLogo"
            isStatic = true
            xcf.add(this)
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)

            implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")

            implementation(compose.components.resources)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }

    target.apply {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
