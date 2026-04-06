package game.vermis.data.rooms

import kotlinx.cinterop.refTo
import kotlinx.cinterop.toKString
import platform.posix.F_OK
import platform.posix.access
import platform.posix.fclose
import platform.posix.fgets
import platform.posix.fopen

const val RESOURCES_ROOT_PATH = "/Users/andrei.efimov/IdeaProjects/my/olcPixelGameEngine/games/vermis/build/bin/macosArm64/VermisDemoDebugExecutable"

fun roomLayoutToString(pathToFile: String): String {
  val realPathToFile = "$RESOURCES_ROOT_PATH/$pathToFile"
  if (!fileExists(realPathToFile)) {
    throw IllegalArgumentException("File not found: $realPathToFile")
  }
  return try {
    val file = fopen(realPathToFile, "r") ?: throw IllegalStateException("Could not open file: $realPathToFile")
    val content = StringBuilder()
    val buffer = ByteArray(1024)
    while (fgets(buffer.refTo(0), buffer.size, file) != null) {
      content.append(buffer.toKString())
    }
    fclose(file)
    content.toString()
  } catch (e: Exception) {
    throw RuntimeException("Error reading file: $realPathToFile", e)
  }
}

private fun fileExists(path: String): Boolean = access(path, F_OK) == 0
