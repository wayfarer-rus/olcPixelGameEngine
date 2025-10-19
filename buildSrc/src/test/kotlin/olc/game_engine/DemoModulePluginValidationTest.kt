package olc.game_engine

import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertTrue

class DemoModulePluginValidationTest {

    @Test
    fun `fails when application name is blank`() {
        TestProjectBuilder.fromFixture("invalid-demo").use { project ->
            project.taskOutputPath("demos/invalid/build.gradle.kts").writeText(
                """
                plugins {
                    id("olc.game_engine.demo")
                }

                demoModule {
                    applicationName = ""
                    entryPoint = "demos.invalid.main"
                }
                """.trimIndent()
            )

            val result = project.buildAndFail(":demos:invalid:tasks")
            assertTrue(
                actual = result.output.contains("applicationName must not be blank"),
                message = "Expected blank applicationName validation message, but output was:\\n${result.output}"
            )
        }
    }

    @Test
    fun `fails when entry point is blank`() {
        TestProjectBuilder.fromFixture("invalid-demo").use { project ->
            project.taskOutputPath("demos/invalid/build.gradle.kts").writeText(
                """
                plugins {
                    id("olc.game_engine.demo")
                }

                demoModule {
                    applicationName = "InvalidDemo"
                    entryPoint = ""
                }
                """.trimIndent()
            )

            val result = project.buildAndFail(":demos:invalid:tasks")
            assertTrue(
                actual = result.output.contains("entryPoint must not be blank"),
                message = "Expected blank entryPoint validation message, but output was:\\n${result.output}"
            )
        }
    }

    @Test
    fun `fails when entry point source cannot be resolved`() {
        TestProjectBuilder.fromFixture("invalid-demo").use { project ->
            project.taskOutputPath("demos/invalid/build.gradle.kts").writeText(
                """
                plugins {
                    id("olc.game_engine.demo")
                }

                demoModule {
                    applicationName = "InvalidDemo"
                    entryPoint = "demos.invalid.missingMain"
                }
                """.trimIndent()
            )

            val result = project.buildAndFail(":demos:invalid:tasks")
            assertTrue(
                actual = result.output.contains("Entry point 'demos.invalid.missingMain' could not be resolved"),
                message = "Expected missing entry point validation message, but output was:\\n${result.output}"
            )
        }
    }

    @Test
    fun `fails when duplicate application names are detected`() {
        TestProjectBuilder.fromFixture("invalid-demo").use { project ->
            val duplicateScript = """
                plugins {
                    id("olc.game_engine.demo")
                }

                demoModule {
                    applicationName = "DuplicateDemo"
                    entryPoint = "%s"
                }
            """.trimIndent()

            project.taskOutputPath("demos/duplicate_one/build.gradle.kts").writeText(
                duplicateScript.format("demos.duplicateone.main")
            )
            project.taskOutputPath("demos/duplicate_two/build.gradle.kts").writeText(
                duplicateScript.format("demos.duplicatetwo.main")
            )

            val result = project.buildAndFail(":demos:duplicate_one:tasks")
            assertTrue(
                actual = result.output.contains("Duplicate demo application name 'DuplicateDemo'"),
                message = "Expected duplicate application name validation message, but output was:\\n${result.output}"
            )
        }
    }
}
