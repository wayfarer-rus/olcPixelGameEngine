package game.vermis

import game.vermis.data.rooms.DoorTile
import game.vermis.data.rooms.localPathToAbsolute
import geometry_2d.BoundingBox
import geometry_2d.collidesWithSprite
import olc.game_engine.Decal
import olc.game_engine.Key
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite
import olc.game_engine.Vf2d
import olc.game_engine.rcode

enum class PlayerDirection { DOWN, UP, LEFT, RIGHT }

data class AnimRow(val row: Int, val frames: Int)

object Player {

  private lateinit var spriteSheet: Sprite
  private lateinit var spriteDecal: Decal

  private const val FRAME_W = 48
  private const val FRAME_H = 64
  private const val SPRITE_PATH = "resources/sprites/player.png"

  private const val DRAW_SCALE = 0.7f
  private const val DRAW_W = (FRAME_W * DRAW_SCALE).toInt()
  private const val DRAW_H = (FRAME_H * DRAW_SCALE).toInt()

  private val idleAnims = mapOf(
    PlayerDirection.DOWN  to AnimRow(row = 0, frames = 6),
    PlayerDirection.UP    to AnimRow(row = 1, frames = 6),
    PlayerDirection.LEFT  to AnimRow(row = 2, frames = 6),
    PlayerDirection.RIGHT to AnimRow(row = 3, frames = 6),
  )
  private val walkAnims = mapOf(
    PlayerDirection.DOWN  to AnimRow(row = 4, frames = 8),
    PlayerDirection.UP    to AnimRow(row = 5, frames = 8),
    PlayerDirection.LEFT  to AnimRow(row = 6, frames = 8),
    PlayerDirection.RIGHT to AnimRow(row = 7, frames = 8),
  )

  const val BBOX_W = 16f
  const val BBOX_H = 20f

  private val spriteOffset = Vf2d(
    (BBOX_W - DRAW_W) / 2,
    BBOX_H - DRAW_H.toFloat()
  )

  var pos: Vf2d = Vf2d(0f, 0f)
  private var facing: PlayerDirection = PlayerDirection.DOWN
  private var animFrame: Long = 0
  private var animTimer: Float = 0f
  private var moving: Boolean = false
  private const val ANIM_SPEED = 0.12f
  private const val SPEED = 100f

  fun initPlayer(e: PixelGameEngine) {
    spriteSheet = Sprite()
    if (spriteSheet.loadFromFile(localPathToAbsolute(SPRITE_PATH)) != rcode.OK) {
      throw IllegalStateException("Failed to load player sprite sheet: $SPRITE_PATH")
    }
    spriteDecal = e.createDecal(spriteSheet)
  }

  fun placeAt(room: Room, e: PixelGameEngine) {
    val offset = room.screenOffset(e)
    for (row in room.roomLayout.indices) {
      for (col in room.roomLayout[row].indices) {
        if (room.roomLayout[row][col] == 'X') {
          pos = Vf2d(
            offset.x + col * TILE_SIZE.x + (TILE_SIZE.x - BBOX_W) / 2,
            offset.y + row * TILE_SIZE.y + (TILE_SIZE.y - BBOX_H) / 2
          )
          return
        }
      }
    }
    pos = Vf2d(
      (offset.x + room.roomLayout[0].size * TILE_SIZE.x / 2).toFloat(),
      (offset.y + room.roomLayout.size * TILE_SIZE.y / 2).toFloat()
    )
  }

  fun placeAtDoor(room: Room, door: DoorTile, offsetX: Float, offsetY: Float, e: PixelGameEngine) {
    val roomOffset = room.screenOffset(e)
    val doorX = (roomOffset.x + door.col * TILE_SIZE.x).toFloat()
    val doorY = (roomOffset.y + door.row * TILE_SIZE.y).toFloat()
    pos = Vf2d(
      doorX + (TILE_SIZE.x - BBOX_W) / 2 + offsetX,
      doorY + (TILE_SIZE.y - BBOX_H) / 2 + offsetY
    )
  }

  fun update(e: PixelGameEngine, elapsedTime: Float) {
    val collisionSurface = LayersMap.sprite(Layer.INTERACTABLE)

    var dx = 0f
    var dy = 0f
    if (e.getKey(Key.W).bHeld) { dy -= SPEED * elapsedTime; facing = PlayerDirection.UP }
    if (e.getKey(Key.S).bHeld) { dy += SPEED * elapsedTime; facing = PlayerDirection.DOWN }
    if (e.getKey(Key.A).bHeld) { dx -= SPEED * elapsedTime; facing = PlayerDirection.LEFT }
    if (e.getKey(Key.D).bHeld) { dx += SPEED * elapsedTime; facing = PlayerDirection.RIGHT }

    moving = dx != 0f || dy != 0f

    val tryX = Vf2d(pos.x + dx, pos.y)
    if (!collidesWithSprite(playerBBox(tryX), collisionSurface)) pos = tryX

    val tryY = Vf2d(pos.x, pos.y + dy)
    if (!collidesWithSprite(playerBBox(tryY), collisionSurface)) pos = tryY

    animTimer += elapsedTime
    if (animTimer >= ANIM_SPEED) {
      animTimer -= ANIM_SPEED
      animFrame++
    }
  }

  /**
   * Check if the player's bounding box is fully inside a D/E tile.
   * Returns the DoorTile coordinates if so, null otherwise.
   */
  fun checkDoorOverlap(room: Room, e: PixelGameEngine): DoorTile? {
    val offset = room.screenOffset(e)
    val bbox = playerBBox(pos)

    val centerX = (bbox.left + bbox.right) / 2
    val centerY = (bbox.top + bbox.bottom) / 2
    val tileCol = ((centerX - offset.x) / TILE_SIZE.x).toInt()
    val tileRow = ((centerY - offset.y) / TILE_SIZE.y).toInt()

    val c = room.roomLayout.getOrNull(tileRow)?.getOrNull(tileCol) ?: return null
    if (c != 'D' && c != 'E') return null

    // Check bbox is fully within this tile
    val tileLeft = offset.x + tileCol * TILE_SIZE.x
    val tileTop = offset.y + tileRow * TILE_SIZE.y
    if (bbox.left < tileLeft || bbox.right > tileLeft + TILE_SIZE.x ||
      bbox.top < tileTop || bbox.bottom > tileTop + TILE_SIZE.y) return null

    return DoorTile(tileCol, tileRow)
  }

  fun drawPlayer(e: PixelGameEngine) {
    e.setDrawTarget(LayersMap[Layer.PLAYER])

    val anim = currentAnim()
    val srcX = (animFrame % anim.frames).toInt() * FRAME_W
    val srcY = anim.row * FRAME_H

    val drawPos = Vf2d(pos.x + spriteOffset.x, pos.y + spriteOffset.y)
    val scale = Vf2d(DRAW_SCALE, DRAW_SCALE)

    e.drawPartialDecal(
      drawPos, spriteDecal,
      Vf2d(srcX.toFloat(), srcY.toFloat()),
      Vf2d(FRAME_W.toFloat(), FRAME_H.toFloat()),
      scale
    )
  }

  private fun currentAnim(): AnimRow =
    if (moving) walkAnims[facing]!! else idleAnims[facing]!!

  private fun playerBBox(p: Vf2d) = BoundingBox(p.x, p.y, BBOX_W, BBOX_H)
}
