package sample

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import olc.game_engine.*

@ExperimentalUnsignedTypes
class Renderable(
    sFile: String,
    private val pge: PixelGameEngine
) {
    val sprite: Sprite = Sprite(sFile)
    val decal: Decal = pge.createDecal(sprite)

    fun close() {
        pge.deleteDecal(decal)
    }

    companion object {
        fun load(file: String, pge: PixelGameEngine): Renderable {
            return Renderable(file, pge)
        }
    }
}

data class Vec3d(
    var x: Float,
    var y: Float,
    var z: Float
)

data class Quad(
    val points: Array<Vec3d>,
    val title: Vf2d
)

data class Cell(
    var wall: Boolean = false,
    val id: Array<Vi2d> = Array(6) { Vi2d(0, 0) }
)

class World {
    var size: Vi2d = Vi2d(0, 0)

    private val nullCell: Cell = Cell()
    private val vCells: MutableList<Cell> = mutableListOf()

    fun create(w: Int, h: Int) {
        size = Vi2d(w, h)
        vCells.addAll(List(w * h) { Cell() })
    }

    fun getCell(v: Vi2d): Cell {
        return if (v.x >= 0 && v.x < size.x && v.y >= 0 && v.y < size.y)
            vCells[v.y * size.x + v.x]
        else
            nullCell
    }
}

@ExperimentalUnsignedTypes
class OlcDungeon : PixelGameEngineImpl() {
    override val appName: String = "Dungeon Explorer"

    private val world = World()
    private var rendSelect: Renderable? = null
    private var rendAllWalls: Renderable? = null

    private var cameraPos = Vf2d(0.0f, 0.0f)
    private var cameraAngle = 0.0f
    private var cameraAngleTarget = cameraAngle
    private var cameraPitch = 5.5f
    private var cameraZoom = 16.0f

    private var visible = Array(6) { false }

    private var cursor = Vi2d(0, 0)
    private var tileCursor = Vi2d(0, 0)
    private var tileSize = Vi2d(32, 32)

    enum class Face {
        Floor,
        North,
        East,
        South,
        West,
        Top
    }

    override fun onUserDestroy() {
        rendSelect?.close()
        rendAllWalls?.close()
    }

    override fun onUserCreate(): Boolean {
        rendSelect = Renderable.load("DungeonWarping/gfx/dng_select.png", this)
        rendAllWalls = Renderable.load("DungeonWarping/gfx/oldDungeon.png", this)

        world.create(16, 16)

        for (y in 0 until world.size.y)
            for (x in 0 until world.size.x) {
                world.getCell(Vi2d(x, y)).wall = false
                world.getCell(Vi2d(x, y)).id[Face.Floor.ordinal] = Vi2d(3, 0) * tileSize
                world.getCell(Vi2d(x, y)).id[Face.Top.ordinal] = Vi2d(1, 0) * tileSize
                world.getCell(Vi2d(x, y)).id[Face.North.ordinal] = Vi2d(0, 6) * tileSize
                world.getCell(Vi2d(x, y)).id[Face.South.ordinal] = Vi2d(0, 6) * tileSize
                world.getCell(Vi2d(x, y)).id[Face.West.ordinal] = Vi2d(0, 6) * tileSize
                world.getCell(Vi2d(x, y)).id[Face.East.ordinal] = Vi2d(0, 6) * tileSize
            }
        return true
    }

