package game.vermis.data.rooms.greengrave

import game.vermis.Layer
import game.vermis.LayersMap
import game.vermis.Room
import game.vermis.TILE_SIZE
import game.vermis.isTraversable
import game.vermis.data.rooms.roomLayoutToString
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Vi2d

class RootCave : Room {
  override val name = "root_cave"

  private val roomLayoutPath = "resources/rooms/root_cave.txt"
  override val roomLayout: Array<CharArray> by lazy {
    val fileContent = roomLayoutToString(roomLayoutPath)
    fileContent.split("\n").map { it.toCharArray() }.toTypedArray()
  }

  override fun screenOffset(e: PixelGameEngine): Vi2d {
    val screenCenter = Vi2d(e.screenWidth() / 2, e.screenHeight() / 2)
    val roomCenter = Vi2d(roomLayout.first().size * TILE_SIZE.x / 2, roomLayout.size * TILE_SIZE.y / 2)
    return screenCenter - roomCenter
  }

  override fun draw(e: PixelGameEngine) {
    val size = TILE_SIZE
    val offset = screenOffset(e)
    var pos = offset

    for (line in roomLayout) {
      for (c in line) {
        if (!isTraversable(c)) {
          e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
          val color = when (c) {
            'R' -> Pixel.DARK_YELLOW
            '.' -> Pixel.YELLOW
            else -> Pixel.DARK_GREY
          }
          e.fillRect(pos, size, color)
        } else {
          e.setDrawTarget(LayersMap[Layer.BACKGROUND])
          when (c) {
            'E' -> e.fillRect(pos, size, Pixel.GREEN)
            else -> e.drawRect(pos, size, Pixel.DARK_GREY)
          }
        }
        pos = Vi2d(pos.x + size.x, pos.y)
      }
      pos = Vi2d(offset.x, pos.y + size.y)
    }
  }
}
