package sample

import kotlinx.cinterop.CPointer
import olc.game_engine.SpriteImpl
import olc.game_engine.fileToByteArray
import olc.game_engine.rcode
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
            fopen("../../../../src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", "r")
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
        val sprite = SpriteImpl()
        val res =
            sprite.LoadFromPGESprFile("../../../../src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", null)
        assertEquals(rcode.OK, res)
    }

    @Test
    fun testSaveToPGESprFile() {
        val sprite = SpriteImpl()
        var res =
            sprite.LoadFromPGESprFile("../../../../src/olcGameEnginePortTest/resources/SeditSlimeTransparent.spr", null)
        assertEquals(rcode.OK, res)

        res = sprite.SaveToPGESprFile("../../../../src/olcGameEnginePortTest/resources/tmp_copy.spr")
        assertEquals(rcode.OK, res)
    }
}