plugins {
    kotlin("multiplatform")
}

kotlin {
    applyDefaultHierarchyTemplate()

    val hostOs = System.getProperty("os.name")
    val hostTarget = when {
        hostOs == "Mac OS X" -> macosX64()
        hostOs == "Linux" -> linuxX64()
        hostOs.startsWith("Windows") -> mingwX64()
        else -> error("Host OS '$hostOs' is not supported for Kotlin/Native.")
    }

    sourceSets {
        val nativeMain by getting {
            dependencies {
                implementation(project(":engine"))
            }
        }
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }
    }

    hostTarget.apply {
        binaries {
            executable("BallsDemo") {
                entryPoint = "demos.balls.main"
            }
        }
    }
}
