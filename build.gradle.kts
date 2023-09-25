plugins {
    kotlin("multiplatform") version "1.9.10"
    id("org.jetbrains.compose")
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64("native") {
        binaries.framework {
            baseName = "logodemo"
            isStatic = true
        }
    }
}

tasks.wrapper {
    gradleVersion = "8.1.1"
    distributionType = Wrapper.DistributionType.ALL
}
