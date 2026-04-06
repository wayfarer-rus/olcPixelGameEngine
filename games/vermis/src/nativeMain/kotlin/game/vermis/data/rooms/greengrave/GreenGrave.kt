package game.vermis.data.rooms.greengrave

import game.vermis.Drawable

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

  val rooms: List<Drawable> = listOf(IsolatedCrypt, Exterior, RootCave, BuriedShrine, PathSouth)

  val roomNames: List<String> = listOf(
    IsolatedCrypt.name,
    Exterior.name,
    RootCave.name,
    BuriedShrine.name,
    PathSouth.name,
  )

  val connections: List<Connection> = listOf(
    Connection("isolated_crypt", "exterior", Direction.E),
    Connection("exterior", "root_cave", Direction.N),
    Connection("exterior", "buried_shrine", Direction.W, secret = true),
    Connection("exterior", "path_south", Direction.S),
  )
}
