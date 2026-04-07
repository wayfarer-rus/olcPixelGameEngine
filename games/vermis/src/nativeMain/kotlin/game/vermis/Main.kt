package game.vermis

import game.vermis.data.rooms.Direction
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
  private var transitionCooldown = false

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

    // === CALCULATE ===

    val currentRoom = GreenGrave.rooms[currentRoomIndex]

    // Update player movement (reads INTERACTABLE layer from previous frame)
    Player.update(this, elapsedTime)

    // Check for room transition (skip one frame after a transition to prevent bounce-back)
    val doorTile = Player.checkDoorOverlap(currentRoom, this)
    if (doorTile != null && !transitionCooldown) {
      val transition = RoomsCatalogue.resolveTransition(currentRoom, doorTile)
      if (transition != null) {
        currentRoomIndex = GreenGrave.rooms.indexOf(transition.destRoom)

        val halfW = Player.BBOX_W / 2
        val halfH = Player.BBOX_H / 2
        val (offsetX, offsetY) = when (transition.direction) {
          Direction.E -> halfW + TILE_SIZE.x to 0f
          Direction.W -> -(halfW + TILE_SIZE.x) to 0f
          Direction.S -> 0f to halfH + TILE_SIZE.y
          Direction.N -> 0f to -(halfH + TILE_SIZE.y)
        }

        Player.placeAtDoor(transition.destRoom, transition.destDoor, offsetX, offsetY, this)
        transitionCooldown = true
      }
    } else if (doorTile == null) {
      transitionCooldown = false
    }

    // === DRAW ===

    LayersMap.entries.forEach { (_, target) ->
      setDrawTarget(target)
      clear(Pixel.BLANK)
    }

    GreenGrave.rooms[currentRoomIndex].draw(this)

    if (playerVisible) {
      Player.drawPlayer(this)
    }

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
