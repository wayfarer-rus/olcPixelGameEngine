package game.vermis.data.rooms.greengrave

import game.vermis.Drawable
import game.vermis.Layer
import game.vermis.LayersMap
import game.vermis.TILE_SIZE
import game.vermis.data.rooms.roomLayoutToString
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Vi2d

class IsolatedCrypt : Drawable {
  private val roomLayoutPath = "resources/rooms/isolated_crypt.txt"
  private val roomLayout: String by lazy { roomLayoutToString(roomLayoutPath) }

  override fun draw(e: PixelGameEngine) {
    var pos = Vi2d(0, 0)
    val size = TILE_SIZE

    for (line in roomLayout.lines()) {
      for (c in line) {
        when (c) {
          'W' -> {
            e.setDrawTarget(LayersMap[Layer.BACKGROUND])
            e.fillRect(pos, size, Pixel.WHITE)
          }
          '.' -> {
            e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
            e.fillRect(pos, size, Pixel.YELLOW)
          }
          'D' -> {
            e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
            e.fillRect(pos, size, Pixel.GREEN)
          }
          else -> {
            e.setDrawTarget(LayersMap[Layer.BACKGROUND])
            e.drawRect(pos, size, Pixel.DARK_GREY)
          }
        }
        // move X
        pos = Vi2d(pos.x + size.x, pos.y)
      }
      // reset X, move Y
      pos = Vi2d(0, pos.y + size.y)
    }
  }
}
