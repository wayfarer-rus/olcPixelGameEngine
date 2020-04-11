plugins {
    kotlin("multiplatform") version "1.3.71"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/dominaezzz/kotlin-native")
    }
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
        val nativeMain by creating {
            dependencies {
                implementation("com.kgl:kgl-glfw:0.1.9-dev-8")
                implementation("com.kgl:kgl-opengl:0.1.9-dev-8")
            }
        }

        hostTarget.apply {
            compilations["main"].cinterops {
                val libglfw3 by creating {
                    when (preset) {
                        presets["macosX64"] -> includeDirs.headerFilterOnly("/opt/local/include", "/usr/local/include")
                        presets["linuxX64"] -> includeDirs.headerFilterOnly(
                            "/usr/include",
                            "/usr/include/x86_64-linux-gnu"
                        )
                        presets["mingwX64"] -> includeDirs.headerFilterOnly(mingwPath.resolve("include"))
                    }
                }
            }

            compilations.all {
                defaultSourceSet {
                    dependsOn(nativeMain)
                }
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
            executable("BoidsDemo") {
                entryPoint = "demos.boids.main"
                runTask
            }
            executable("DestructibleBlockDemo") {
                entryPoint = "demos.destructible_sprite.main"
                runTask
            }
            executable("BallsDemo") {
                entryPoint = "demos.balls.main"
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
