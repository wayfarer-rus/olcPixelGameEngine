package demos.fireworks

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode

@ExperimentalUnsignedTypes
class Firework(
    var x: Int,
    var y: Int,
    var p: Pixel,
    val drawTargetWidth: Int,
    val drawTargetHeight: Int
) {
    val visible
        inline get() = y.inBounds(0, drawTargetHeight + 100) && x.inBounds(
            -(drawTargetWidth / 2 + 50),
            drawTargetWidth / 2 + 50
        )

    // translate coordinates
    val tx: Int
        inline get() = this.drawTargetWidth / 2 - x

    val ty: Int
        inline get() = this.drawTargetHeight - y
}

fun Int.inBounds(begin: Int, end: Int) = this in begin..end

@ExperimentalUnsignedTypes
class FireworksDemo : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel.BLACK)

        drawString(5, 5, firesList.size.toString())

        if (getMouseKey(1).bPressed) {
            // fire a dot
            firesList.add(Firework(0, 0, Pixel.WHITE, getDrawTargetWidth(), getDrawTargetHeight()))
        }

        firesList.forEach {
            draw(it.tx, it.ty, it.p)
            // move
            it.y++
        }

        firesList.removeAll { !it.visible }

        return true
    }

    private var firesList: MutableList<Firework> = mutableListOf()
    override val appName = "Fireworks Demo"
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = FireworksDemo()

    if (demo.construct() == rcode.OK)
        demo.start()
}