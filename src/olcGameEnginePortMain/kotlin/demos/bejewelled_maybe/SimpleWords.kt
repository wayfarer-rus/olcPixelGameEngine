package demos.bejewelled_maybe

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngine
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d

interface World {
    fun tick(elapsedTime: Float)
    fun render(pge: PixelGameEngine, elapsedTime: Float)
    fun selectGem(vMouse: Vi2d)
}

class RectWorld(val w: Int, val h: Int, val jewelFactory: JewelFactory) : World {
    var state: Matrix<Jewel?> = Matrix<Jewel?>(w, h)
    var selectedGem = Vi2d(0, 0)

    private fun findCombinations(): List<Pair<JewelType, List<Pair<Int, Int>>>> {
        val simpleState = Matrix(w, h) { x, y ->
            if (state[x, y]?.state == JewelState.STILL)
                state[x, y]?.type!!
            else
                JewelType.UNDEFINED
        }

        return CombinationsValidator.threeInARow(simpleState) + CombinationsValidator.threeInACol(simpleState)
    }

    override fun tick(elapsedTime: Float) {
        // find combinations
        val stateWithIndexes = findCombinations()
//        println(stateWithIndexes.toString())
//        println("---------------------------------------")
        stateWithIndexes.forEach { entry ->
            entry.second.forEach { coords -> state[coords.first, coords.second]?.state = JewelState.COLLAPSING }
        }

        // falling
        state.forEachIndexed { x, y, jewel ->
            when (jewel?.state) {
                JewelState.ELIMINATED -> state[x, y] = null
                JewelState.FALLING -> {
//                    println("World0: [$x, $y] -> $jewel")
                    val curPos = adjust(jewel.positionOnGrid)
                    val jewelBelow = state[curPos.x, curPos.y + 1]
                    state[curPos.x, curPos.y] = null

                    if (jewelBelow != null && jewelBelow.state != JewelState.ELIMINATED) {
                        jewel.setStill()
                        state[curPos.x, curPos.y] = jewel
                    } else if (curPos.y == h - 1) {
                        jewel.setStill()
                        state[x, h - 1] = jewel
                    } else {
                        jewel.fall(elapsedTime)
//                        println("World1: [$x, $y] -> $jewel")
                        val newPos = adjust(jewel.positionOnGrid)
                        state[newPos.x, newPos.y] = jewel
                    }

//                    println("World2: [$x, $y] -> $jewel")
                }
                JewelState.STILL -> {
                    if (jewel.pos.y < 0) {
                        jewel.fall(elapsedTime)
                    }
                }
            }
        }
        // check for empty spaces
        state.forEachIndexed { x, y, jewel ->
            if (jewel == null) {
                var colI = y - 1

                while (colI >= 0) {
                    if (state[x, colI] != null && state[x, colI]?.state == JewelState.STILL) {
                        state[x, colI]?.state = JewelState.FALLING
                    }

                    colI--
                }

                // spawn new jewel
                if (state[x, 0] == null) {
                    var nextJewelType = JewelType.values().random()
                    while (nextJewelType == JewelType.UNDEFINED) nextJewelType = JewelType.values().random()
                    state[x, 0] = jewelFactory.createJewel(nextJewelType)
                    state[x, 0]?.pos = Vf2d(x * Jewel.JEWEL_SIZE, -Jewel.JEWEL_SIZE)
                    state[x, 0]?.state = JewelState.FALLING
                }
            }
        }
    }

    private fun adjust(pos: Vi2d): Vi2d {
        var xVal = pos.x
        var yVal = pos.y

        when {
            xVal < 0 -> xVal = 0
            xVal >= w -> xVal = w - 1
        }

        when {
            yVal < 0 -> yVal = 0
            yVal >= h -> yVal = h - 1
        }

        return Vi2d(xVal, yVal)
    }

    override fun render(pge: PixelGameEngine, elapsedTime: Float) {
        var skipSelectionDraw = false

        state.forEachIndexed { x, y, jewel ->
//            println("World: [$x, $y] -> $jewel")

            when (jewel?.state) {
                JewelState.STILL, JewelState.MOVING, JewelState.FALLING, JewelState.COLLAPSING -> {
                    jewel.render(pge, elapsedTime)
                }
            }

            if (jewel?.state == JewelState.FALLING) skipSelectionDraw = true

            error()
        }

        if (skipSelectionDraw.not())
            pge.drawRect(selectedGem, Vi2d(Jewel.JEWEL_SIZE.toInt(), Jewel.JEWEL_SIZE.toInt()), Pixel.CYAN)
    }

    override fun selectGem(vMouse: Vi2d) {
        selectedGem = Vi2d(
            vMouse.x / Jewel.JEWEL_SIZE.toInt(),
            vMouse.y / Jewel.JEWEL_SIZE.toInt()
        )
        selectedGem *= Jewel.JEWEL_SIZE
        println("selection at: $selectedGem")
    }
}
