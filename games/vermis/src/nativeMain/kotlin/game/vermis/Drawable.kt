package game.vermis

import olc.game_engine.PixelGameEngine
import olc.game_engine.Vi2d

interface Drawable {
  fun draw(e: PixelGameEngine)
}

val TILE_SIZE = Vi2d(32, 32)
