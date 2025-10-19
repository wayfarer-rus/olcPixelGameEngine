package olc.game_engine

import kotlin.test.Test
import kotlin.test.assertTrue

class DemoModulePluginSetupTest {

    @Test
    fun `configures executable and default dependencies`() {
        TestProjectBuilder.fromFixture("minimal-demo").use { project ->
            val osFamily = System.getProperty("os.name")
            val configurationName = when {
                osFamily == "Mac OS X" -> "macosX64MainImplementation"
                osFamily == "Linux" -> "linuxX64MainImplementation"
                osFamily.startsWith("Windows") -> "mingwX64MainImplementation"
                else -> "nativeMainImplementation"
            }
            val dependenciesResult = project.build(
                ":demos:minimal:dependencies",
                "--configuration",
                configurationName
            )
            assertTrue(
                actual = dependenciesResult.output.contains("project :engine") ||
                        dependenciesResult.output.contains("project engine"),
                message = "Expected engine dependency in $configurationName, but output was:\\n${dependenciesResult.output}"
            )
            assertTrue(
                actual = dependenciesResult.output.contains("project :demos:shared-assets") ||
                        dependenciesResult.output.contains("project shared-assets"),
                message = "Expected shared assets dependency in $configurationName, but output was:\\n${dependenciesResult.output}"
            )
        }
    }
}
