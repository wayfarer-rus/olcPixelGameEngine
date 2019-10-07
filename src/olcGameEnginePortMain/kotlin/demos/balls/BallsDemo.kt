package demos.balls

import geometry_2d.Point
import olc.game_engine.Key
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.pow

@ExperimentalUnsignedTypes
class BallsDemo : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {
        reset()
        offset = Point(
            getDrawTargetWidth() / 2 - body.w * R + R,
            getDrawTargetHeight() / 2 - body.h * R + R
        )
        return true
    }

    private fun reset() {
        body = Body(8, 8, 64, R)
        projectiles.clear()
        splats.clear()
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        val gunPos = Point(0, getDrawTargetHeight() / 2)

        if (getMouseKey(1).bPressed) {
            val p = Projectile(gunPos, (Point(getMouseX(), getMouseY()) - gunPos).toLength(speed), 1, speed)
            projectiles.add(p)
        }

        if (getKey(Key.R).bPressed) {
            reset()
        }

        // draw
        clear()

        var collision: List<Pair<Projectile, Point>> = emptyList()

        projectiles.forEach {
            it.move(elapsedTime)
            drawCircle(it.pos.xi, it.pos.yi, R)

            collision = body.parts.filter { part ->
                val p = offset + part
                Point.distance(it.pos, p) <= 2 * R
            }.mapNotNull { part ->
                val p = offset + part
                val u = it.mass * it.speed / (it.mass + body.partMass)
                val k = body.partMass * u.pow(2) / 2

                println("u = $u; K = $k")
                it.collided = true

                if (k > energyThreshold) {
                    val result = Pair(Projectile(p, (p - it.pos).toLength(u), body.partMass, u), part)
//                    it.pos = Point(-100, -100)
                    it.v = (it.pos - p).toLength(it.speed)
                    result
                } else if (k > 1.0) {
                    it.v = (it.pos - p).toLength(it.speed)
                    null
                } else null
            }
        }

        if (collision.isNotEmpty()) {
            body.parts.removeAll(collision.map { it.second })
            projectiles.addAll(collision.map { it.first.collided = true; it.first })
        }

        splats.forEach {
            it.move(elapsedTime)
            drawCircle(it.pos.xi, it.pos.yi, R)
        }

        splats.removeAll { it.v.x < 1.0 || it.v.y < 1.0 }

        projectiles.removeAll { it.pos.x < 0 || it.pos.y < 0 || it.pos.x > getDrawTargetWidth() || it.pos.y > getDrawTargetHeight() }

        body.parts.forEach {
            val p = offset + it
            drawCircle(p.xi, p.yi, R)
        }

        val gunEndPoint = (Point(getMouseX(), getMouseY()) - gunPos).toLength(10f) + gunPos
        drawLine(gunPos.toPair(), gunEndPoint.toPair())

        fillCircle(getMouseX(), getMouseY(), 1)
        return true
    }

    private val R = 1
    private lateinit var body: Body
    private var offset = Point(0, 0)
    private val speed = 400f
    private val energyThreshold = 75f
    private val projectiles: MutableList<Projectile> = mutableListOf()
    private val splats: MutableList<Projectile> = mutableListOf()
}

class Projectile(var pos: Point, var v: Point, val mass: Int, var speed: Float) {
    var collided = false
    private var lifetime = 1.0f
    fun move(elapsedTime: Float) {
        if (collided) lifetime -= elapsedTime

        if (lifetime < 0.0) pos = Point(-100, -100)
        else pos += v * elapsedTime * lifetime
    }
}

class Body(val w: Int, val h: Int, val mass: Int, radius: Int) {
    val parts: MutableList<Point> = mutableListOf()
    val partMass = mass / (w * h)

    init {
        val R = radius
        for (j in 0 until h) {
            for (i in 0 until w) {
                val p = Point(i * 2 * R, j * 2 * R)
                this.parts.add(p)
            }
        }
    }
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = BallsDemo()
    if (demo.construct() == rcode.OK) demo.start()
}