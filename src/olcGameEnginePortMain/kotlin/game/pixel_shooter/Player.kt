package game.pixel_shooter

import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.random.Random

class Player(pos: Pos<Float>, velocityVector: Vector = Vector(0f, 0f), angle: Float = 0f, speed: Float = 4.0f) :
    Creature(pos, velocityVector, angle, speed) {

    private var drawStep: Boolean = false
    private var fire: Boolean = false
    private var stepState: StepState = StepState.LEFT_STEP
    private var stepsTiming: Float = 0.0f
    private var playerState = PlayerState.STANDING
    private var currentGun: Gun = Pistol()
    private var projectiles = mutableListOf<Projectile>()

    @ExperimentalUnsignedTypes
    fun drawSelf(
        gfx: PixelGameEngine,
        offsetPos: Pos<Float>,
        world: Map,
        aim: Pos<Int>
    ) {
        // position on screen
        val x = (pos.x - offsetPos.x) * spriteSize
        val y = (pos.y - offsetPos.y) * spriteSize

        projectiles.forEach {
            it.drawSelf(gfx)
        }

        gfx.fillRect(
            (x - spriteSize / 2).roundToInt(),
            (y - spriteSize / 2).roundToInt(), spriteSize, spriteSize, Pixel(0, 66, 255)
        )
        // velocity vector
        gfx.drawLine(
            Pair(x.roundToInt(), y.roundToInt()),
            velocityVector.toPos(Pos(x, y), spriteSize.toFloat()).roundToInt().p
        )

        // hit(box)circle
        gfx.drawCircle(x.roundToInt(), y.roundToInt(), spriteSize / 2)

        // effects
        if (fire) {
            fire = false

            projectiles.add(
                Projectile(Pos(x, y), Vector(aim - Pos(x, y)), currentGun.projectileVelocity, world)
            )

            val shellPos = pos * spriteSize
            gfx.setDrawTarget(world)
            gfx.draw(
                shellPos.x.roundToInt() + Random.nextInt(-spriteSize / 2, spriteSize / 2),
                shellPos.y.roundToInt() + Random.nextInt(-spriteSize / 2, spriteSize / 2)
            )
            gfx.resetDrawTarget()
        }

        if (drawStep) {
            drawStep = false

            // calculate step pos
            val stepPos = if (stepState == StepState.LEFT_STEP) {
                velocityVector.rotate(-PI / 2).toPos(pos * spriteSize, spriteSize / 4.0f)
            } else {
                velocityVector.rotate(PI / 2).toPos(pos * spriteSize, spriteSize / 4.0f)
            }

            gfx.setDrawTarget(world)
            gfx.setPixelMode(Pixel.Mode.ALPHA)
            gfx.fillCircle(stepPos.x.roundToInt(), stepPos.y.roundToInt(), spriteSize / 8, stepColor)
            gfx.setPixelMode(Pixel.Mode.NORMAL)
            gfx.resetDrawTarget()
        }
    }

    @ExperimentalUnsignedTypes
    override fun updateState(
        gfx: PixelGameEngine,
        world: Map,
        elapsedTime: Float
    ) {
        var newPlayerPosX = pos.x
        var newPlayerPosY = pos.y
        playerState = PlayerState.STANDING

        // up-down
        if (gfx.getKey(Key.W).bPressed || gfx.getKey(Key.W).bHeld ||
            gfx.getKey(Key.UP).bPressed || gfx.getKey(Key.UP).bHeld
        ) {
            newPlayerPosY -= speed * elapsedTime
            playerState = PlayerState.MOVING
        } else if (gfx.getKey(Key.S).bPressed || gfx.getKey(Key.S).bHeld ||
            gfx.getKey(Key.DOWN).bPressed || gfx.getKey(Key.DOWN).bHeld
        ) {
            newPlayerPosY += speed * elapsedTime
            playerState = PlayerState.MOVING
        }
        // left-right
        if (gfx.getKey(Key.A).bPressed || gfx.getKey(Key.A).bHeld ||
            gfx.getKey(Key.LEFT).bPressed || gfx.getKey(Key.LEFT).bHeld
        ) {
            newPlayerPosX -= speed * elapsedTime
            playerState = PlayerState.MOVING
        } else if (gfx.getKey(Key.D).bPressed || gfx.getKey(Key.D).bHeld ||
            gfx.getKey(Key.RIGHT).bPressed || gfx.getKey(Key.RIGHT).bHeld
        ) {
            newPlayerPosX += speed * elapsedTime
            playerState = PlayerState.MOVING
        }

        // update velocityVector
        velocityVector = Vector(newPlayerPosX - pos.x, newPlayerPosY - pos.y)

        pos = Pos(newPlayerPosX.let {
            when {
                it < 0 -> 0.0f
                it > world.w - 1 -> (world.w - 1).toFloat()
                else -> it
            }
        }, newPlayerPosY.let {
            when {
                it < 0 -> 0.0f
                it > world.h - 1 -> (world.h - 1).toFloat()
                else -> it
            }
        })

        stepsTiming += elapsedTime

        if (stepsTiming >= 0.2f) {
            stepsTiming -= 0.2f

            if (playerState == PlayerState.MOVING) {
                stepState = if (stepState == StepState.LEFT_STEP) StepState.RIGHT_STEP else StepState.LEFT_STEP
                drawStep = true
            }
        }

        if (gfx.getMouseKey(1).bPressed) {
            // fire
            fire = currentGun.fire(elapsedTime)
        }

        projectiles.forEach {
            it.updateSelf(elapsedTime)
        }

        projectiles.removeAll { it.away }
    }

    private enum class StepState {
        LEFT_STEP, RIGHT_STEP
    }

    private enum class PlayerState {
        STANDING, MOVING
    }

    companion object {
        @ExperimentalUnsignedTypes
        private val stepColor = Pixel(80, 80, 80, 50)
        private const val spriteSize = globalSpriteSize
    }
}


