package game.vermis

import game.vermis.data.rooms.DoorwayAxis
import game.vermis.data.rooms.RoomsCatalogue
import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.Key
import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.RetCode

class Vermis : PixelGameEngineImpl() {
  override val appName = "Vermis"

  private var currentRoomIndex = "isolated_crypt"
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

    // === CALCULATE ===

    val currentRoom = RoomsCatalogue[currentRoomIndex]

    // Update player movement (reads INTERACTABLE layer from previous frame)
    Player.update(this, elapsedTime)

    // Check for room transition (skip one frame after a transition to prevent bounce-back)
    val doorTile = Player.checkDoorOverlap(currentRoom, this)
    if (doorTile != null) {
      val transition = RoomsCatalogue.resolveTransition(currentRoom, doorTile)
      if (transition != null) {
        val doorwayAxis = when (transition.direction) {
          game.vermis.data.rooms.Direction.E, game.vermis.data.rooms.Direction.W -> DoorwayAxis.VERTICAL
          game.vermis.data.rooms.Direction.N, game.vermis.data.rooms.Direction.S -> DoorwayAxis.HORIZONTAL
        }
        val sourceDoorway = RoomsCatalogue.doorwaySpan(currentRoom, doorTile)?.copy(axis = doorwayAxis)
        val destDoorway = RoomsCatalogue.doorwaySpan(transition.destRoom, transition.destDoor)?.copy(axis = doorwayAxis)

        if (sourceDoorway != null && destDoorway != null && Player.isFullyInsideDoorway(
            currentRoom,
            sourceDoorway,
            this
          )
        ) {
          currentRoomIndex = transition.destRoom.name
          val progress = Player.doorwayProgress(currentRoom, sourceDoorway, this)
          Player.placeAtDoorway(transition.destRoom, destDoorway, transition.direction, progress, this)
        } else if (sourceDoorway == null || destDoorway == null) {
          currentRoomIndex = transition.destRoom.name
          val halfW = Player.BBOX_W / 2
          val halfH = Player.BBOX_H / 2
          val (offsetX, offsetY) = when (transition.direction) {
            game.vermis.data.rooms.Direction.E -> halfW to 0f
            game.vermis.data.rooms.Direction.W -> -(halfW) to 0f
            game.vermis.data.rooms.Direction.S -> 0f to halfH
            game.vermis.data.rooms.Direction.N -> 0f to -(halfH)
          }
          Player.placeAtDoor(transition.destRoom, transition.destDoor, offsetX, offsetY, this)
        }
      }
    }

    // === DRAW ===

    LayersMap.entries.forEach { (_, target) ->
      setDrawTarget(target)
      clear(Pixel.BLANK)
    }

    RoomsCatalogue[currentRoomIndex].draw(this)

    if (playerVisible) {
      Player.drawPlayer(this)
    }

    setDrawTarget(LayersMap[Layer.OVERLAY_DEBUG])
    drawString(0, 0, RoomsCatalogue[currentRoomIndex].name)
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
