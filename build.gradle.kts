plugins {
    kotlin("multiplatform") version "1.4.10"
}

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/dominaezzz/kotlin-native")
    }
    maven {
        url = uri("https://kotlin.bintray.com/kotlinx")
    }
}

val kglVersion = "0.1.10"

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
                implementation("com.kgl:kgl-glfw:$kglVersion")
                implementation("com.kgl:kgl-glfw-static:$kglVersion")
                implementation("com.kgl:kgl-opengl:$kglVersion")
                implementation("com.kgl:kgl-stb:$kglVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:1.3.6")
            }
        }

        hostTarget.apply {
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
            executable("olcDungeonWarpingApp") {
                entryPoint = "sample.mainOlcDungeon"
                runTask?.workingDir("src/olcGameEnginePortMain/resources")
            }
            executable("FireworksDemo") {
                entryPoint = "demos.fireworks.main"
                runTask?.args("")
            }
            executable("AsteroidsDemo") {
                entryPoint = "demos.asteroids.main"
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
            executable("MandelbrotDemo") {
                entryPoint = "demos.mandelbrot.main"
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
