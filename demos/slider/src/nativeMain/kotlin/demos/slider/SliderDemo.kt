package demos.slider

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode

@ExperimentalUnsignedTypes
class SliderDemo : PixelGameEngineImpl() {
    private val slider: Slider = Slider(this, initialPosition = 0.3f)
    private val slider2: Slider = Slider(this, orientation = Slider.Orientation.VERTICAL, initialPosition = 0.3f)

    override fun onUserCreate(): Boolean {
        println("OnUserCreate() triggered")
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel.DARK_BLUE)

        val mouseKeysState = (0..4).map { ind ->
            ind to getMouseKey(ind)
        }

        val pressedKeys = mouseKeysState.filter { it.second.bPressed }.map { it.first }
        val heldKeys = mouseKeysState.filter { it.second.bHeld }.map { it.first }
        val releasedKeys = mouseKeysState.filter { it.second.bReleased }.map { it.first }
        val mouseKyesMessage = """
            Mouse keys
            are pressed: $pressedKeys
            are held: $heldKeys
            released: $releasedKeys
        """.trimIndent()

        drawString(10, 1, "Mouse: [${getMouseX()}; ${getMouseY()}]\nWheel: ${getMouseWheel()}", Pixel.DARK_GREEN)
        drawString(10, 20, mouseKyesMessage, Pixel.YELLOW)

        // draw a slider
        slider.draw(10, screenHeight() / 2, Pixel.WHITE)
        drawString(10, screenHeight() / 2 - 10, "${slider.position}", Pixel.WHITE)
        // draw second slider
        slider2.draw(screenWidth() - 20, screenHeight() / 2 - 50, Pixel.CYAN)
        drawString(screenWidth() - 20 - 10, screenHeight() / 2 - 50 - 10, "${slider2.position}", Pixel.CYAN)

        drawCircle(getMouseX(), getMouseY(), 1)
        return true
    }

    override val appName: String = "Example"
}


@ExperimentalUnsignedTypes
fun main(args: Array<String>) {
    val demo = SliderDemo()
    if (demo.construct(256, 200, 4, 4) == RetCode.OK)
        demo.start()
}