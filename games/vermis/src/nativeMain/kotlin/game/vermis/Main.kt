package game.vermis

import game.vermis.data.rooms.greengrave.IsolatedCrypt
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode

class Vermis : PixelGameEngineImpl() {
  override val appName = "Vermis"

  private lateinit var isolatedCrypt: IsolatedCrypt

  override fun onUserCreate(): Boolean {
    LayersMap.initLayers(this)

    isolatedCrypt = IsolatedCrypt()
    return true
  }

  override fun onUserUpdate(elapsedTime: Float): Boolean {
    clear(Pixel.BLACK)

    isolatedCrypt.draw(this)
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
