package game.vermis

import game.vermis.data.rooms.DoorTile
import game.vermis.data.rooms.DoorwayAxis
import game.vermis.data.rooms.DoorwaySpan
import game.vermis.data.rooms.localPathToAbsolute
import geometry_2d.BoundingBox
import geometry_2d.collidesWithSprite
import olc.game_engine.*

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

  fun doorwayProgress(room: Room, doorway: DoorwaySpan, e: PixelGameEngine): Float {
    val roomOffset = room.screenOffset(e)
    val bbox = playerBBox(pos)
    val centerX = bbox.left + bbox.w / 2f
    val centerY = bbox.top + bbox.h / 2f

    return when (doorway.axis) {
      DoorwayAxis.HORIZONTAL -> {
        val start = roomOffset.x + doorway.tiles.first().col * TILE_SIZE.x
        val end = roomOffset.x + (doorway.tiles.last().col + 1) * TILE_SIZE.x
        ((centerX - start) / (end - start)).coerceIn(0f, 1f)
      }

      DoorwayAxis.VERTICAL -> {
        val start = roomOffset.y + doorway.tiles.first().row * TILE_SIZE.y
        val end = roomOffset.y + (doorway.tiles.last().row + 1) * TILE_SIZE.y
        ((centerY - start) / (end - start)).coerceIn(0f, 1f)
      }
    }
  }

  fun isFullyInsideDoorway(room: Room, doorway: DoorwaySpan, e: PixelGameEngine): Boolean {
    val roomOffset = room.screenOffset(e)
    val bbox = playerBBox(pos)

    return when (doorway.axis) {
      DoorwayAxis.HORIZONTAL -> {
        val left = roomOffset.x + doorway.tiles.first().col * TILE_SIZE.x
        val right = roomOffset.x + (doorway.tiles.last().col + 1) * TILE_SIZE.x
        val top = roomOffset.y + doorway.tiles.first().row * TILE_SIZE.y
        val bottom = top + TILE_SIZE.y
        bbox.left >= left && bbox.right <= right && bbox.top >= top && bbox.bottom <= bottom
      }

      DoorwayAxis.VERTICAL -> {
        val left = roomOffset.x + doorway.tiles.first().col * TILE_SIZE.x
        val right = left + TILE_SIZE.x
        val top = roomOffset.y + doorway.tiles.first().row * TILE_SIZE.y
        val bottom = roomOffset.y + (doorway.tiles.last().row + 1) * TILE_SIZE.y
        bbox.left >= left && bbox.right <= right && bbox.top >= top && bbox.bottom <= bottom
      }
    }
  }

  fun placeAtDoorway(
    room: Room,
    doorway: DoorwaySpan,
    direction: game.vermis.data.rooms.Direction,
    progress: Float,
    e: PixelGameEngine
  ) {
    val roomOffset = room.screenOffset(e)
    val clampedProgress = progress.coerceIn(0f, 1f)

    pos = when (doorway.axis) {
      DoorwayAxis.HORIZONTAL -> {
        val startX = roomOffset.x + doorway.tiles.first().col * TILE_SIZE.x
        val endX = roomOffset.x + (doorway.tiles.last().col + 1) * TILE_SIZE.x
        val centerX = when {
          endX - startX <= BBOX_W -> (startX + endX) / 2f
          else -> {
            val minCenterX = startX + BBOX_W / 2f
            val maxCenterX = endX - BBOX_W / 2f
            minCenterX + (maxCenterX - minCenterX) * clampedProgress
          }
        }
        val row = doorway.tiles.first().row
        val spawnY = when (direction) {
          game.vermis.data.rooms.Direction.S ->
            (roomOffset.y + (row + 1) * TILE_SIZE.y - BBOX_H / 2f).toFloat()

          game.vermis.data.rooms.Direction.N ->
            (roomOffset.y + row * TILE_SIZE.y - BBOX_H / 2f).toFloat()

          else ->
            (roomOffset.y + row * TILE_SIZE.y + (TILE_SIZE.y - BBOX_H) / 2).toFloat()
        }
        Vf2d(centerX - BBOX_W / 2f, spawnY)
      }

      DoorwayAxis.VERTICAL -> {
        val startY = roomOffset.y + doorway.tiles.first().row * TILE_SIZE.y
        val endY = roomOffset.y + (doorway.tiles.last().row + 1) * TILE_SIZE.y
        val centerY = when {
          endY - startY <= BBOX_H -> (startY + endY) / 2f
          else -> {
            val minCenterY = startY + BBOX_H / 2f
            val maxCenterY = endY - BBOX_H / 2f
            minCenterY + (maxCenterY - minCenterY) * clampedProgress
          }
        }
        val col = doorway.tiles.first().col
        val spawnX = when (direction) {
          game.vermis.data.rooms.Direction.E ->
            (roomOffset.x + (col + 1) * TILE_SIZE.x - BBOX_W / 2f).toFloat()

          game.vermis.data.rooms.Direction.W ->
            (roomOffset.x + col * TILE_SIZE.x - BBOX_W / 2f).toFloat()

          else ->
            (roomOffset.x + col * TILE_SIZE.x + (TILE_SIZE.x - BBOX_W) / 2).toFloat()
        }
        Vf2d(spawnX, centerY - BBOX_H / 2f)
      }
    }
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
   * Check if the player's inner trigger box overlaps one or more D/E tiles.
   * Returns the door tile closest to the player's center, or null otherwise.
   */
  fun checkDoorOverlap(room: Room, e: PixelGameEngine): DoorTile? {
    val offset = room.screenOffset(e)
    val bbox = playerBBox(pos)
    val triggerInset = 1f
    val sampleLeft = bbox.left + triggerInset
    val sampleTop = bbox.top + triggerInset
    val sampleRight = bbox.right - triggerInset
    val sampleBottom = bbox.bottom - triggerInset

    val leftTileCol = ((sampleLeft - offset.x) / TILE_SIZE.x).toInt()
    val rightTileCol = ((sampleRight - offset.x) / TILE_SIZE.x).toInt()
    val topTileRow = ((sampleTop - offset.y) / TILE_SIZE.y).toInt()
    val bottomTileRow = ((sampleBottom - offset.y) / TILE_SIZE.y).toInt()
    val centerX = (sampleLeft + sampleRight) / 2f
    val centerY = (sampleTop + sampleBottom) / 2f

    val doorTiles = mutableListOf<DoorTile>()
    for (row in topTileRow..bottomTileRow) {
      for (col in leftTileCol..rightTileCol) {
        val c = room.roomLayout.getOrNull(row)?.getOrNull(col) ?: continue
        if (c == 'D' || c == 'E') {
          doorTiles += DoorTile(col, row)
        }
      }
    }

    if (doorTiles.isEmpty()) return null

    return doorTiles.minByOrNull { door ->
      val tileCenterX = offset.x + door.col * TILE_SIZE.x + TILE_SIZE.x / 2f
      val tileCenterY = offset.y + door.row * TILE_SIZE.y + TILE_SIZE.y / 2f
      val dx = centerX - tileCenterX
      val dy = centerY - tileCenterY
      dx * dx + dy * dy
    }
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
