package demos.bejewelled_maybe

import olc.game_engine.*

object CombinationsValidator {
    fun fiveInARow(state: Matrix<JewelType>) = xInARow(state, 5)
    fun fourInARow(state: Matrix<JewelType>) = xInARow(state, 4)
    fun threeInARow(state: Matrix<JewelType>) = xInARow(state, 3)

    fun fiveInACol(state: Matrix<JewelType>) = xInACol(state, 5)
    fun fourInACol(state: Matrix<JewelType>) = xInACol(state, 4)
    fun threeInACol(state: Matrix<JewelType>) = xInACol(state, 3)

    private fun xInACol(state: Matrix<JewelType>, nSize: Int): List<Pair<JewelType, List<Pair<Int, Int>>>> {
        val result = mutableListOf<Pair<JewelType, List<Pair<Int, Int>>>>()

        for (x in 0 until state.xSize) {
            for (y in 0 until state.ySize) {
                if (state[x, y] != JewelType.UNDEFINED) {
                    val row = mutableListOf<Pair<Int, Int>>()

                    for (i in 0 until nSize)
                        if (state[x, y] != state[x, y + i])
                            break
                        else
                            row.add(Pair(x, y + i))

                    if (row.size == nSize) {
                        result.add(Pair(state[x, y]!!, row.toList()))
                    }
                }
            }
        }

        return result
    }

    private fun xInARow(state: Matrix<JewelType>, nSize: Int): List<Pair<JewelType, List<Pair<Int, Int>>>> {
        val result = mutableListOf<Pair<JewelType, List<Pair<Int, Int>>>>()

        for (y in 0 until state.ySize) {
            for (x in 0 until state.xSize) {
                if (state[x, y] != JewelType.UNDEFINED) {
                    val row = mutableListOf<Pair<Int, Int>>()

                    for (i in 0 until nSize)
                        if (state[x, y] != state[x + i, y])
                            break
                        else
                            row.add(Pair(x + i, y))

                    if (row.size == nSize) {
                        result.add(Pair(state[x, y]!!, row.toList()))
                    }
                }
            }
        }

        return result
    }
}

class SortOfBejewelled : PixelGameEngineImpl() {
    override val appName: String = "Bejewelled"
    lateinit var arrowRenderable: Renderable
    lateinit var jewelsRenderable: Renderable
    lateinit var jewelsFactory: JewelFactory
    lateinit var world: World

    override fun onUserDestroy() {
        jewelsRenderable.close()
        arrowRenderable.close()
    }

    override fun onUserCreate(): Boolean {
        this.arrowRenderable = Renderable("jewels/cursor.png", this)
        this.jewelsRenderable = Renderable(Jewel.JEWEL_SPRITE_FILEPATH, this)
        this.jewelsFactory = JewelFactory(jewelsRenderable)
        this.world = RectWorld(8, 8, jewelsFactory)
        return true
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        clear(Pixel.BLACK)

        // Grab mouse for convenience
        val vMouse = Vi2d(getMouseX(), getMouseY())

        if (getMouseKey(1).bReleased) {
            world.selectGem(vMouse)
        }

        world.tick(elapsedTime)
        world.render(this, elapsedTime)

        drawStringDecal(Vf2d(0, 300), vMouse.toString())
        drawPartialDecal(vMouse.data, arrowRenderable.decal, Vf2d(0, 0), Vf2d(17, 20))

        return getKey(Key.ESCAPE).bPressed.not()
    }
}

fun mainSortOfBejewelled() {
    val main = SortOfBejewelled()
    val xFactor = 1

    if (main.construct(
            480 / xFactor,
            480 / xFactor,
            2 * xFactor,
            2 * xFactor,
            false
        ) == RetCode.OK
    ) {
        main.start()
    }
}

fun main() = mainSortOfBejewelled()
