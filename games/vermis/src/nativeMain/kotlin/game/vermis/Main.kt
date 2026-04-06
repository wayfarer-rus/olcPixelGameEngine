package game.vermis

import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode

class Vermis : PixelGameEngineImpl() {
  override val appName = "Vermis"

  private var currentRoomIndex = 0

  override fun onUserCreate(): Boolean {
    LayersMap.initLayers(this)
    Player.initPlayer(this)
    Player.placeAt(GreenGrave.rooms[0], this)
    return true
  }

  override fun onUserUpdate(elapsedTime: Float): Boolean {
    if (getKey(Key.SHIFT).bPressed) {
      currentRoomIndex = (currentRoomIndex + 1) % GreenGrave.rooms.size
      Player.placeAt(GreenGrave.rooms[currentRoomIndex], this)
    }

    // Clear all layers
    LayersMap.entries.forEach { (_, target) ->
      setDrawTarget(target)
      clear(Pixel.BLANK)
    }

    // Draw room (populates INTERACTABLE layer with collision sprites)
    GreenGrave.rooms[currentRoomIndex].draw(this)

    // Update player (reads INTERACTABLE layer for collision)
    Player.update(this, elapsedTime)

    // Draw player
    Player.drawPlayer(this)

    // Debug overlay
    setDrawTarget(LayersMap[Layer.OVERLAY_DEBUG])
    drawString(0, 0, GreenGrave.rooms[currentRoomIndex].name)
    return true
  }
}

fun main() {
  val game = Vermis()
  if (game.construct(
      screen_w = 640,
      screen_h = 480,
      pixel_w = 2,
      pixel_h = 2,
    ) == RetCode.OK
  ) game.start()
}
