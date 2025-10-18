package olc.game_engine

import kotlin.test.Test

@ExperimentalUnsignedTypes
class PixelGameEngineTest {

    private val pge = object : PixelGameEngineImpl() {
        override fun onUserCreate(): Boolean {
            return true
        }

        override fun onUserUpdate(elapsedTime: Float): Boolean {
            return false
        }

    }.also { it.construct(100, 100, 1, 1) }

    @Test
    fun testClear() {
        pge.clear(Pixel.WHITE)
    }

    @Test
    fun testDraw() {
        // draw 10 000 pixels
        for (x in 0 until pge.screenWidth())
            for (y in 0 until pge.screenHeight())
                pge.draw(x, y)
    }

    @Test
    fun testDrawCircle() {
        // draw 50 circles
        for (r in 0 until pge.screenHeight() / 2)
            pge.drawCircle(pge.screenWidth() / 2, pge.screenHeight() / 2, r)
    }

    @Test
    fun testFillCircle() {
        // draw 50 circles
        for (r in 0 until pge.screenHeight() / 2)
            pge.fillCircle(pge.screenWidth() / 2, pge.screenHeight() / 2, r)
    }

    @Test
    fun testDrawLine() {
        // draw 100 lines
        for (i in 0 until pge.screenWidth())
            pge.drawLine(0 to i, i to 0, Pixel.BLACK)
    }

    @Test
    fun testDrawRect() {
        // draw 100 rectangles
        for (i in 0 until pge.screenWidth())
            pge.drawRect(0, 0, i, i)
    }

    @Test
    fun testFillRect() {
        // draw 100 rectangles
        for (i in 0 until pge.screenWidth())
            pge.fillRect(0, 0, i, i, Pixel.YELLOW)
    }

    @Test
    fun testDrawTriangle() {
        // draw 100 triangles
        for (i in 0 until pge.screenWidth())
            pge.drawTriangle(
                pge.screenWidth() / 2 to 0,
                pge.screenWidth() to i,
                pge.screenHeight() - i to pge.screenHeight()
            )
    }

    @Test
    fun testFillTriangle() {
        // draw 100 triangles
        for (i in 0 until pge.screenWidth())
            pge.fillTriangle(
                pge.screenWidth() / 2 to 0,
                pge.screenWidth() to i,
                pge.screenHeight() - i to pge.screenHeight()
            )
    }
}