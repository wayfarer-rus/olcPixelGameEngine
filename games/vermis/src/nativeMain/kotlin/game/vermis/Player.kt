package game.vermis

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite
import olc.game_engine.Vi2d

object Player {

  private lateinit var playerSprite: Sprite

  fun initPlayer(e: PixelGameEngine) {
    this.playerSprite = Sprite(TILE_SIZE)
    e.setDrawTarget(playerSprite)
    e.clear(Pixel.BLANK)
    e.fillCircle(Vi2d(TILE_SIZE.x / 2, TILE_SIZE.y / 2), TILE_SIZE.x / 2, Pixel.WHITE)
  }

  fun drawPlayer(e: PixelGameEngine, pos: Vi2d) {
    e.setDrawTarget(LayersMap[Layer.PLAYER])
    e.drawSprite(pos, playerSprite)
  }

}
