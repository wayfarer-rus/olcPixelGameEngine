package demo.asteroids

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin


open class SpaceObject(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var vx: Float = 0.0f,
    var vy: Float = 0.0f,
    open var angle: Float = 0.0f,
    val size: Int = 1
) {
    val yi: Int
        inline get() = y.roundToInt()
    val xi: Int
        inline get() = x.roundToInt()

    fun move(elapsedTime: Float) {
        x += vx * elapsedTime
        y += vy * elapsedTime
    }
}

class Ship : SpaceObject {
    constructor(shipModel: Triple<Pair<Float, Float>, Pair<Float, Float>, Pair<Float, Float>>) : super() {
        this.shipModel = shipModel.toList()
    }

    operator fun get(i: Int) = translate(shipModel[i]).roundToInt()

    private var shipModel: List<Pair<Float, Float>>
    override var angle: Float = 0.0f
        set(newAngle) {
            field = newAngle%(2*PI.toFloat())
            rotate()
        }

    private fun rotate() {
        shipModel = shipModel.map {
            Pair(
                it.first*cos(angle) - it.second*sin(angle),
                it.first*sin(angle) + it.second*cos(angle)
            )
        }
    }

    private fun translate(coord: Pair<Float, Float>) = Pair(coord.first + x, coord.second + y)

}

@ExperimentalUnsignedTypes
class AsteroidsDemo : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {

        player.x = getDrawTargetWidth() / 2f
        player.y = getDrawTargetHeight() / 2f

        asteroids.add(SpaceObject(vx = 8.0f, vy = -10.0f, size = 8))
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel(0xFF4c4a41u)) // space color

        asteroids.forEach {
            // move
            it.move(elapsedTime)
            val (nx, ny) = wrapFloatCoordinates(Pair(it.x, it.y))
            it.x = nx; it.y = ny
            // draw
            drawString(it.xi, it.yi, scale = it.size, text = "H", col = Pixel.YELLOW)
        }

        with(player) {
            if (getKey(Key.SPACE).bPressed || getKey(Key.SPACE).bHeld) {
                // shoot
            }

            if (getKey(Key.UP).bHeld) {
                // thrust
            }

            if (getKey(Key.LEFT).bPressed || getKey(Key.LEFT).bHeld) {
                // rotate left
                this.angle -= 1.0f * elapsedTime
            }

            if (getKey(Key.RIGHT).bPressed || getKey(Key.RIGHT).bHeld) {
                this.angle += 1.0f * elapsedTime
            }

            drawTriangle(this[0], this[1], this[2])
        }
        return true
    }

    private fun wrapFloatCoordinates(coord: Pair<Float, Float>): Pair<Float, Float> {
        var (ox, oy) = coord
        val (ix, iy) = coord
        if (ix < 0.0f) ox = ix + getDrawTargetWidth()
        if (ix >= getDrawTargetWidth()) ox = ix - getDrawTargetWidth()
        if (iy < 0.0f) oy = iy + getDrawTargetHeight()
        if (iy >= getDrawTargetHeight()) oy = iy - getDrawTargetHeight()
        return Pair(ox, oy)
    }

    private fun wrapIntCoordinates(coord: Pair<Int, Int>): Pair<Int, Int> {
        var (ox, oy) = coord
        val (ix, iy) = coord
        if (ix < 0) ox = ix + getDrawTargetWidth()
        if (ix >= getDrawTargetWidth()) ox = ix - getDrawTargetWidth()
        if (iy < 0) oy = iy + getDrawTargetHeight()
        if (iy >= getDrawTargetHeight()) oy = iy - getDrawTargetHeight()
        return Pair(ox, oy)
    }

    override fun draw(x: Int, y: Int, p: Pixel) {
        val (nx, ny) = wrapIntCoordinates(Pair(x, y))
        super.draw(nx, ny, p)
    }

    private var asteroids: MutableList<SpaceObject> = mutableListOf()
    private val player = Ship(Triple(Pair(0.0f, -5.0f), Pair(-2.5f, +2.5f), Pair(+2.5f, +2.5f)))
    override val appName = "Asteroids Demo"
}

fun Pair<Float, Float>.roundToInt() = Pair(this.first.roundToInt(), this.second.roundToInt())

@ExperimentalUnsignedTypes
fun main() {
    val demo = AsteroidsDemo()
    if (demo.construct() == rcode.OK)
        demo.start()
}