package game.pixel_shooter

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine

class Projectile(
    var pos: Pos<Float>,
    var vector: Vector,
    var velocity: Float,
    private val world: Map
) {
    var previousPos: Pos<Float> = pos
    var away = false

    @ExperimentalUnsignedTypes
    fun updateSelf(elapsedTime: Float) {
        previousPos = pos
        pos = vector.toPos(previousPos, velocity * elapsedTime)

        if (pos.x < -50 || pos.x > world.width + 50 ||
            pos.y < -50 || pos.y > world.height + 50
        ) {
            away = true
        }
    }

    @ExperimentalUnsignedTypes
    fun drawSelf(gfx: PixelGameEngine, offsetPos: Pos<Float>) {
        // position on screen
        val previousScreenPos = (previousPos - offsetPos) * globalSpriteSize
        val screenPos = (pos - offsetPos) * globalSpriteSize
        gfx.drawLine(previousScreenPos.roundToInt().p, screenPos.roundToInt().p, projectileColor)
    }

    companion object {
        @ExperimentalUnsignedTypes
        private val projectileColor = Pixel.YELLOW
    }
}
