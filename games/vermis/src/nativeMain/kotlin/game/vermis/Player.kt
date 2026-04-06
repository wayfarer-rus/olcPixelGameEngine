package game.vermis

import geometry_2d.BoundingBox
import geometry_2d.collidesWithSprite
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d
import olc.game_engine.toVi2d

object Player {

  private lateinit var playerSprite: Sprite
  var pos: Vf2d = Vf2d(0f, 0f)
  private const val SPEED = 100f

  fun initPlayer(e: PixelGameEngine) {
    this.playerSprite = Sprite(TILE_SIZE)
    e.setDrawTarget(playerSprite)
    e.clear(Pixel.BLANK)
    e.fillCircle(Vi2d(TILE_SIZE.x / 2, TILE_SIZE.y / 2), TILE_SIZE.x / 2, Pixel.WHITE)
  }

  fun placeAt(room: Room, e: PixelGameEngine) {
    val offset = room.screenOffset(e)
    for (row in room.roomLayout.indices) {
      for (col in room.roomLayout[row].indices) {
        if (room.roomLayout[row][col] == 'X') {
          pos = Vf2d(
            (offset.x + col * TILE_SIZE.x).toFloat(),
            (offset.y + row * TILE_SIZE.y).toFloat()
          )
          return
        }
      }
    }
    // Fallback: center of room
    pos = Vf2d(
      (offset.x + room.roomLayout[0].size * TILE_SIZE.x / 2).toFloat(),
      (offset.y + room.roomLayout.size * TILE_SIZE.y / 2).toFloat()
    )
  }

  fun update(e: PixelGameEngine, elapsedTime: Float) {
    val collisionSurface = LayersMap.sprite(Layer.INTERACTABLE)
    var dx = 0f
    var dy = 0f
    if (e.getKey(Key.W).bHeld) dy -= SPEED * elapsedTime
    if (e.getKey(Key.S).bHeld) dy += SPEED * elapsedTime
    if (e.getKey(Key.A).bHeld) dx -= SPEED * elapsedTime
    if (e.getKey(Key.D).bHeld) dx += SPEED * elapsedTime

    // Try X and Y independently (wall-sliding)
    val tryX = Vf2d(pos.x + dx, pos.y)
    if (!collidesWithSprite(playerRect(tryX), collisionSurface)) pos = tryX

    val tryY = Vf2d(pos.x, pos.y + dy)
    if (!collidesWithSprite(playerRect(tryY), collisionSurface)) pos = tryY
  }

  fun drawPlayer(e: PixelGameEngine) {
    e.setDrawTarget(LayersMap[Layer.PLAYER])
    e.drawSprite(pos.toVi2d(), playerSprite)
  }

  private fun playerRect(p: Vf2d) =
    BoundingBox(p.x, p.y, TILE_SIZE.x.toFloat(), TILE_SIZE.y.toFloat())
}
