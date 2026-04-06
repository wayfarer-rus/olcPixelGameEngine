package game.vermis

import geometry_2d.BoundingBox
import geometry_2d.collidesWithSprite
import olc.game_engine.Decal
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d
import olc.game_engine.rcode
import game.vermis.data.rooms.localPathToAbsolute

enum class Direction { DOWN, UP, LEFT, RIGHT }

/**
 * Animation row mapping for the player sprite sheet.
 * Grid: 48x64 per cell, 10 cols x 31 rows (480x1984 sheet).
 * Each direction maps to a sheet row index and frame count.
 */
data class AnimRow(val row: Int, val frames: Int)

object Player {

  private lateinit var spriteSheet: Sprite
  private lateinit var spriteDecal: Decal

  // Sprite sheet layout: 48x64 grid cells, sprites ~32px wide centered in each cell
  private const val FRAME_W = 48
  private const val FRAME_H = 64
  private const val SPRITE_PATH = "resources/sprites/player.png"

  // Scale down to fit within tile height: 64 * 0.5 = 32 (= TILE_SIZE.y)
  private const val DRAW_SCALE = 0.5f
  private const val DRAW_W = (FRAME_W * DRAW_SCALE).toInt()   // 24
  private const val DRAW_H = (FRAME_H * DRAW_SCALE).toInt()   // 32

  // Animation rows per direction (row index in sheet, number of frames)
  private val walkAnims = mapOf(
    Direction.DOWN  to AnimRow(row = 0, frames = 6),
    Direction.UP    to AnimRow(row = 2, frames = 6),
    Direction.RIGHT to AnimRow(row = 4, frames = 8),
    Direction.LEFT  to AnimRow(row = 6, frames = 8),
  )

  // Collision bounding box size (character's feet area)
  private const val BBOX_W = 16f
  private const val BBOX_H = 12f

  // Offset from collision box top-left to sprite draw position
  // Drawn sprite is DRAW_W x DRAW_H (24x32), centered horizontally on bbox, feet at bottom
  private val spriteOffset = Vf2d(
    (BBOX_W - DRAW_W) / 2,   // center sprite horizontally on bbox
    BBOX_H - DRAW_H.toFloat() // sprite extends above bbox
  )

  // State
  var pos: Vf2d = Vf2d(0f, 0f)          // collision box position
  private var facing: Direction = Direction.DOWN
  private var animFrame: Int = 0
  private var animTimer: Float = 0f
  private var moving: Boolean = false
  private const val ANIM_SPEED = 0.12f    // seconds per frame
  private const val SPEED = 100f          // pixels per second

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
          // Center the collision box in the tile
          pos = Vf2d(
            offset.x + col * TILE_SIZE.x + (TILE_SIZE.x - BBOX_W) / 2,
            offset.y + row * TILE_SIZE.y + (TILE_SIZE.y - BBOX_H) / 2
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

  fun update(e: PixelGameEngine, room: Room, elapsedTime: Float) {
    val collisionSurface = LayersMap.sprite(Layer.INTERACTABLE)
    val offset = room.screenOffset(e)
    val roomBounds = BoundingBox(
      offset.x.toFloat(), offset.y.toFloat(),
      (room.roomLayout[0].size * TILE_SIZE.x).toFloat(),
      (room.roomLayout.size * TILE_SIZE.y).toFloat()
    )

    var dx = 0f
    var dy = 0f
    if (e.getKey(Key.W).bHeld) { dy -= SPEED * elapsedTime; facing = Direction.UP }
    if (e.getKey(Key.S).bHeld) { dy += SPEED * elapsedTime; facing = Direction.DOWN }
    if (e.getKey(Key.A).bHeld) { dx -= SPEED * elapsedTime; facing = Direction.LEFT }
    if (e.getKey(Key.D).bHeld) { dx += SPEED * elapsedTime; facing = Direction.RIGHT }

    moving = dx != 0f || dy != 0f

    // Try X and Y independently (wall-sliding)
    val tryX = Vf2d(pos.x + dx, pos.y)
    if (isInsideRoom(playerBBox(tryX), roomBounds) &&
      !collidesWithSprite(playerBBox(tryX), collisionSurface)) pos = tryX

    val tryY = Vf2d(pos.x, pos.y + dy)
    if (isInsideRoom(playerBBox(tryY), roomBounds) &&
      !collidesWithSprite(playerBBox(tryY), collisionSurface)) pos = tryY

    // Animate
    if (moving) {
      animTimer += elapsedTime
      if (animTimer >= ANIM_SPEED) {
        animTimer -= ANIM_SPEED
        val anim = walkAnims[facing]!!
        animFrame = (animFrame + 1) % anim.frames
      }
    } else {
      animFrame = 0
      animTimer = 0f
    }
  }

  fun drawPlayer(e: PixelGameEngine) {
    e.setDrawTarget(LayersMap[Layer.PLAYER])

    val anim = walkAnims[facing]!!
    val srcX = animFrame * FRAME_W
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

  private fun playerBBox(p: Vf2d) = BoundingBox(p.x, p.y, BBOX_W, BBOX_H)

  private fun isInsideRoom(player: BoundingBox, room: BoundingBox): Boolean =
    player.left >= room.left && player.right <= room.right &&
      player.top >= room.top && player.bottom <= room.bottom
}
