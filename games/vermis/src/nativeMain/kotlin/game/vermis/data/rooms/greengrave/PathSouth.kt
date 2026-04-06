package game.vermis.data.rooms.greengrave

import game.vermis.Drawable
import game.vermis.Layer
import game.vermis.LayersMap
import game.vermis.TILE_SIZE
import game.vermis.data.rooms.roomLayoutToString
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Vi2d

class PathSouth : Drawable {
  val name = "path_south"

  private val roomLayoutPath = "resources/rooms/path_south.txt"
  private val roomLayout: Array<CharArray> by lazy {
    val fileContent = roomLayoutToString(roomLayoutPath)
    fileContent.split("\n").map { it.toCharArray() }.toTypedArray()
  }

  override fun draw(e: PixelGameEngine) {
    val size = TILE_SIZE

    val screenCenter = Vi2d(e.screenWidth() / 2, e.screenHeight() / 2)
    val roomCenter = Vi2d(roomLayout.first().size * size.x / 2, roomLayout.size * size.y / 2)
    val offset = screenCenter - roomCenter
    var pos = offset

    for (line in roomLayout) {
      for (c in line) {
        when (c) {
          'W' -> {
            e.setDrawTarget(LayersMap[Layer.BACKGROUND])
            e.fillRect(pos, size, Pixel.WHITE)
          }
          'C' -> {
            e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
            e.fillRect(pos, size, Pixel.DARK_GREY)
          }
          '.' -> {
            e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
            e.fillRect(pos, size, Pixel.YELLOW)
          }
          '=' -> {
            e.setDrawTarget(LayersMap[Layer.BACKGROUND])
            e.fillRect(pos, size, Pixel.VERY_DARK_GREY)
          }
          else -> {
            e.setDrawTarget(LayersMap[Layer.BACKGROUND])
            e.drawRect(pos, size, Pixel.DARK_GREY)
          }
        }
        pos = Vi2d(pos.x + size.x, pos.y)
      }
      pos = Vi2d(offset.x, pos.y + size.y)
    }
  }
}
