package game.pixel_shooter

import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.roundToInt

const val globalSpriteSize = 16

@ExperimentalUnsignedTypes
class PixelShooterGame : PixelGameEngineImpl() {
    override val appName = "Pixel Shooter"

    override fun onUserCreate(): Boolean {
        visibleTiles = Dimensions(screenWidth() / spriteSize, screenHeight() / spriteSize)
//        player.pos = Pos(1.0f, 1.0f)
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        // grab mouse cursor
        val mouseCursorPos = Pos(getMouseX(), getMouseY())

        // handle player movement
        player.updateState(this, map, elapsedTime)

        // fix camera on the player
        cameraPos = player.pos

        // calculate offset
        // clamping camera to game boundaries
        offsetPos = Pos(
            (cameraPos.x - visibleTiles.w / 2.0f).let {
                when {
                    it < 0.0 -> 0.0f
                    it > map.w - visibleTiles.w -> (map.w - visibleTiles.w).toFloat()
                    else -> it
                }
            },
            (cameraPos.y - visibleTiles.h / 2.0f).let {
                when {
                    it < 0.0 -> 0.0f
                    it > map.h - visibleTiles.h -> (map.h - visibleTiles.h).toFloat()
                    else -> it
                }
            }
        )

        // render
        drawPartialSprite(
            0, 0, map,
            (offsetPos.x * spriteSize).roundToInt(), (offsetPos.y * spriteSize).roundToInt(),
            screenWidth(), screenHeight()
        )

        player.drawSelf(this, offsetPos, map, mouseCursorPos)

        // draw aim
        fillCircle(mouseCursorPos.x, mouseCursorPos.y, 1)
        return true
    }

    private var cameraPos = Pos(0f, 0f)
    private var offsetPos = Pos(0f, 0f)
    private var visibleTiles = Dimensions(0, 0)

    private val map = Map(48, 48) // in sprites. we can see only 16 sprites on screen
    private val player = Player(Pos(map.w / 2.0f, map.h / 2.0f), speed = 4.0f)

    companion object {
        const val spriteSize = globalSpriteSize
    }
}



inline class Dimensions(inline val p: Pair<Int, Int>) {
    inline val w: Int
        inline get() = p.first
    inline val h: Int
        inline get() = p.second

    constructor(w: Int, h: Int) : this(Pair(w, h))
}

@ExperimentalUnsignedTypes
fun main() {
    val game = PixelShooterGame()
    if (game.construct(screen_w = 256, screen_h = 192, pixel_w = 3, pixel_h = 3) == rcode.OK) game.start()
}