package demos.slider

import olc.game_engine.Pixel
import olc.game_engine.PixelGameEngineImpl
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d

class Slider(
    val engine: PixelGameEngineImpl,
    val orientation: Orientation = Orientation.HORIZONTAL,
    val length: Int = 100,
    val width: Int = 12,
    val initialPosition: Float = 0f,
) {

    enum class Orientation {
        HORIZONTAL,
        VERTICAL
    }

    // moves from 0 to 1
    var position: Float = initialPosition
    var isHeld: Boolean = false
    var pickedAtPosition: Vi2d = Vi2d(0, 0)

    fun drawHorizontal(x: Int, y: Int, p: Pixel) {
        engine.drawLine(
            start = x to y,
            end = x + length to y,
            p
        )
        // left end
        engine.fillTriangle(
            point1 = x to y,
            point2 = x + width / 2 to y - width / 2,
            point3 = x + width / 2 to y + width / 2,
            p
        )
        // right end
        engine.fillTriangle(
            point1 = x + length to y,
            point2 = (x + length - width / 2) to (y - width / 2),
            point3 = (x + length - width / 2) to (y + width / 2),
            p
        )
        // movable block position
        val blockPosition = getBlockPosition(x, y, Orientation.HORIZONTAL)
        val color = if (isHeld) Pixel.YELLOW else p
        engine.fillRect(Vi2d(blockPosition.first), Vi2d(blockPosition.second), color)
    }

    fun drawVertical(x: Int, y: Int, p: Pixel) {
        engine.drawLine(
            start = x to y,
            end = x to y + length,
            p
        )
        // top end
        engine.fillTriangle(
            point1 = x to y,
            point2 = x + width / 2 to y + width / 2,
            point3 = x - width / 2 to y + width / 2,
            p
        )
        // bottom end
        engine.fillTriangle(
            point1 = (x + width / 2) to (y + length - width / 2),
            point2 = x to y + length,
            point3 = (x - width / 2) to (y + length - width / 2),
            p
        )
        // movable block position
        val blockPosition = getBlockPosition(x, y, Orientation.VERTICAL)
        val color = if (isHeld) Pixel.YELLOW else p
        engine.fillRect(Vi2d(blockPosition.first), Vi2d(blockPosition.second), color)
    }

    fun getBlockPosition(x: Int, y: Int, orientation: Orientation): Pair<Vf2d, Vf2d> {
        return when (orientation) {
            Orientation.HORIZONTAL -> {
                val startPosition = Vf2d(x + width / 2f, y - width / 2f) // top-left corner
                val size = Vf2d(width / 2f, width.toFloat())
                startPosition.x += (length - 3 * (width / 2f)) * position
                Pair(startPosition, size)
            }

            Orientation.VERTICAL -> {
                val startPosition = Vf2d(x - width / 2f, y + width / 2f) // top-left corner
                val size = Vf2d(width.toFloat(), width / 2f)
                startPosition.y += (length - 3 * (width / 2f)) * position
                Pair(startPosition, size)
            }
        }
    }

    fun draw(x: Int, y: Int, p: Pixel) {
        // get mouse position
        val mousePos = Vi2d(engine.getMouseX(), engine.getMouseY())
        val lmbState = engine.getMouseKey(1)
        val blockPosition = getBlockPosition(x, y, orientation)
        // check if mouse is in the block
        val isMouseInside = checkIfMouseIsInsideTheBlock(mousePos, blockPosition)

        if (!isHeld) {
            if (isMouseInside && lmbState.bHeld) {
                isHeld = true
                pickedAtPosition = mousePos
            }
        } else {
            if (!lmbState.bHeld) {
                isHeld = false
            }
        }

        // calculate position
        position = calculateNewPosition(mousePos)

        when (orientation) {
            Orientation.HORIZONTAL -> drawHorizontal(x, y, p)
            Orientation.VERTICAL -> drawVertical(x, y, p)
        }
    }

    private fun calculateNewPosition(mousePos: Vi2d): Float {
        if (!isHeld) {
            // nothing changed
            return position
        }

        // Calculate incremental delta since last update
        val delta = mousePos - pickedAtPosition

        // Determine travel range available for the block (must match getBlockPosition)
        val travel = (length - 3 * (width / 2f))
        if (travel <= 0f) {
            // Avoid division by zero or negative ranges
            pickedAtPosition = mousePos
            return position
        }

        // Select movement along the proper axis depending on orientation
        val axisDelta = when (orientation) {
            Orientation.HORIZONTAL -> delta.x.toFloat()
            Orientation.VERTICAL -> delta.y.toFloat()
        }

        // Convert pixel movement into normalized position change and clamp
        var newPos = position + (axisDelta / travel)
        if (newPos < 0f) newPos = 0f
        if (newPos > 1f) newPos = 1f

        // Update anchor for next incremental movement
        pickedAtPosition = mousePos

        return newPos
    }

    private fun checkIfMouseIsInsideTheBlock(mousePos: Vi2d, blockPosition: Pair<Vf2d, Vf2d>): Boolean {
        val start = blockPosition.first
        val size = blockPosition.second
        val endX = start.x + size.x
        val endY = start.y + size.y
        val mx = mousePos.x.toFloat()
        val my = mousePos.y.toFloat()
        return mx >= start.x && mx < endX && my >= start.y && my < endY
    }
}

