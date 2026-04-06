package game.vermis.data.rooms.greengrave

import game.vermis.Layer
import game.vermis.LayersMap
import game.vermis.Room
import game.vermis.TILE_SIZE
import game.vermis.data.rooms.localPathToAbsolute
import game.vermis.data.rooms.roomLayoutToString
import game.vermis.isTraversable
import olc.game_engine.Decal
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d
import olc.game_engine.rcode
import olc.game_engine.toVf2d

class IsolatedCrypt : Room {
  override val name = "isolated_crypt"

  private val roomLayoutPath = "resources/rooms/isolated_crypt.txt"
  private val roomBackgroundImagePath = "resources/rooms/backgrounds/isolated_crypt.png"

  private lateinit var backgroundImage: Decal
  private var backgroundScale: Vf2d = Vf2d(1, 1)

  override val roomLayout: Array<CharArray> by lazy {
    val fileContent = roomLayoutToString(roomLayoutPath)
    fileContent.split("\n").filter { it.isNotEmpty() }.map { it.toCharArray() }.toTypedArray()
  }

  override fun screenOffset(e: PixelGameEngine): Vi2d {
    val screenCenter = Vi2d(e.screenWidth() / 2, e.screenHeight() / 2)
    val roomCenter = Vi2d(roomLayout.first().size * TILE_SIZE.x / 2, roomLayout.size * TILE_SIZE.y / 2)
    return screenCenter - roomCenter
  }

  override fun initRoom(e: PixelGameEngine) {
    val backgroundSprite = Sprite()
    if (backgroundSprite.loadFromFile(localPathToAbsolute(roomBackgroundImagePath)) != rcode.OK) {
      throw IllegalStateException("Failed to load background image for room $name from path: $roomBackgroundImagePath")
    }

    backgroundImage = e.createDecal(backgroundSprite)
    // calculate background scale for drawing
    backgroundScale = Vf2d(
      TILE_SIZE.x * roomLayout.first().size.toFloat() / backgroundSprite.width.toFloat(),
      TILE_SIZE.y * roomLayout.size.toFloat() / backgroundSprite.height.toFloat()
    )
  }

  override fun draw(e: PixelGameEngine) {
    val size = TILE_SIZE
    val offset = screenOffset(e)
    var pos = offset

    e.setDrawTarget(LayersMap[Layer.BACKGROUND])
//    e.drawDecal(pos.toVf2d(), backgroundImage, backgroundScale)

    for (line in roomLayout) {
      for (c in line) {
        if (!isTraversable(c)) {
          e.setDrawTarget(LayersMap[Layer.INTERACTABLE])
          val color = when (c) {
            'W' -> Pixel.WHITE
            '.' -> Pixel.YELLOW
            else -> Pixel.DARK_GREY
          }
          e.fillRect(pos, size, color)
        } else {
          e.setDrawTarget(LayersMap[Layer.BACKGROUND])
          when (c) {
            'D' -> e.fillRect(pos, size, Pixel.GREEN)
//            else -> e.drawRect(pos, size, Pixel.DARK_GREY)
          }
        }
        pos = Vi2d(pos.x + size.x, pos.y)
      }
      pos = Vi2d(offset.x, pos.y + size.y)
    }
  }
}
