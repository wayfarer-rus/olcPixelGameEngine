package olc.game_engine

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class PixelTests {

    @Test
    fun testPixel() {
        val p = Pixel()
        assertEquals(0xFF000000.toUInt(), p.n)
    }

    @Test
    fun testPixel2() {
        val p = Pixel(255u, 255u, 255u)
        assertEquals(0xFFFFFFFF.toUInt(), p.n)
    }

    @Test
    fun testPixel3() {
        val p = Pixel(1u, 2u, 3u)
        assertEquals(1u, p.r)
        assertEquals(2u, p.g)
        assertEquals(3u, p.b)
    }
}