import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.3.0-Beta1" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    plugins.withId("org.jetbrains.kotlin.multiplatform") {
        afterEvaluate {
            val kotlinExt = extensions.findByName("kotlin") as? KotlinMultiplatformExtension
                ?: return@afterEvaluate

            val resourceRoots = listOf("src/nativeMain/resources", "src/commonMain/resources")
                .map { project.layout.projectDirectory.dir(it).asFile }
                .filter { it.exists() }

            if (resourceRoots.isEmpty()) return@afterEvaluate

            kotlinExt.targets.withType(KotlinNativeTarget::class.java).forEach { target ->
                target.binaries.forEach { binary ->
                    if (binary !is Executable) return@forEach

                    val outputResourcesDir = binary.outputFile.parentFile.resolve("resources")
                    val copyTaskName =
                        "copy${project.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}${binary.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}Resources"
                    val copyTaskProvider = tasks.register<Sync>(copyTaskName) {
                        from(resourceRoots)
                        into(outputResourcesDir)
                    }

                    binary.linkTaskProvider.configure { finalizedBy(copyTaskProvider) }
                    val runTaskName = binary.runTaskName ?: return@forEach
                    tasks.named(runTaskName).configure { dependsOn(copyTaskProvider) }
                }
            }
        }
    }
}
