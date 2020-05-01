package demos.destructible_sprite

import geometry_2d.Point
import olc.game_engine.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

@ExperimentalUnsignedTypes
class DestructibleSprite : PixelGameEngineImpl() {
    override fun onUserCreate(): Boolean {
        // define initial sprite state: green square
        setDrawTarget(sprite)
        clear(Pixel.GREEN)
        resetDrawTarget()
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        val gunPos = Point(getDrawTargetWidth() / 2, getDrawTargetHeight() / 2) + Point.pointTo(
            gunA,
            max(getDrawTargetWidth(), getDrawTargetHeight()).toFloat()
        )

        if (gunPos.x < 0) gunPos.x = 0f
        if (gunPos.x > getDrawTargetWidth() - 1) gunPos.x = getDrawTargetWidth().toFloat()
        if (gunPos.y < 0) gunPos.y = 0f
        if (gunPos.y > getDrawTargetHeight() - 1) gunPos.y = getDrawTargetHeight().toFloat()

        // handle input
        if (getKey(Key.D).bPressed || getKey(Key.D).bHeld) {
            // move gun right
            gunA -= gunMoveSpeed * elapsedTime
        } else if (getKey(Key.A).bPressed || getKey(Key.A).bHeld) {
            // move gun left
            gunA += gunMoveSpeed * elapsedTime
        }

        if (getKey(Key.SPACE).bPressed || getMouseKey(1).bPressed) {
            // fire gun
            val b = Bullet(gunPos, bulletSpeed, Point(getMouseX(), getMouseY()) - gunPos)
            bullets.add(b)
        }


        // draw on screen
        clear()

        // draw sprite in center of the screen
        val spritePos =
            Point(getDrawTargetWidth() / 2 - sprite.width / 2, getDrawTargetHeight() / 2 - sprite.height / 2)
        drawSprite(spritePos.xi, spritePos.yi, sprite)

        // draw gun
        val gunEndPoint = (Point(getMouseX(), getMouseY()) - gunPos).toLength(1.5f) + gunPos
        drawLine(gunPos.toPair(), gunEndPoint.toPair())

        // draw bullets
        bullets.forEach {
            it.move(elapsedTime)
            draw(it.position.xi, it.position.yi, Pixel.YELLOW)

            val currentBulletInSpriteCoordinates = it.position - spritePos
            if (currentBulletInSpriteCoordinates.inBounds(
                    Point(0, 0),
                    Point(sprite.width.toFloat() - 0.5f, sprite.height.toFloat() - 0.5f)
                )
            ) {
                // collision with the sprite
                val r = Point(currentBulletInSpriteCoordinates.xi, currentBulletInSpriteCoordinates.yi)
                val f = Point(floor(currentBulletInSpriteCoordinates.x), floor(currentBulletInSpriteCoordinates.y))
                val c = Point(ceil(currentBulletInSpriteCoordinates.x), ceil(currentBulletInSpriteCoordinates.y))

                if (sprite.getPixel(r.xi, r.yi) != Pixel.BLANK ||
                    sprite.getPixel(f.xi, f.yi) != Pixel.BLANK
                ) {
                    // draw into sprite
                    setDrawTarget(sprite)
                    draw(r.xi, r.yi, Pixel.BLANK)
                    draw(f.xi, f.yi, Pixel.BLANK)
//                draw(c.xi, c.yi, Pixel.BLANK)
                    resetDrawTarget()

                    it.position = Point(-10, -10)
                }
            }
        }

        bullets.removeAll { it.position.x < 0 || it.position.y < 0 || it.position.x > getDrawTargetWidth() || it.position.y > getDrawTargetHeight() }

        // draw mouse cursor
        fillCircle(getMouseX(), getMouseY(), 1)
        return true
    }

    private val sprite = Sprite(8, 8)
    private var gunA = 0f
    private val bullets: MutableList<Bullet> = mutableListOf()
    private val bulletSpeed = 31f
    private val gunMoveSpeed = 2f
}

class Bullet(
    var position: Point,
    speed: Float,
    vector: Point
) {
    private var v: Point = Point(0, 0)

    init {
        this.v = vector.toLength(speed)
    }

    fun move(elapsedTime: Float) {
        position += v * elapsedTime
    }

}

@ExperimentalUnsignedTypes
fun main() {
    val demo = DestructibleSprite()

    if (demo.construct(32, 32, 24, 24) == rcode.OK) demo.start()
}