    private fun createCube(vCell: Vi2d, fAngle: Float, fPitch: Float, fScale: Float, vCamera: Vec3d): Array<Vec3d> {
        // Unit Cube
        val rotCube = Array(8) { Vec3d(0f, 0f, 0f) }
        val worldCube = Array(8) { Vec3d(0f, 0f, 0f) }
        val projCube = Array(8) { Vec3d(0f, 0f, 0f) }
        val unitCube = arrayOf(
            Vec3d(0.0f, 0.0f, 0.0f),
            Vec3d(fScale, 0.0f, 0.0f),
            Vec3d(fScale, -fScale, 0.0f),
            Vec3d(0.0f, -fScale, 0.0f),
            Vec3d(0.0f, 0.0f, fScale),
            Vec3d(fScale, 0.0f, fScale),
            Vec3d(fScale, -fScale, fScale),
            Vec3d(0.0f, -fScale, fScale)
        )

        // Translate Cube in X-Z Plane
        for (i in 0 until 8) {
            unitCube[i].x += (vCell.x * fScale - vCamera.x)
            unitCube[i].y += -vCamera.y
            unitCube[i].z += (vCell.y * fScale - vCamera.z)
        }

        // Rotate Cube in Y-Axis around origin
        var s = kotlin.math.sin(fAngle)
        var c = kotlin.math.cos(fAngle)
        for (i in 0 until 8) {
            rotCube[i].x = unitCube[i].x * c + unitCube[i].z * s
            rotCube[i].y = unitCube[i].y
            rotCube[i].z = unitCube[i].x * -s + unitCube[i].z * c
        }

        // Rotate Cube in X-Axis around origin (tilt slighly overhead)
        s = kotlin.math.sin(fPitch)
        c = kotlin.math.cos(fPitch)
        for (i in 0 until 8) {
            worldCube[i].x = rotCube[i].x
            worldCube[i].y = rotCube[i].y * c - rotCube[i].z * s
            worldCube[i].z = rotCube[i].y * s + rotCube[i].z * c
        }

        // Project Cube Orthographically - Unit Cube Viewport
        //float fLeft = -ScreenWidth() * 0.5f;
        //float fRight = ScreenWidth() * 0.5f;
        //float fTop = ScreenHeight() * 0.5f;
        //float fBottom = -ScreenHeight() * 0.5f;
        //float fNear = 0.1f;
        //float fFar = 100.0f;*/
        //for (int i = 0; i < 8; i++)
        //{
        //	projCube[i].x = (2.0f / (fRight - fLeft)) * worldCube[i].x - ((fRight + fLeft) / (fRight - fLeft));
        //	projCube[i].y = (2.0f / (fTop - fBottom)) * worldCube[i].y - ((fTop + fBottom) / (fTop - fBottom));
        //	projCube[i].z = (2.0f / (fFar - fNear)) * worldCube[i].z - ((fFar + fNear) / (fFar - fNear));
        //  projCube[i].x *= -fRight;
        //  projCube[i].y *= -fTop;
        //  projCube[i].x += fRight;
        //  projCube[i].y += fTop;
        //}

        // Project Cube Orthographically - Full Screen Centered
        for (i in 0 until 8) {
            projCube[i].x = worldCube[i].x + screenWidth() * 0.5f
            projCube[i].y = worldCube[i].y + screenHeight() * 0.5f
            projCube[i].z = worldCube[i].z
        }

        return projCube
    }

    private fun calculateVisibleFaces(cube: Array<Vec3d>) {
        val checkNormalFun: (Int, Int, Int) -> Boolean = { v1, v2, v3 ->
            val a = Vf2d(cube[v1].x, cube[v1].y)
            val b = Vf2d(cube[v2].x, cube[v2].y)
            val c = Vf2d(cube[v3].x, cube[v3].y)
            (b - a).cross(c - a) > 0
        }

        visible[Face.Floor.ordinal] = checkNormalFun(4, 0, 1)
        visible[Face.South.ordinal] = checkNormalFun(3, 0, 1)
        visible[Face.North.ordinal] = checkNormalFun(6, 5, 4)
        visible[Face.East.ordinal] = checkNormalFun(7, 4, 0)
        visible[Face.West.ordinal] = checkNormalFun(2, 1, 5)
        visible[Face.Top.ordinal] = checkNormalFun(7, 3, 2)
    }

    private fun calculateFaceQuads(
        vCell: Vi2d,
        fAngle: Float,
        fPitch: Float,
        fScale: Float,
        vCamera: Vec3d
    ): List<Quad> {
        val render = mutableListOf<Quad>()
        val projCube = createCube(vCell, fAngle, fPitch, fScale, vCamera)
        val cell = world.getCell(vCell)

        val makeFaceFun: (Int, Int, Int, Int, Face) -> Unit = { v1, v2, v3, v4, f ->
            render.add(
                Quad(
                    points = arrayOf(projCube[v1], projCube[v2], projCube[v3], projCube[v4]),
                    title = cell.id[f.ordinal].data
                )
            )
        }

        if (!cell.wall) {
            if (visible[Face.Floor.ordinal]) makeFaceFun(4, 0, 1, 5, Face.Floor)
        } else {
            if (visible[Face.South.ordinal]) makeFaceFun(3, 0, 1, 2, Face.South)
            if (visible[Face.North.ordinal]) makeFaceFun(6, 5, 4, 7, Face.North)
            if (visible[Face.East.ordinal]) makeFaceFun(7, 4, 0, 3, Face.East)
            if (visible[Face.West.ordinal]) makeFaceFun(2, 1, 5, 6, Face.West)
            if (visible[Face.Top.ordinal]) makeFaceFun(7, 3, 2, 6, Face.Top)
        }

        return render
    }

