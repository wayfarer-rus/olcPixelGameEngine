package demos.fireworks

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.*
import kotlin.random.Random

const val G = 9.81f

@ExperimentalUnsignedTypes
class Particle(
    var x0: Int,
    var y0: Int,
    var p: Pixel,
    val angle: Float = (PI / 3).toFloat(),
    val V: Int = 50,
    val drawTargetWidth: Int,
    val drawTargetHeight: Int
) {
    fun calculateNewPosition(elapsedTime: Float) {
        this.time += elapsedTime
        this.x = (V * cos(angle) * time).roundToInt() + x0
        val vy = V * sin(angle)
        this.y = (vy * time - (G / 2) * time.pow(2) + y0).roundToInt()
    }

    var dead = false
    var exploded = false
    var y: Int = y0
    var x: Int = x0
    var time = 0.0f

    val canExplode
        inline get() = (V * sin(angle)).pow(2) / (2 * G) <= (this.y + 1)

    val visible
        inline get() = y.inBounds(0, drawTargetHeight + 100) && x.inBounds(
            -(drawTargetWidth / 2 + 50),
            drawTargetWidth / 2 + 50
        )

    // translate coordinates
    val tx: Int
        inline get() = this.drawTargetWidth / 2 + this.x

    val ty: Int
        inline get() = this.drawTargetHeight - this.y
}

fun Int.inBounds(begin: Int, end: Int) = this in begin..end

@ExperimentalUnsignedTypes
class FireworksDemo : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        time -= elapsedTime
        clear(Pixel.BLACK)

        drawString(5, getDrawTargetHeight() - 5 - 8, firesList.size.toString())
        drawString(
            getDrawTargetWidth() - 5 - explosionsList.size.toString().length * 8,
            getDrawTargetHeight() - 5 - 8,
            explosionsList.size.toString(),
            Pixel.YELLOW
        )

        if (getMouseKey(1).bPressed || getKey(Key.SPACE).bPressed || time < 0) {
            time = Random.nextDouble(0.1, 0.5).toFloat()
            // fire a dot
            firesList.add(
                Particle(
                    0, 0, Pixel.WHITE,
                    angle = (Random.nextInt(70, 120) * (PI.toFloat() / 180)),
                    V = Random.nextInt(40, 60),
                    drawTargetWidth = getDrawTargetWidth(), drawTargetHeight = getDrawTargetHeight()
                )
            )
        }

        firesList.forEach { particle: Particle ->
            draw(particle.tx, particle.ty, particle.p)
            // move
            particle.calculateNewPosition(elapsedTime)
            // explode
            if (particle.canExplode) {
                repeat(n) {
                    explosionsList.add(
                        Particle(
                            particle.x,
                            particle.y,
                            Pixel(Random.nextInt(0, 255), Random.nextInt(0, 255), Random.nextInt(0, 255)),
                            V = Random.nextInt(20, 100),
                            angle = Random.nextDouble(2 * PI).toFloat(),
                            drawTargetWidth = getDrawTargetWidth(),
                            drawTargetHeight = getDrawTargetHeight()
                        )
                    )
                }

                particle.exploded = true
            }
        }

        explosionsList.forEach {
            draw(it.tx, it.ty, it.p)
            it.calculateNewPosition(elapsedTime)
            it.p = Pixel(((it.p.af / 4).toUInt() shl 24) or (0x00FFFFFFu and it.p.n))

            if (it.time >= explosionParticleLifeTime) it.dead = true
        }

        firesList.removeAll { !it.visible || it.exploded }
        explosionsList.removeAll { !it.visible || it.dead }

        drawString(getDrawTargetWidth() / 2 - 56, 5, "Hackathon 2019", Pixel.MAGENTA)

        return true
    }

    private val n = 256
    private var time = 1.0f
    private val explosionParticleLifeTime = 3.0f
    private var explosionsList: MutableList<Particle> = mutableListOf()
    private var firesList: MutableList<Particle> = mutableListOf()
    override val appName = "Fireworks Demo"
}

@ExperimentalUnsignedTypes
fun main() {
    val demo = FireworksDemo()

    if (demo.construct() == rcode.OK)
        demo.start()
}