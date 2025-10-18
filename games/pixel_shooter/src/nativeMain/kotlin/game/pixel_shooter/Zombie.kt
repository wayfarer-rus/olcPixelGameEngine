package game.pixel_shooter

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import kotlin.math.roundToInt

class Zombie(
    pos: Pos<Float>
) : Creature(pos) {

    @ExperimentalUnsignedTypes
    override fun drawSelf(gfx: PixelGameEngine, offsetPos: Pos<Float>) {
        screenPos = toScreen(pos, offsetPos)

        gfx.fillRect(
            screenPos.x.roundToInt() - spriteSize / 2,
            screenPos.y.roundToInt() - spriteSize / 2,
            spriteSize, spriteSize, color
        )

        gfx.drawCircle(screenPos.x.roundToInt(), screenPos.y.roundToInt(), spriteSize / 2)
    }

    @ExperimentalUnsignedTypes
    override fun updateState(gfx: PixelGameEngine, world: Map, elapsedTime: Float) {
        super.updateState(gfx, world, elapsedTime)
    }

    companion object {
        @ExperimentalUnsignedTypes
        private val color = Pixel(65, 94, 60)
        private const val spriteSize = globalSpriteSize
    }
}