    private fun getFaceQuads(
        vCell: Vi2d,
        fAngle: Float,
        fPitch: Float,
        fScale: Float,
        vCamera: Vec3d,
        render: MutableList<Quad>
    ) {
        val projCube = createCube(vCell, fAngle, fPitch, fScale, vCamera)
        val cell = world.getCell(vCell)

        val makeFaceFun: (Int, Int, Int, Int, Face) -> Unit = { v1, v2, v3, v4, f ->
            render.add(
                Quad(
                    points = arrayOf(projCube[v1], projCube[v2], projCube[v3], projCube[v4]),
                    title = cell.id[f.ordinal].data
                )
            )
        }

        if (!cell.wall) {
            if (visible[Face.Floor.ordinal]) makeFaceFun(4, 0, 1, 5, Face.Floor)
        } else {
            if (visible[Face.South.ordinal]) makeFaceFun(3, 0, 1, 2, Face.South)
            if (visible[Face.North.ordinal]) makeFaceFun(6, 5, 4, 7, Face.North)
            if (visible[Face.East.ordinal]) makeFaceFun(7, 4, 0, 3, Face.East)
            if (visible[Face.West.ordinal]) makeFaceFun(2, 1, 5, 6, Face.West)
            if (visible[Face.Top.ordinal]) makeFaceFun(7, 3, 2, 6, Face.Top)
        }
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        // Grab mouse for convenience
        val vMouse = Vi2d(getMouseX(), getMouseY())

        // Edit mode - Selection from tile sprite sheet
        if (getKey(Key.TAB).bHeld) {
            drawSprite(Vi2d(0, 0), rendAllWalls!!.sprite)
            drawRect(tileCursor * tileSize, tileSize)
            drawCircle(vMouse, 1, Pixel.YELLOW)
            if (getMouse(1).bPressed) tileCursor = vMouse / tileSize
            return true
        }

        // WS keys to tilt camera
        if (getKey(Key.W).bHeld) cameraPitch += 1.0f * elapsedTime
        if (getKey(Key.S).bHeld) cameraPitch -= 1.0f * elapsedTime

        // DA Keys to manually rotate camera
        if (getKey(Key.D).bHeld) cameraAngleTarget += 1.0f * elapsedTime
        if (getKey(Key.A).bHeld) cameraAngleTarget -= 1.0f * elapsedTime

        // QZ Keys to zoom in or out
        if (getKey(Key.Q).bHeld) cameraZoom += 5.0f * elapsedTime
        if (getKey(Key.Z).bHeld) cameraZoom -= 5.0f * elapsedTime

        // Numpad keys used to rotate camera to fixed angles
        if (getKey(Key.NP2).bPressed) cameraAngleTarget = 3.14159f * 0.0f
        if (getKey(Key.NP1).bPressed) cameraAngleTarget = 3.14159f * 0.25f
        if (getKey(Key.NP4).bPressed) cameraAngleTarget = 3.14159f * 0.5f
        if (getKey(Key.NP7).bPressed) cameraAngleTarget = 3.14159f * 0.75f
        if (getKey(Key.NP8).bPressed) cameraAngleTarget = 3.14159f * 1.0f
        if (getKey(Key.NP9).bPressed) cameraAngleTarget = 3.14159f * 1.25f
        if (getKey(Key.NP6).bPressed) cameraAngleTarget = 3.14159f * 1.5f
        if (getKey(Key.NP3).bPressed) cameraAngleTarget = 3.14159f * 1.75f

        // Numeric keys apply selected tile to specific face
        if (getKey(Key.K1).bPressed) world.getCell(cursor).id[Face.North.ordinal] = tileCursor * tileSize
        if (getKey(Key.K2).bPressed) world.getCell(cursor).id[Face.East.ordinal] = tileCursor * tileSize
        if (getKey(Key.K3).bPressed) world.getCell(cursor).id[Face.South.ordinal] = tileCursor * tileSize
        if (getKey(Key.K4).bPressed) world.getCell(cursor).id[Face.West.ordinal] = tileCursor * tileSize
        if (getKey(Key.K5).bPressed) world.getCell(cursor).id[Face.Floor.ordinal] = tileCursor * tileSize
        if (getKey(Key.K6).bPressed) world.getCell(cursor).id[Face.Top.ordinal] = tileCursor * tileSize

        // Smooth camera
        cameraAngle += (cameraAngleTarget - cameraAngle) * 10.0f * elapsedTime

        // Arrow keys to move the selection cursor around map (boundary checked)
        if (getKey(Key.LEFT).bPressed) cursor.x--
        if (getKey(Key.RIGHT).bPressed) cursor.x++
        if (getKey(Key.UP).bPressed) cursor.y--
        if (getKey(Key.DOWN).bPressed) cursor.y++
        if (cursor.x < 0) cursor.x = 0
        if (cursor.y < 0) cursor.y = 0
        if (cursor.x >= world.size.x) cursor.x = world.size.x - 1
        if (cursor.y >= world.size.y) cursor.y = world.size.y - 1

        // Place block with space
        if (getKey(Key.SPACE).bPressed) {
            world.getCell(cursor).wall = !world.getCell(cursor).wall
        }

        // Position camera in world
        cameraPos = Vf2d(cursor.x + 0.5f, cursor.y + 0.5f)
        cameraPos *= cameraZoom

        // Rendering

        // 1) Create dummy cube to extract visible face information
        // Cull faces that cannot be seen
        val cullCube =
            createCube(Vi2d(0, 0), cameraAngle, cameraPitch, cameraZoom, Vec3d(cameraPos.x, 0.0f, cameraPos.y))
        calculateVisibleFaces(cullCube)

        // 2) Get all visible sides of all visible "tile cubes"
        val vQuads = mutableListOf<Quad>()
        /*for (y in 0 until world.size.y)
            for (x in 0 until world.size.x)
                getFaceQuads(
                    Vi2d(x, y),
                    cameraAngle,
                    cameraPitch,
                    cameraZoom,
                    Vec3d(cameraPos.x, 0.0f, cameraPos.y),
                    vQuads
                )*/

        runBlocking {
            (0 until world.size.y).asFlow()
                .flatMapConcat { y ->
                    (0 until world.size.x).asFlow()
                        .map { x ->
                            calculateFaceQuads(
                                Vi2d(x, y),
                                cameraAngle,
                                cameraPitch,
                                cameraZoom,
                                Vec3d(cameraPos.x, 0.0f, cameraPos.y)
                            )
                        }
                }.flatMapConcat { it.asFlow() }.toList(vQuads)
        }

        // 3) Sort in order of depth, from farthest away to closest
        vQuads.sortWith { a, b ->
            val z1 = (a.points[0].z + a.points[1].z + a.points[2].z + a.points[3].z) * 0.25f
            val z2 = (b.points[0].z + b.points[1].z + b.points[2].z + b.points[3].z) * 0.25f
            z1.compareTo(z2)
        }

        // 4) Iterate through all "tile cubes" and draw their visible faces
        clear(Pixel.BLACK)
        vQuads.forEach { q ->
            drawPartialWarpedDecal(
                decal = rendAllWalls!!.decal,
                pos = arrayOf(
                    Vf2d(q.points[0].x, q.points[0].y),
                    Vf2d(q.points[1].x, q.points[1].y),
                    Vf2d(q.points[2].x, q.points[2].y),
                    Vf2d(q.points[3].x, q.points[3].y)
                ),
                source_pos = q.title,
                source_size = tileSize.data
            )
        }

        // 5) Draw current tile selection
        drawPartialDecal(
            Vf2d(10, 10),
            rendAllWalls!!.decal,
            (tileCursor * tileSize).data,
            tileSize.data
        )

        // 6) Draw selection "tile cube"
        vQuads.clear()
        getFaceQuads(cursor, cameraAngle, cameraPitch, cameraZoom, Vec3d(cameraPos.x, 0.0f, cameraPos.y), vQuads)
        vQuads.forEach { q ->
            drawWarpedDecal(
                rendSelect!!.decal.apply { dirty = true },
                arrayOf(
                    Vf2d(q.points[0].x, q.points[0].y),
                    Vf2d(q.points[1].x, q.points[1].y),
                    Vf2d(q.points[2].x, q.points[2].y),
                    Vf2d(q.points[3].x, q.points[3].y)
                )
            )
        }
        // 7) Draw some debug info
        drawStringDecal(
            Vf2d(0, 0),
            "Cursor: ${cursor.x}, ${cursor.y}",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.5f)
        )
        drawStringDecal(
            Vf2d(0, 8),
            "Angle: $cameraAngle, $cameraPitch",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.5f)
        )

        // Graceful exit if user is in full screen mode
        return !getKey(Key.ESCAPE).bPressed
    }
}

fun mainOlcDungeon() {
    val demo = OlcDungeon()
    val xFactor = 1
    if (demo.construct(640 / xFactor, 480 / xFactor, 2 * xFactor, 2 * xFactor, false) == RetCode.OK)
        demo.start()
}
