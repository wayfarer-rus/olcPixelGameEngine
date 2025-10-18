package game.pixel_shooter

import olc.game_engine.Pixel
import olc.game_engine.Sprite

@ExperimentalUnsignedTypes
class Map(val w: Int, val h: Int) : Sprite(w * globalSpriteSize, h * globalSpriteSize) {

    init {
        this.data.fill(color.n)
    }

    companion object {
        @ExperimentalUnsignedTypes
        val color = Pixel(69, 24, 4)//(193,68,14)
    }
}