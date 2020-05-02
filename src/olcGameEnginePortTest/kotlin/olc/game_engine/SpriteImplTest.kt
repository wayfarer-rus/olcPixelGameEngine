package olc.game_engine

import kotlinx.cinterop.CPointer
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalUnsignedTypes
class SpriteImplTest {

    @Test
    fun testReadSpriteFile() {
        val fh: CPointer<FILE>? =
            fopen("src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", "r")
        assertNotNull(fh)

        try {
            val result = fh.fileToByteArray()
            println(":::collected result = " + result)
            assertTrue(result.isNotEmpty())
        } finally {
            fclose(fh)
        }
    }

    @Test
    fun testLoadFromPGESprFile() {
        val sprite = Sprite()
        val res =
            sprite.loadFromPGESprFile("src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", null)
        assertEquals(RetCode.OK, res)
    }

    @Test
    fun testSaveToPGESprFile() {
        val sprite = Sprite()
        var res =
            sprite.loadFromPGESprFile("src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", null)
        assertEquals(RetCode.OK, res)

        res = sprite.saveToPGESprFile("src/olcGameEnginePortTest/resources/tmp_copy.spr")
        assertEquals(RetCode.OK, res)
    }
}