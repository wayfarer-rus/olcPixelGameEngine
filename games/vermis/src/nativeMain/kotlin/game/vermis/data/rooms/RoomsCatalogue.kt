package game.vermis.data.rooms

import game.vermis.Room
import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.PixelGameEngine

enum class Direction { N, S, E, W }

data class DoorTile(val col: Int, val row: Int)

data class Connection(
  val from: String,
  val fromDoor: DoorTile,
  val to: String,
  val toDoor: DoorTile,
  val direction: Direction,
  val secret: Boolean = false,
)

/**
 * Result of resolving a door transition: the destination room, the door tile in that room,
 * and the direction of travel.
 */
data class TransitionResult(
  val destRoom: Room,
  val destDoor: DoorTile,
  val direction: Direction,
)

object RoomsCatalogue {

  private val allRooms: List<Room> by lazy { GreenGrave.rooms }
  private val allConnections: List<Connection> by lazy { GreenGrave.connections }

  fun initRooms(e: PixelGameEngine) {
    GreenGrave.initRooms(e)
  }

  fun roomByName(name: String): Room? = allRooms.find { it.name == name }

  /**
   * Given a room and a door tile the player is standing on,
   * resolve the destination room and its corresponding door tile.
   */
  fun resolveTransition(room: Room, door: DoorTile): TransitionResult? {
    for (conn in allConnections) {
      if (conn.from == room.name && conn.fromDoor == door) {
        val dest = roomByName(conn.to) ?: continue
        return TransitionResult(dest, conn.toDoor, conn.direction)
      }
      if (conn.to == room.name && conn.toDoor == door) {
        val dest = roomByName(conn.from) ?: continue
        val reverseDir = when (conn.direction) {
          Direction.N -> Direction.S
          Direction.S -> Direction.N
          Direction.E -> Direction.W
          Direction.W -> Direction.E
        }
        return TransitionResult(dest, conn.fromDoor, reverseDir)
      }
    }
    return null
  }
}
