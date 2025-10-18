import org.jetbrains.kotlin.konan.target.Family

plugins {
    kotlin("multiplatform")
}

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

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
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
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
        compilations["main"].cinterops {
            val glfwHeaderDirs: List<Any> = when (konanTarget.family) {
                Family.OSX -> listOf("/opt/local/include", "/usr/local/include")
                Family.LINUX -> listOf("/usr/include", "/usr/include/x86_64-linux-gnu")
                Family.MINGW -> listOf(mingwPath.resolve("include"))
                else -> emptyList()
            }

            val libglfw3 by creating {
                glfwHeaderDirs.forEach { includeDirs.headerFilterOnly(it) }
            }
        }
    }
}
