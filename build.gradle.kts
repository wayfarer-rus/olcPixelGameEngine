plugins {
    kotlin("multiplatform") version "1.3.31"
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
                implementation("com.kgl:kgl-glfw:0.1.5")
                implementation("com.kgl:kgl-opengl:0.1.5")
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
            executable {
                entryPoint = "sample.main"
                runTask?.args("args")
            }
        }
    }
}

