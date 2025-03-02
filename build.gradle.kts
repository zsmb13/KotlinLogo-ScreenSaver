import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.1.10"
}

repositories {
    mavenCentral()
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

    sourceSets {
        macosMain.dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
