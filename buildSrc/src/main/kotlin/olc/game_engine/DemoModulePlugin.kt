package olc.game_engine

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.Executable
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import java.nio.file.Files

private const val ENGINE_PROJECT_PATH = ":engine"
private const val SHARED_ASSETS_PROJECT_PATH = ":demos:shared-assets"
private const val DEFAULT_KOTLIN_VERSION = "2.3.0-Beta1"

class DemoModulePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val extension = target.extensions.create(
            "demoModule",
            DemoModuleExtension::class.java
        )

        target.plugins.apply("org.jetbrains.kotlin.multiplatform")

        val kotlin = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
        kotlin.applyDefaultHierarchyTemplate()
        val hostTarget = configureHostTarget(kotlin)
        initialiseBinary(hostTarget)

        target.afterEvaluate {
            configureDemoModule(this, extension, kotlin, hostTarget)
        }
    }

    private fun configureDemoModule(
        project: Project,
        extension: DemoModuleExtension,
        kotlin: KotlinMultiplatformExtension,
        hostTarget: KotlinNativeTarget
    ) {
        val applicationName = extension.applicationNameProperty().orNull?.trim().orEmpty()
        val entryPoint = extension.entryPointProperty().orNull?.trim().orEmpty()

        validateInputs(project, applicationName, entryPoint)
        registerApplicationName(project, applicationName)
        configureSourceSets(project, kotlin, extension, hostTarget)
        finalizeBinary(project, hostTarget, applicationName, entryPoint)
    }

    private fun configureHostTarget(kotlin: KotlinMultiplatformExtension): KotlinNativeTarget {
        val hostOs = System.getProperty("os.name")
        val factoryMethod = when {
            hostOs == "Mac OS X" -> "macosX64"
            hostOs == "Linux" -> "linuxX64"
            hostOs.startsWith("Windows") -> "mingwX64"
            else -> error("Host OS '$hostOs' is not supported for Kotlin/Native.")
        }
        val method = kotlin::class.java.methods.firstOrNull { candidate ->
            candidate.name == factoryMethod && candidate.parameterCount == 0
        }
            ?: error("Kotlin Multiplatform extension does not expose function '$factoryMethod'. Update the plugin or Kotlin version.")

        val target = method.invoke(kotlin) as? KotlinNativeTarget
            ?: error("Failed to create host Kotlin/Native target via '$factoryMethod'.")
        return target
    }

    private fun initialiseBinary(hostTarget: KotlinNativeTarget) {
        hostTarget.binaries.executable()
    }

    private fun configureSourceSets(
        project: Project,
        kotlin: KotlinMultiplatformExtension,
        extension: DemoModuleExtension,
        hostTarget: KotlinNativeTarget
    ) {
        val engineDependency = project.dependencies.project(mapOf("path" to ENGINE_PROJECT_PATH))
        val sharedAssetsDependency =
            project.dependencies.project(mapOf("path" to SHARED_ASSETS_PROJECT_PATH))
        val kotlinVersion =
            kotlin.coreLibrariesVersion ?: project.findProperty("kotlin.version")?.toString()
            ?: DEFAULT_KOTLIN_VERSION

        kotlin.sourceSets.all {
            languageSettings.enableLanguageFeature("InlineClasses")
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
        }

        val additionalDependencies = extension.dependencies.getOrElse(emptyList())
        val resourceDirs = extension.resourceDirs.getOrElse(emptyList())

        val hostMain = hostTarget.compilations.getByName("main").defaultSourceSet
        hostMain.resources.srcDir(project.layout.projectDirectory.dir("src/nativeMain/resources"))
        resourceDirs.forEach { dir ->
            hostMain.resources.srcDir(project.layout.projectDirectory.dir(dir))
        }
        hostMain.dependencies {
            implementation(engineDependency)
            implementation(sharedAssetsDependency)
            additionalDependencies.forEach { notation ->
                implementation(project.dependencies.create(notation))
            }
        }

        hostTarget.compilations.findByName("test")?.defaultSourceSet?.dependencies {
            implementation(project.dependencies.create("org.jetbrains.kotlin:kotlin-test:$kotlinVersion"))
        }
    }

    private fun finalizeBinary(
        project: Project,
        hostTarget: KotlinNativeTarget,
        applicationName: String,
        entryPoint: String
    ) {
        hostTarget.binaries.withType(Executable::class.java).configureEach {
            baseName = applicationName
            this.entryPoint = entryPoint
            val linkTaskProvider = project.tasks.named(linkTaskName)
            project.tasks.named("assemble").configure {
                dependsOn(linkTaskProvider)
            }
        }
    }

    private fun validateInputs(project: Project, applicationName: String, entryPoint: String) {
        if (applicationName.isBlank()) {
            throw GradleException("Demo module applicationName must not be blank for project '${project.path}'.")
        }
        if (entryPoint.isBlank()) {
            throw GradleException("Demo module entryPoint must not be blank for project '${project.path}'.")
        }
        val separatorIndex = entryPoint.lastIndexOf('.')
        if (separatorIndex <= 0 || separatorIndex == entryPoint.length - 1) {
            throw GradleException("Entry point '$entryPoint' must include a package and function name (e.g., demos.foo.main).")
        }

        val packagePath = entryPoint.substring(0, separatorIndex).replace('.', '/')
        val functionName = entryPoint.substring(separatorIndex + 1)
        val sourceRoot = project.layout.projectDirectory.dir("src/nativeMain/kotlin").asFile.toPath()
        val packageDir = sourceRoot.resolve(packagePath)
        if (!Files.exists(packageDir)) {
            throw GradleException("Entry point '$entryPoint' could not be resolved: directory '$packagePath' is missing under src/nativeMain/kotlin.")
        }

        val hasFunction = Files.walk(packageDir).use { paths ->
            paths.filter { Files.isRegularFile(it) && it.toString().endsWith(".kt") }
                .anyMatch { file ->
                    Files.readString(file).contains("fun $functionName(")
                }
        }

        if (!hasFunction) {
            throw GradleException("Entry point '$entryPoint' could not be resolved: function '$functionName' not found within package '$packagePath'.")
        }
    }

    private fun registerApplicationName(project: Project, applicationName: String) {
        val extras: ExtraPropertiesExtension =
            project.gradle.rootProject.extensions.extraProperties
        val registry = if (extras.has(NAME_REGISTRY_KEY)) {
            @Suppress("UNCHECKED_CAST")
            extras.get(NAME_REGISTRY_KEY) as MutableMap<String, String>
        } else {
            mutableMapOf<String, String>().also {
                extras.set(NAME_REGISTRY_KEY, it)
            }
        }

        val existing = registry[applicationName]
        if (existing != null && existing != project.path) {
            throw GradleException(
                "Duplicate demo application name '$applicationName' detected. Already used by project '$existing'."
            )
        }

        registry[applicationName] = project.path
    }

    companion object {
        private const val NAME_REGISTRY_KEY = "olc.game_engine.demo.registry"
    }
}
