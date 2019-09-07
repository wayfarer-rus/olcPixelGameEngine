package sample

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import platform.posix.rand

@ExperimentalUnsignedTypes
class Example : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {
        println("OnUserCreate() triggered")
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel.DARK_BLUE)
        for (x in 0 until screenWidth())
            for (y in screenHeight() / 2 until screenHeight())
                draw(x, y, Pixel((rand() % 255), (rand() % 255), (rand() % 255)))

        val pressedKeys = Key.values().map { Pair(it, getKey(it)) }.filter { it.second.bPressed || it.second.bHeld }
            .joinToString { it.first.toString() }

        drawString(10, 1, "Focus: [${isFocused()}; ${isMouseInWindow()}]", Pixel.GREEN)
        drawString(10, 10, "Mouse: [${getMouseX()}; ${getMouseY()}]\nWheel: ${getMouseWheel()}", Pixel.DARK_GREEN)
        drawString(10, 30, "These keys\nare pressed:\n$pressedKeys", Pixel.YELLOW)

        drawCircle(getMouseX(), getMouseY(), 1)
        return true
    }

    override val appName: String = "Example"
}


@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val demo = Example()
    if (demo.construct(256, 200, 4, 4) == rcode.OK)
        demo.start()
}