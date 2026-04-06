package game.vermis.data.rooms.greengrave

import game.vermis.Room
import olc.game_engine.PixelGameEngine

enum class Direction { N, S, E, W }

data class Connection(
  val from: String,
  val to: String,
  val direction: Direction,
  val secret: Boolean = false,
)

object GreenGrave {
  val IsolatedCrypt = IsolatedCrypt()
  val Exterior = Exterior()
  val RootCave = RootCave()
  val BuriedShrine = BuriedShrine()
  val PathSouth = PathSouth()

  val rooms: List<Room> = listOf(IsolatedCrypt, Exterior, RootCave, BuriedShrine, PathSouth)

  val connections: List<Connection> = listOf(
    Connection("isolated_crypt", "exterior", Direction.E),
    Connection("exterior", "root_cave", Direction.N),
    Connection("exterior", "buried_shrine", Direction.W, secret = true),
    Connection("exterior", "path_south", Direction.S),
  )

  fun initRooms(e: PixelGameEngine) {
    rooms.forEach { room -> room.initRoom(e) }
  }
}
