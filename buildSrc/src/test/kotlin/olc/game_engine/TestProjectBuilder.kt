package olc.game_engine

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

object TestProjectBuilder {
    private val fixturesRoot: Path = Path.of("src", "test", "resources", "test-projects")

    fun fromFixture(name: String): GradleTestProject {
        val fixtureDir = fixturesRoot.resolve(name)
        require(fixtureDir.exists() && fixtureDir.isDirectory()) {
            "Fixture '$name' not found under $fixturesRoot"
        }
        val tempDir = Files.createTempDirectory("demo-plugin-test-")
        copyFixture(fixtureDir, tempDir)
        return GradleTestProject(tempDir, fixtureDir)
    }

    @OptIn(ExperimentalPathApi::class)
    private fun copyFixture(source: Path, destination: Path) {
        source.walk().forEach { path ->
            val relative = path.relativeTo(source)
            val target = destination.resolve(relative)
            if (path.isDirectory()) {
                Files.createDirectories(target)
            } else {
                Files.createDirectories(target.parent)
                Files.copy(path, target)
            }
        }
    }
}

class GradleTestProject internal constructor(
    val projectDir: Path,
    private val fixtureDir: Path
) : Closeable {

    fun build(vararg args: String): BuildResult =
        runner(*args).build()

    fun buildAndFail(vararg args: String): BuildResult =
        runner(*args).buildAndFail()

    fun taskOutputPath(vararg segments: String): Path =
        segments.fold(projectDir) { acc, segment -> acc.resolve(segment) }

    fun fixtureName(): String = fixtureDir.name

    private fun runner(vararg args: String): GradleRunner =
        GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withArguments(*args, "--stacktrace")
            .withPluginClasspath()
            .forwardOutput()

    @OptIn(ExperimentalPathApi::class)
    override fun close() {
        projectDir.deleteRecursively()
    }
}
