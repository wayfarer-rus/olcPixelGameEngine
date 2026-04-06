package game.vermis

import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode
import olc.game_engine.Vi2d

class Vermis : PixelGameEngineImpl() {
  override val appName = "Vermis"

  private var currentRoomIndex = 0

  override fun onUserCreate(): Boolean {
    LayersMap.initLayers(this)
    Player.initPlayer(this)
    return true
  }

  override fun onUserUpdate(elapsedTime: Float): Boolean {
    if (getKey(Key.SHIFT).bPressed) {
      currentRoomIndex = (currentRoomIndex + 1) % GreenGrave.rooms.size
    }

    LayersMap.entries.forEach { (layer, target) ->
      setDrawTarget(target)
      clear(Pixel.BLANK)
    }

    GreenGrave.rooms[currentRoomIndex].draw(this)

    // draw player's sprite
    Player.drawPlayer(this, Vi2d(100, 100))

    setDrawTarget(LayersMap[Layer.OVERLAY_DEBUG])
    drawString(0, 0, GreenGrave.roomNames[currentRoomIndex])
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
