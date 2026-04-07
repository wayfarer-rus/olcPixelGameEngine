package game.vermis.data.rooms

import game.vermis.Room
import game.vermis.data.rooms.greengrave.GreenGrave
import olc.game_engine.PixelGameEngine

enum class Direction { N, S, E, W }

data class DoorTile(val col: Int, val row: Int)

enum class DoorwayAxis { HORIZONTAL, VERTICAL }

data class DoorwaySpan(
    val axis: DoorwayAxis,
    val tiles: List<DoorTile>,
)

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

    private val rooms: Map<String, Room> by lazy { allRooms.associateBy { it.name } }

    operator fun get(name: String): Room = rooms[name] ?: error("No room with name $name")

    fun initRooms(e: PixelGameEngine) {
        GreenGrave.initRooms(e)
    }

    fun roomByName(name: String): Room? = rooms[name]

    fun doorwaySpan(room: Room, tile: DoorTile): DoorwaySpan? {
        if (!isDoorTile(room, tile)) return null

        val connectedTiles = mutableSetOf<DoorTile>()
        val queue = ArrayDeque<DoorTile>()
        queue.add(tile)
        connectedTiles.add(tile)

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            for (neighbor in doorwayNeighbors(current)) {
                if (neighbor !in connectedTiles && isDoorTile(room, neighbor)) {
                    connectedTiles.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }

        val rows = connectedTiles.map { it.row }.distinct()
        val cols = connectedTiles.map { it.col }.distinct()
        val axis = if (rows.size == 1) DoorwayAxis.HORIZONTAL else DoorwayAxis.VERTICAL
        val sortedTiles = when (axis) {
            DoorwayAxis.HORIZONTAL -> connectedTiles.sortedBy { it.col }
            DoorwayAxis.VERTICAL -> connectedTiles.sortedBy { it.row }
        }
        return DoorwaySpan(axis, sortedTiles)
    }

    /**
     * Given a room and a door tile the player is standing on,
     * resolve the destination room and its corresponding door tile.
     */
    fun resolveTransition(room: Room, door: DoorTile): TransitionResult? {
        for (conn in allConnections) {
            if (conn.from == room.name && sameDoorway(room, conn.fromDoor, door)) {
                val dest = roomByName(conn.to) ?: continue
                return TransitionResult(dest, conn.toDoor, conn.direction)
            }
            if (conn.to == room.name && sameDoorway(room, conn.toDoor, door)) {
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

    private fun sameDoorway(room: Room, anchor: DoorTile, candidate: DoorTile): Boolean {
        val span = doorwaySpan(room, anchor) ?: return false
        return candidate in span.tiles
    }

    private fun doorwayNeighbors(tile: DoorTile): List<DoorTile> = listOf(
        DoorTile(tile.col - 1, tile.row),
        DoorTile(tile.col + 1, tile.row),
        DoorTile(tile.col, tile.row - 1),
        DoorTile(tile.col, tile.row + 1),
    )

    private fun isDoorTile(room: Room, tile: DoorTile): Boolean {
        val c = room.roomLayout.getOrNull(tile.row)?.getOrNull(tile.col) ?: return false
        return c == 'D' || c == 'E'
    }
}
