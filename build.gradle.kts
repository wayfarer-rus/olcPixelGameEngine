plugins {
    kotlin("multiplatform") version "1.3.50"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://dl.bintray.com/dominaezzz/kotlin-native")
    }
}

kotlin {
    // Determine host preset.
    val hostOs = System.getProperty("os.name")

    // Create target for the host platform.
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64("olcGameEnginePort")
        hostOs == "Linux" -> linuxX64("olcGameEnginePort")
        hostOs.startsWith("Windows") -> mingwX64("olcGameEnginePort")
        else -> throw GradleException("Host OS '$hostOs' is not supported in Kotlin/Native $project.")
    }

    sourceSets {
        val nativeMain by creating {
            kotlin.srcDir("src")
            resources.srcDir("res")
            dependencies {
                implementation("com.kgl:kgl-glfw:0.1.8-dev-1")
                implementation("com.kgl:kgl-opengl:0.1.8-dev-1")
            }
        }

        hostTarget.compilations.all {
            defaultSourceSet {
                dependsOn(nativeMain)
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
        }
    }

    hostTarget.apply {
        binaries {
            executable("olcGameEnginePortSampleApp") {
                entryPoint = "sample.main"
                runTask?.args("")
            }
            executable("FireworksDemo") {
                entryPoint = "demos.fireworks.main"
                runTask?.args("")
            }
            executable("AsteroidsDemo") {
                entryPoint = "demo.asteroids.main"
                runTask?.args("")
            }
            executable("BreackoutDemo") {
                entryPoint = "demos.breakout.main"
                runTask
            }
            // in progress
            executable("PixelShooterGame") {
                entryPoint = "game.pixel_shooter.main"
                runTask
            }
        }
    }
}
