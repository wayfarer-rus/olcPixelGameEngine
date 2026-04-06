package game.vermis

import olc.game_engine.PixelGameEngine
import olc.game_engine.Vi2d

interface Drawable {
  fun draw(e: PixelGameEngine)
}

interface Room : Drawable {
  val name: String
  val roomLayout: Array<CharArray>
  fun screenOffset(e: PixelGameEngine): Vi2d
  fun initRoom(e: PixelGameEngine)
}

fun isTraversable(c: Char): Boolean = c in " DEX="

val TILE_SIZE = Vi2d(32, 32)
