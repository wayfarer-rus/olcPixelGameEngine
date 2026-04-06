package game.vermis.data.rooms.greengrave

import game.vermis.Room
import game.vermis.data.rooms.Connection
import game.vermis.data.rooms.Direction
import game.vermis.data.rooms.DoorTile
import olc.game_engine.PixelGameEngine

object GreenGrave {
  val IsolatedCrypt = IsolatedCrypt()
  val Exterior = Exterior()
  val RootCave = RootCave()
  val BuriedShrine = BuriedShrine()
  val PathSouth = PathSouth()

  val rooms: List<Room> = listOf(IsolatedCrypt, Exterior, RootCave, BuriedShrine, PathSouth)

  val connections: List<Connection> = listOf(
    Connection("isolated_crypt", DoorTile(8, 5), "exterior", DoorTile(0, 9), Direction.E),
    Connection("exterior", DoorTile(11, 0), "root_cave", DoorTile(9, 10), Direction.N),
    Connection("exterior", DoorTile(0, 6), "buried_shrine", DoorTile(13, 7), Direction.W, secret = true),
    Connection("exterior", DoorTile(10, 10), "path_south", DoorTile(10, 0), Direction.S),
  )

  fun initRooms(e: PixelGameEngine) {
    rooms.forEach { room -> room.initRoom(e) }
  }
}
