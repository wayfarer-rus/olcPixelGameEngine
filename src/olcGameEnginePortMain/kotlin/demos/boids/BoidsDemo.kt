package demos.boids

import demos.asteroids.roundToInt
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@ExperimentalUnsignedTypes
class BoidsDemo : PixelGameEngineImpl() {
    override val appName = "BoidsDemo"

    override fun onUserCreate(): Boolean {
        boids = mutableListOf()

        for (i in 0..maxBoids)
            boids.add(
                Boid(
                    Random.nextDouble(0.0, getDrawTargetWidth().toDouble()).toFloat(),
                    Random.nextDouble(0.0, getDrawTargetHeight().toDouble()).toFloat()
                ).also { it.angle = Random.nextDouble(2* PI).toFloat() }
            )

        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear()

        // avoidance strategy
        boids.forEach {

        }

        // draw
        boids.forEach {
            drawTriangle(it[0], it[1], it[2], Pixel.GREEN)
            it.move(elapsedTime)
            val (nx, ny) = wrapFloatCoordinates(Pair(it.x, it.y))
            it.x = nx; it.y = ny
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

    private lateinit var boids: MutableList<Boid>;

    companion object {
        const val maxBoids = 30
    }
}

class Boid(
    var x: Float,
    var y: Float,
    private var vx: Float = speed,
    private var vy: Float = speed
) {
    fun move(elapsedTime: Float) {
        x += vx * elapsedTime
        y += vy * elapsedTime
    }

    operator fun get(i: Int) = translate(shipModel[i]).roundToInt()

    private var shipModel: List<Pair<Float, Float>> = shape.toList()

    var angle: Float = 0.0f
        set(newAngle) {
            rotate(newAngle - field)
            field = newAngle

            vx *= sin(newAngle)
            vy *= -cos(newAngle)
        }

    private fun rotate(deltaAngle: Float) {
        shipModel = shipModel.map {
            Pair(
                it.first * cos(deltaAngle) - it.second * sin(deltaAngle),
                it.first * sin(deltaAngle) + it.second * cos(deltaAngle)
            )
        }
    }

    private fun translate(coord: Pair<Float, Float>) = Pair(coord.first + x, coord.second + y)

    companion object {
        val speed = 30f
        val shape = Triple(Pair(0.0f, -5.0f), Pair(-2.5f, +2.5f), Pair(+2.5f, +2.5f))
    }
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = BoidsDemo()
    if (demo.construct() == rcode.OK) demo.start()
}