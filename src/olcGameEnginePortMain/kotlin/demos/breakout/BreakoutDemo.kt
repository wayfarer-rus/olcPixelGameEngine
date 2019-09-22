package demos.breakout

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.*
import kotlin.random.Random

@ExperimentalUnsignedTypes
class BreakoutDemo : PixelGameEngineImpl() {
    override val appName = "Breakout"

    override fun onUserCreate(): Boolean {
        batPos = Pos(getDrawTargetWidth() / 2f, getDrawTargetHeight().toFloat())
        resetBall()
        level = mutableListOf()

        for (j in 0 until 10) {
            for (i in 0 until getDrawTargetWidth() / Block.width) {
                level.add(
                    Block(
                        pos = Pos((i * Block.width).toFloat() + 3f, (j * Block.height).toFloat()),
                        color = randomPixel()
                    )
                )
            }
        }

        return true
    }

    private fun resetBall() {
        ballPos = Pos(getDrawTargetWidth() / 2f, getDrawTargetHeight() / 2f)
        ballVec = (PI - PI / 4.0).toFloat()
    }

    private fun randomPixel() = Pixel(Random.nextInt(255), Random.nextInt(255), Random.nextInt(255))

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel.BLACK)

        if (getKey(Key.LEFT).bHeld || getKey(Key.LEFT).bPressed) {
            batPos.x -= 80 * elapsedTime
        } else if (getKey(Key.RIGHT).bHeld || getKey(Key.RIGHT).bPressed) {
            batPos.x += 80 * elapsedTime
        }

        if (getKey(Key.SPACE).bPressed) {
            resetBall()
            startGame = true
        }

        if (startGame) {
            if (ballPos.x + ballR >= getDrawTargetWidth() || ballPos.x - ballR <= 0) {
                ballVec += ballVec.sign * (PI / 2).toFloat()
            }

            if (ballPos.y > getDrawTargetHeight() + ballR) {
                resetBall()
            } else if ((ballPos.y - ballR <= 0) || (
                        ((ballPos.x - ballR) in (batPos.x - batWidth / 2)..(batPos.x + batWidth / 2) ||
                                (ballPos.x + ballR) in (batPos.x - batWidth / 2)..(batPos.x + batWidth / 2)
                                )
                                &&
                                ((ballPos.y - ballR) in (batPos.y - batHeight * 2)..batPos.y ||
                                        (ballPos.y + ballR) in (batPos.y - batHeight * 2)..batPos.y)
                        )
            ) {
                ballVec *= -1
            }

            level.find {
                ((ballPos.x - ballR) in it.pos.x..(it.pos.x + Block.width) ||
                        (ballPos.x + ballR) in it.pos.x..(it.pos.x + Block.width)
                        )
                        &&
                        ((ballPos.y - ballR) in it.pos.y..(it.pos.y + Block.height) ||
                                (ballPos.y + ballR) in it.pos.y..(it.pos.y + Block.height))
            }?.let {
                it.active = false; level.remove(it);

                if (ballPos.y > it.pos.y + Block.height || ballPos.y < it.pos.y) {
                    ballVec *= -1
                } else {
                    ballVec += ballVec.sign * (PI / 2).toFloat()
                }
            }

            ballPos.x += ballSpeed * cos(ballVec) * elapsedTime
            ballPos.y += ballSpeed * sin(ballVec) * elapsedTime
        }

        // draw blocks
        level.forEach {
            fillRect(it.pos.x.roundToInt(), it.pos.y.roundToInt(), Block.width, Block.height, it.color)
        }

        // draw bat
        val x = batPos.x - batWidth / 2
        val y = batPos.y - batHeight * 2

        fillRect(x.roundToInt(), y.roundToInt(), batWidth, batHeight, Pixel.WHITE)

        // draw ball
        fillCircle(ballPos.x.roundToInt(), ballPos.y.roundToInt(), ballR)

        return true
    }

    private lateinit var batPos: Pos
    private lateinit var ballPos: Pos
    private val batHeight = 5
    private val batWidth = 20
    private var ballVec: Float = 0f
    private var ballSpeed: Float = 80f
    private val ballR = 2
    private lateinit var level: MutableList<Block>
    private var startGame = false
}

class Block(val pos: Pos, var active: Boolean = true, val color: Pixel) {
    companion object {
        const val width = 10
        const val height = 5
    }
}

class Pos(var x: Float = 0f, var y: Float = 0f)

@ExperimentalUnsignedTypes
fun main() {
    val demo = BreakoutDemo()
    if (demo.construct() == rcode.OK) demo.start()
}