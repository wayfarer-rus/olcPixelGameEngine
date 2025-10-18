import org.jetbrains.kotlin.konan.target.Family

plugins {
    kotlin("multiplatform") version "2.3.0-Beta1"
}

repositories {
    mavenCentral()
}

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

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
        val olcGameEnginePortMain by getting {
        }

        hostTarget.apply {
            val glfwHeaderDirs: List<Any> = when (konanTarget.family) {
                Family.OSX -> listOf("/opt/local/include", "/usr/local/include")
                Family.LINUX -> listOf("/usr/include", "/usr/include/x86_64-linux-gnu")
                Family.MINGW -> listOf(mingwPath.resolve("include"))
                else -> emptyList()
            }

            compilations["main"].cinterops {
                val libglfw3 by creating {
                    glfwHeaderDirs.forEach { includeDirs.headerFilterOnly(it) }
                }
            }
        }

        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
        }
    }

    hostTarget.apply {
        binaries {
            executable("olcGameEnginePortSampleApp") {
                entryPoint = "sample.main"
                runTaskProvider?.configure { args("") }
            }
            executable("FireworksDemo") {
                entryPoint = "demos.fireworks.main"
                runTaskProvider?.configure { args("") }
            }
            executable("AsteroidsDemo") {
                entryPoint = "demos.asteroids.main"
                runTaskProvider?.configure { args("") }
            }
            executable("BreackoutDemo") {
                entryPoint = "demos.breakout.main"
            }
            executable("BoidsDemo") {
                entryPoint = "demos.boids.main"
            }
            executable("DestructibleBlockDemo") {
                entryPoint = "demos.destructible_sprite.main"
            }
            executable("BallsDemo") {
                entryPoint = "demos.balls.main"
            }
            // in progress
            executable("PixelShooterGame") {
                entryPoint = "game.pixel_shooter.main"
            }
        }
    }
}
