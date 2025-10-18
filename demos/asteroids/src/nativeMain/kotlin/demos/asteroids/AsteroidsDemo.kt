package demos.asteroids

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode
import kotlin.math.*
import kotlin.random.Random
import kotlin.test.assertNotNull

open class SpaceObject(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var vx: Float = 0.0f,
    var vy: Float = 0.0f,
    open var angle: Float = 0.0f
) {
    var time = 0.0f
    var dead = false
    val yi: Int
        inline get() = y.roundToInt()
    val xi: Int
        inline get() = x.roundToInt()

    fun accelerate(elapsedTime: Float) {
        vx += sin(angle) * 20.0f * elapsedTime
        vy += -cos(angle) * 20.0f * elapsedTime
    }

    fun move(elapsedTime: Float) {
        x += vx * elapsedTime
        y += vy * elapsedTime
    }
}

@ExperimentalUnsignedTypes
class Asteroid(
    val letter: String,
    val color: Pixel,
    val scale: Int,
    x: Float,
    y: Float,
    vx: Float,
    vy: Float,
    angle: Float = 0.0f
) : SpaceObject(x, y, vx, vy, angle)

class Ship : SpaceObject {
    constructor(shipModel: Triple<Pair<Float, Float>, Pair<Float, Float>, Pair<Float, Float>>) : super() {
        this.shipModel = shipModel.toList()
    }

    operator fun get(i: Int) = translate(shipModel[i]).roundToInt()

    private var shipModel: List<Pair<Float, Float>>

    override var angle: Float = 0.0f
        set(newAngle) {
            rotate(newAngle - field)
            field = newAngle
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
}

@ExperimentalUnsignedTypes
class AsteroidsDemo : PixelGameEngineImpl() {
    override fun onUserCreate() = reset()

    private fun reset(): Boolean {
        bullets.clear()
        asteroids.clear()

        player.x = getDrawTargetWidth() / 2f
        player.y = getDrawTargetHeight() / 2f
        player.dead = false
        player.vx = 0.0f; player.vy = 0.0f
        player.angle = (PI / 2).toFloat()

        asteroids.add(
            Asteroid(
                "H",
                letters["H"]!!,
                8,
                x = 0.0f,
                y = 0.0f,
                vx = Random.nextDouble(-10.0, 10.0).toFloat(),
                vy = Random.nextDouble(-10.0, 10.0).toFloat()
            )
        )

        this.score = "_________"

        return true
    }

    private fun messageInCenter(msg: String, scale: Int, color: Pixel) {
        drawString(
            getDrawTargetWidth() / 2 - (msg.length * 8 * 4) / 2,
            getDrawTargetHeight() / 2 - 8 * 2,
            msg,
            color,
            scale = scale
        )
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel(spaceColor))

        if (asteroids.isEmpty()) {
            messageInCenter("GAME OVER!", 4, Pixel.YELLOW)
            if (getKey(Key.ENTER).bPressed) reset()
            return true
        }

        if (player.dead) {
            messageInCenter("WASTED!", 4, Pixel.DARK_RED)
            if (getKey(Key.ENTER).bPressed) reset()
            return true
        }

        asteroids.forEach {
            // move
            it.move(elapsedTime)
            val (nx, ny) = wrapFloatCoordinates(Pair(it.x, it.y))
            it.x = nx; it.y = ny
            // draw
            drawString(it.xi, it.yi, scale = it.scale, text = it.letter, col = it.color)
        }

        with(player) {
            // player can shoot only 3 bullets
            if (getKey(Key.SPACE).bPressed && bullets.size < 1) {
                // shoot
                bullets.add(
                    SpaceObject(
                        this.x,
                        this.y,
                        max(50.0f, this.vx * 10) * sin(this.angle),
                        -max(50.0f, this.vy * 10) * cos(this.angle)
                    )
                )
            }

            if (getKey(Key.UP).bHeld) {
                // thrust
                this.accelerate(elapsedTime)
                val rpi = Random.nextDouble(PI - (1.0 / 18.0) * PI, PI + (1.0 / 18.0) * PI).toFloat()
                exhaust.add(
                    SpaceObject(
                        this.x,
                        this.y,
                        (this.vx + 20.0f) * sin(this.angle - rpi),
                        -(this.vy + 20.0f) * cos(this.angle - rpi)
                    )
                )
            }

            if (getKey(Key.LEFT).bPressed || getKey(Key.LEFT).bHeld) {
                // rotate left
                this.angle -= 5.0f * elapsedTime
            }

            if (getKey(Key.RIGHT).bPressed || getKey(Key.RIGHT).bHeld) {
                this.angle += 5.0f * elapsedTime
            }

            this.move(elapsedTime)
            val (nx, ny) = wrapFloatCoordinates(Pair(this.x, this.y))
            this.x = nx; this.y = ny
            // check collision
            val loc = getDrawTarget().getPixel(this.xi, this.yi)?.n
            // pixel in current location is not yet drawn.
            // so it either equal to space color or we approximated to the same coordinates
            if (loc != null && loc != spaceColor && loc != Pixel.WHITE.n) {
                // hit detected
                this.dead = true
            }
            drawTriangle(this[0], this[1], this[2])
        }

        exhaust.forEach {
            it.move(elapsedTime)
            it.time += elapsedTime
            val (nx, ny) = wrapFloatCoordinates(Pair(it.x, it.y))
            it.x = nx; it.y = ny
            draw(it.xi, it.yi)

            if (it.time > 0.3) {
                it.dead = true
            }
        }

