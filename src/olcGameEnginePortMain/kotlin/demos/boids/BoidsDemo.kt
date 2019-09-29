package demos.boids

import geometry_2d.Point
import geometry_2d.Shape
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.PI
import kotlin.random.Random

@ExperimentalUnsignedTypes
class BoidsDemo : PixelGameEngineImpl() {
    override val appName = "BoidsDemo"

    override fun onUserCreate(): Boolean {
        boids = mutableListOf()

        for (i in 0..maxBoids)
            boids.add(
                Boid(
                    Point(
                        Random.nextDouble(0.0, getDrawTargetWidth().toDouble()).toFloat(),
                        Random.nextDouble(0.0, getDrawTargetHeight().toDouble()).toFloat()
                    ),
                    Random.nextDouble(2 * PI).toFloat()
                )
            )

        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        val mousePos = Point(getMouseX(), getMouseY())

        // movement strategy
        boids.forEach { current ->
            // calculate distance to other boids
            val nearbyBoids = boids
                // filter self
                .filter { it != current }
                // calculate distance to all other boids
                .map {
                    Pair(it, Point.distance(it.pos, current.pos))
                }
                // filter boids that too far away
                .filter {
                    it.second <= Boid.visibilityRadius
                }

            val tooClose = nearbyBoids.filter { it.second < Boid.distanceThreshold }
            val notTooClose = nearbyBoids.filter { it.second >= Boid.distanceThreshold }.filter {
                val areClockwise: (v1: Point, v2: Point) -> Boolean = { v1, v2 ->
                    -v1.x * v2.y + v1.y * v2.x > 0
                }

                val v = it.first.pos - current.pos

                !areClockwise(current.sightZoneStart(), v) &&
                        areClockwise(current.sightZoneEnd(), v)
            }

            // for those that are too close we have to change direction - avoidance maneuver
            current.velocityVector += tooClose.map {
                (current.pos - it.first.pos).toLength(((Boid.distanceThreshold - it.second) / 2f).toFloat())
            }.foldRight(Point(0, 0)) { point, acc -> acc + point }

            // for those that are close enough but not too much
            // steer to each other
            current.velocityVector += notTooClose.map {
                (it.first.pos - current.pos).toLength(Boid.poll)
            }.foldRight(Point(0, 0)) { point, acc -> acc + point }

            // steer towards mouse cursor pos
            current.velocityVector += (mousePos - current.pos).toLength(Boid.poll)
        }

        // draw
        clear()

        boids.forEach {
            drawTriangle(it[0], it[1], it[2], Pixel.GREEN)
//            drawCircle(it.pos.x.toInt(), it.pos.y.toInt(), Boid.distanceThreshold.toInt())

            it.move(elapsedTime)
            it.pos = wrapCoordinates(it.pos)
        }

        fillCircle(getMouseX(), getMouseY(), 1)

        return true
    }

    fun drawTriangle(point1: Point, point2: Point, point3: Point, p: Pixel) {
//        println("$point1; $point2; $point3")
        super.drawTriangle(point1.toPair(), point2.toPair(), point3.toPair(), p)
    }

    private fun wrapCoordinates(coord: Point): Point {
        var (ox, oy) = coord
        val (ix, iy) = coord

        if (ix < 0.0f) ox = ix + getDrawTargetWidth()
        if (ix >= getDrawTargetWidth()) ox = ix - getDrawTargetWidth()

        if (iy < 0.0f) oy = iy + getDrawTargetHeight()
        if (iy >= getDrawTargetHeight()) oy = iy - getDrawTargetHeight()

        return Point(ox, oy)
    }

    override fun draw(x: Int, y: Int, p: Pixel) {
        val (nx, ny) = wrapCoordinates(Point(x, y))
        super.draw(nx.toInt(), ny.toInt(), p)
    }

    private lateinit var boids: MutableList<Boid>

    companion object {
        const val maxBoids = 50
    }
}

class Boid(
    var pos: Point,
    initialAngle: Float
) {
    var velocityVector: Point = Point.pointTo(initialAngle, speed)

    fun move(elapsedTime: Float) {
        // move
        pos += velocityVector * elapsedTime

        // adjust shape to current velocity and pos
        shape = initialShape
            .rotate(velocityVector.angle() + (PI / 2).toFloat())
            .translate(pos)

        // control speed
        velocityVector -= velocityVector.toLength((elapsedTime * (velocityVector.length() - speed) / 0.2f).toFloat())
    }

    operator fun get(i: Int) = shape[i].roundToInt()

    fun sightZoneEnd(): Point {
        val sectorStart = (velocityVector.angle() + (PI * 2 / 3)).toFloat()
        return Point.pointTo(sectorStart, visibilityRadius)
    }

    fun sightZoneStart(): Point {
        val sectorStart = (velocityVector.angle() - (PI * 2 / 3)).toFloat()
        return Point.pointTo(sectorStart, visibilityRadius)
    }

    private var shape = initialShape

    companion object {
        const val visibilityRadius = 40f
        const val distanceThreshold = 15f
        const val poll = 0.1f
        const val speed = 50f
        val initialShape = Shape(
            Point(0.0f, -5.0f),
            Point(-2.5f, +2.5f),
            Point(+2.5f, +2.5f)
        )
    }
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = BoidsDemo()
    if (demo.construct() == rcode.OK) demo.start()
}