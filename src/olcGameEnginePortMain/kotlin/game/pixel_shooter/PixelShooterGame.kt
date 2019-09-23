package game.pixel_shooter

import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.rcode
import kotlin.math.roundToInt

const val globalSpriteSize = 8

@ExperimentalUnsignedTypes
class PixelShooterGame : PixelGameEngineImpl() {
    override val appName = "Pixel Shooter"

    override fun onUserCreate(): Boolean {
        visibleTiles = Dimensions(screenWidth() / spriteSize, screenHeight() / spriteSize)
        monsters = mutableListOf()
        monsters.add(Zombie(player.pos - Pos(5f, 5f)))
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
            visibleTiles.w * spriteSize, visibleTiles.h * spriteSize
        )

        player.drawSelf(this, offsetPos, map, mouseCursorPos)

        // draw monsters
        monsters.forEach {
            it.drawSelf(this, offsetPos)
            drawLine(it.screenPos.roundToInt().p, player.screenPos.roundToInt().p)
        }

        // draw aim
        fillCircle(mouseCursorPos.x, mouseCursorPos.y, 1)

        // sraw some other stuff
        drawString(1, 1, "o = $offsetPos\np = ${player.pos}\nps = ${player.screenPos}")
        return true
    }

    private var cameraPos = Pos(0f, 0f)
    private var offsetPos = Pos(0f, 0f)
    private var visibleTiles = Dimensions(0, 0)

    private val map = Map(48, 48) // in sprites. we can see only 16 sprites on screen
    private val player = Player(Pos(map.w / 2.0f, map.h / 2.0f), speed = 4.0f)
    private lateinit var monsters: MutableList<Creature>

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
    if (game.construct(
            screen_w = (256 * 1.5).roundToInt(),
            screen_h = (192 * 1.5).roundToInt(),
            pixel_w = 3,
            pixel_h = 3
        ) == rcode.OK
    ) game.start()
}