        bullets.forEach {
            // first move
            it.move(elapsedTime)
            val (nx, ny) = wrapFloatCoordinates(Pair(it.x, it.y))
            it.x = nx; it.y = ny
            // check collision
            val loc = getDrawTarget().getPixel(it.xi, it.yi)?.n
            // pixel in current location is not yet drawn.
            // so it either equal to space color or we approximated to the same coordinates
            if (loc != null && loc != spaceColor && loc != Pixel.WHITE.n) {
                // hit detected
                it.dead = true // kill bullet

                val angle1 = Random.nextDouble(2 * PI).toFloat()
                val angle2 = Random.nextDouble(2 * PI).toFloat()
                val reversedLetters = letters.entries.associate { (k, v) -> v to k }

                val killAsteroid: (Pixel) -> Asteroid = { p: Pixel ->
                    val tmp = asteroids.filter { asteroid ->
                        asteroid.letter == reversedLetters[p]
                    }.minBy { asteroid ->
                        // center of the letter
                        val center = Pair(asteroid.x + 4 * asteroid.scale, asteroid.y + 4 * asteroid.scale)
                        // distance to the letter's center from current bullet
                        sqrt((center.first - nx).pow(2) + (center.second - ny).pow(2))
                    }
                    assertNotNull(tmp)
                    tmp.also { asteroid -> asteroid.dead = true }
                }

                val newAsteroid: (Asteroid, Float, Pixel) -> Asteroid = { a, angle, p ->
                    Asteroid(
                        reversedLetters[p]!!, // must exist
                        letters[reversedLetters[p]!!]!!, // must exist
                        a.scale - 1,
                        a.x,
                        a.y,
                        Random.nextDouble(10.0, 20.0).toFloat() * sin(angle),
                        Random.nextDouble(10.0, 20.0).toFloat() * cos(angle)
                    )
                }

                val splitAsteroid: (Pixel, Pixel) -> Unit = { self, new ->
                    val a = killAsteroid(self)
                    asteroids.add(newAsteroid(a, angle1, new))
                    asteroids.add(newAsteroid(a, angle2, new))
                }

                when (loc) {
                    Pixel.YELLOW.n -> {// H letter hit split to two A
                        splitAsteroid(Pixel.YELLOW, Pixel.GREEN)
                        score = replaceCharAtIndex(score, 0, 'H')
                    }

                    Pixel.GREEN.n -> {
                        splitAsteroid(Pixel.GREEN, Pixel.BLUE)
                        score = replaceCharAtIndex(score, 1, 'A')
                    }

                    Pixel.BLUE.n -> {
                        splitAsteroid(Pixel.BLUE, Pixel.CYAN)
                        score = replaceCharAtIndex(score, 2, 'C')
                    }

                    Pixel.CYAN.n -> {
                        splitAsteroid(Pixel.CYAN, Pixel.RED)
                        score = replaceCharAtIndex(score, 3, 'K')
                    }

                    Pixel.RED.n -> {
                        splitAsteroid(Pixel.RED, Pixel.MAGENTA)
                        score = replaceCharAtIndex(score, 4, 'a')
                    }

                    Pixel.MAGENTA.n -> {
                        splitAsteroid(Pixel.MAGENTA, Pixel.DARK_YELLOW)
                        score = replaceCharAtIndex(score, 5, 't')
                    }

                    Pixel.DARK_YELLOW.n -> {
                        splitAsteroid(Pixel.DARK_YELLOW, Pixel.DARK_CYAN)
                        score = replaceCharAtIndex(score, 6, 'h')
                    }

                    Pixel.DARK_CYAN.n -> {
                        splitAsteroid(Pixel.DARK_CYAN, Pixel.DARK_MAGENTA)
                        score = replaceCharAtIndex(score, 7, 'o')
                    }

                    Pixel.DARK_MAGENTA.n -> {
                        killAsteroid(Pixel.DARK_MAGENTA)
                        score = replaceCharAtIndex(score, 8, 'n')
                    }
                }
            }

            draw(it.xi, it.yi)
        }

        bullets.removeAll { it.dead }
        asteroids.removeAll { it.dead }
        exhaust.removeAll { it.dead }
        val msg = "Score: $score"
        fillRect(4, 4, msg.length * 8 + 6, 10, Pixel.BLACK)
        drawString(5, 5, msg)

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

    private lateinit var score: String
    private val bullets: MutableList<SpaceObject> = mutableListOf()
    private val asteroids: MutableList<Asteroid> = mutableListOf()
    private val exhaust: MutableList<SpaceObject> = mutableListOf()
    private val player = Ship(Triple(Pair(0.0f, -5.0f), Pair(-2.5f, +2.5f), Pair(+2.5f, +2.5f)))
    private val letters = mapOf(
        "H" to Pixel.YELLOW,
        "A" to Pixel.GREEN,
        "C" to Pixel.BLUE,
        "K" to Pixel.CYAN,
        "a" to Pixel.RED,
        "t" to Pixel.MAGENTA,
        "h" to Pixel.DARK_YELLOW,
        "o" to Pixel.DARK_CYAN,
        "n" to Pixel.DARK_MAGENTA
    )
    override val appName = "Asteroids Demo"

    companion object {
        private const val spaceColor = 0xFF4c4a41u
        private const val transparentWhite = 0x00FFFFFFu
    }
}

fun replaceCharAtIndex(input: String, i: Int, c: Char): String {
    val chars = input.toCharArray()
    chars[i] = c
    return chars.concatToString()
}

fun Pair<Float, Float>.roundToInt() = Pair(this.first.roundToInt(), this.second.roundToInt())

@ExperimentalUnsignedTypes
fun main() {
    val demo = AsteroidsDemo()
    if (demo.construct() == RetCode.OK)
        demo.start()
}