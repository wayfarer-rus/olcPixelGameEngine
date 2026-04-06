package game.vermis

import game.vermis.data.rooms.RoomsCatalogue
import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode

class Vermis : PixelGameEngineImpl() {
  override val appName = "Vermis"

  private var currentRoomIndex = 0
  private var playerVisible = true

  override fun onUserCreate(): Boolean {
    LayersMap.initLayers(this)
    RoomsCatalogue.initRooms(this)
    Player.initPlayer(this)
    Player.placeAt(GreenGrave.rooms[0], this)
    return true
  }

  override fun onUserUpdate(elapsedTime: Float): Boolean {
    if (getKey(Key.X).bPressed) {
      playerVisible = !playerVisible
    }

    // Clear all layers
    LayersMap.entries.forEach { (_, target) ->
      setDrawTarget(target)
      clear(Pixel.BLANK)
    }

    // Draw room (populates INTERACTABLE layer with collision sprites)
    GreenGrave.rooms[currentRoomIndex].draw(this)

    if (playerVisible) {
      // Update player (reads INTERACTABLE layer for collision, clamped to room bounds)
      Player.update(this, GreenGrave.rooms[currentRoomIndex], elapsedTime)

      // Draw player
      Player.drawPlayer(this)
    }

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
