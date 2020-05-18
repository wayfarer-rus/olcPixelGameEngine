package demos.mandelbrot

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import olc.game_engine.*
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker
import kotlin.system.getTimeNanos

class MandelbrotDemo : PixelGameEngineImpl() {
    override val appName = "Mandelbrot Set"

    private lateinit var sprite: Sprite
    private val world = Pair(Vd2d(-2.5, 1.0), Vd2d(3.5, 2.0))
    private var type = 0
    private var name = "Naive"
    private var iterations = 64
    private lateinit var workers: Array<Worker>
    private var zoom = .2

    override fun onUserCreate(): Boolean {
        iterations = 64
        sprite = getDrawTarget()
        workers = Array(4) { Worker.start() }
        return true
    }

    override fun onUserDestroy() {
        workers.forEach {
            it.requestTermination().result
        }
    }

    override fun onUserUpdate(elapsedTime: Float): Boolean {
        val mousePos = Vi2d(getMouseX(), getMouseY())
        clear()

        val t = getTimeNanos()
        runBlocking {
            when (type) {
                0 -> calculate(Vi2d(0, 0), Vi2d(sprite.width, sprite.height), iterations)
                1 -> calculateWithFlow(Vi2d(0, 0), Vi2d(sprite.width, sprite.height), iterations)
                2 -> calculateWithAsync(Vi2d(0, 0), Vi2d(sprite.width, sprite.height), iterations)
                3 -> calculateWithThreads(Vi2d(0, 0), Vi2d(sprite.width, sprite.height), iterations)
            }
        }
        val time = (getTimeNanos() - t).toDouble() / 1_000_000_000

        when {
            getKey(Key.ESCAPE).bReleased -> return false
            getKey(Key.UP).bReleased -> iterations *= 2
            getKey(Key.DOWN).bReleased -> iterations /= 2
            getKey(Key.SPACE).bReleased -> iterations = 64
            getKey(Key.K1).bReleased || getKey(Key.K1).bHeld -> {
                type = 0; name = "Naive"
            }
            getKey(Key.K2).bReleased || getKey(Key.K2).bHeld -> {
                type = 1; name = "Naive Flow"
            }
            getKey(Key.K3).bReleased || getKey(Key.K3).bHeld -> {
                type = 2; name = "Naive Async"
            }
            getKey(Key.K4).bReleased || getKey(Key.K4).bHeld -> {
                type = 3; name = "Naive Workers"
            }
        }

        zoom = world.second.x / 5.0

        when {
            getMouse(1).bHeld -> handleZoomIn(mousePos, elapsedTime)
            getMouse(2).bHeld -> handleZoomOut(mousePos, elapsedTime)
            getMouse(3).bReleased -> {
                world.first.x = -2.5
                world.first.y = 1.0
                world.second.x = 3.5
                world.second.y = 2.0
            }
        }

        // draw info
        drawStringDecal(
            Vf2d(0, 0),
            "Iterations: $iterations",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.8f)
        )
        drawStringDecal(
            Vf2d(0, 8),
            "Algorithm: $name",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.8f)
        )
        drawStringDecal(
            Vf2d(0, 16),
            "Processing time: $time",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.8f)
        )
        drawStringDecal(
            Vf2d(0, 24),
            "World: $world",
            Pixel.YELLOW,
            Vf2d(0.5f, 0.8f)
        )
        // draw mouse cursor
        drawCircle(mousePos, 1, Pixel.WHITE)
        return true
    }

    private fun handleZoomOut(mousePos: Vi2d, elapsedTime: Float) {
        val w = Vd2d(screenToWorldX(mousePos.x), screenToWorldY(mousePos.y))

        world.second.x += elapsedTime * zoom * world.second.x / world.second.y
        world.second.y += elapsedTime * zoom

        val wn = Vd2d(screenToWorldX(mousePos.x), screenToWorldY(mousePos.y))
        val wd = w - wn
        world.first.x += wd.x
        world.first.y += wd.y
    }

    private fun handleZoomIn(mousePos: Vi2d, elapsedTime: Float) {
        val w = Vd2d(screenToWorldX(mousePos.x), screenToWorldY(mousePos.y))
        var dwx = elapsedTime * zoom * world.second.x / world.second.y
        var dwy = elapsedTime * zoom

        while (world.second.x - dwx < 0.0) dwx /= 10
        world.second.x -= dwx

        while (world.second.y - dwy < 0.0) dwy /= 10
        world.second.y -= dwy

        val wn = Vd2d(screenToWorldX(mousePos.x), screenToWorldY(mousePos.y))
        val wd = w - wn
        world.first.x += wd.x
        world.first.y += wd.y
    }

    /*
    for each pixel (Px, Py) on the screen do
        x0 = scaled x coordinate of pixel (scaled to lie in the Mandelbrot X scale (-2.5, 1))
        y0 = scaled y coordinate of pixel (scaled to lie in the Mandelbrot Y scale (-1, 1))
        x := 0.0
        y := 0.0
        iteration := 0
        max_iteration := 1000
        while (x×x + y×y ≤ 2×2 AND iteration < max_iteration) do
            xtemp := x×x - y×y + x0
            y := 2×x×y + y0
            x := xtemp
            iteration := iteration + 1

        color := palette[iteration]
        plot(Px, Py, color)
     */
    private fun calculate(pos: Vi2d, size: Vi2d, maxIterations: Int) {
        for (py in pos.y until (pos.y + size.y))
            for (px in pos.x until (pos.x + size.x)) {
                val x0 = screenToWorldX(px)
                val y0 = screenToWorldY(py)
                var x = 0.0
                var y = 0.0
                var iteration = 0

                while ((x * x + y * y) <= 4 && iteration < maxIterations) {
                    val tmp = x * x - y * y + x0
                    y = 2 * x * y + y0
                    x = tmp
                    ++iteration
                }

                sprite.data[py * sprite.width + px] = palette(x, y, iteration, maxIterations)
            }
    }

    fun calculateUnbound(input: Triple<Vi2d, Vi2d, Int>): UIntArray {
        val pos: Vi2d = input.first
        val size: Vi2d = input.second
        val maxIterations: Int = input.third
        val result = UIntArray(size.x * size.y)

        for (py in pos.y until (pos.y + size.y))
            for (px in pos.x until (pos.x + size.x)) {
                val x0 = screenToWorldX(px)
                val y0 = screenToWorldY(py)
                var x = 0.0
                var y = 0.0
                var iteration = 0

                while ((x * x + y * y) <= 4 && iteration < maxIterations) {
                    val tmp = x * x - y * y + x0
                    y = 2 * x * y + y0
                    x = tmp
                    ++iteration
                }

                result[(py - pos.y) * size.x + (px - pos.x)] = palette(x, y, iteration, maxIterations)
            }

        return result
    }

    private suspend fun calculateWithAsync(pos: Vi2d, size: Vi2d, maxIterations: Int) = coroutineScope {
        val scale = 4
        for (j in 0 until scale)
            for (i in 0 until scale) {
                launch {
                    calculate(
                        Vi2d(pos.x + i * size.x / scale, pos.y + j * size.y / scale),
                        Vi2d(size.x / scale, size.y / scale),
                        maxIterations
                    )
                }
            }
    }

    private fun calculateWithThreads(pos: Vi2d, size: Vi2d, maxIterations: Int) {
        sprite.data = Array(workers.size) { workerIndex ->
            workers[workerIndex].execute(TransferMode.UNSAFE, {
                Pair(
                    this::calculateUnbound,
                    Triple(
                        Vi2d(pos.x, workerIndex * size.y / workers.size),
                        Vi2d(size.x, size.y / workers.size),
                        maxIterations
                    )
                )
            }) { it.first(it.second) }
        }.fold(UIntArray(0)) { acc, future ->
            acc + future.result
        }
    }

    private suspend fun calculateWithFlow(pos: Vi2d, size: Vi2d, maxIterations: Int) = coroutineScope {
        (pos.y until size.y).asFlow()
            .flatMapConcat { py ->
                (pos.x until size.x).asFlow()
                    .map { px ->
                        val x0 = screenToWorldX(px)
                        val y0 = screenToWorldY(py)
                        var x = 0.0
                        var y = 0.0
                        var iteration = 0

                        while ((x * x + y * y) <= 4 && iteration < maxIterations) {
                            val tmp = x * x - y * y + x0
                            y = 2 * x * y + y0
                            x = tmp
                            ++iteration
                        }

                        palette(x, y, iteration, maxIterations)
                    }
            }.collectIndexed { i, v ->
                sprite.data[i] = v
            }
    }

    /*
    // Used to avoid floating point issues with points inside the set.
    if iteration < max_iteration then
        // sqrt of inner term removed using log simplification rules.
        log_zn := log(x*x + y*y) / 2
        nu := log(log_zn / log(2)) / log(2)
        // Rearranging the potential function.
        // Dividing log_zn by log(2) instead of log(N = 1<<8)
        // because we want the entire palette to range from the
        // center to radius 2, NOT our bailout radius.
        iteration := iteration + 1 - nu

    color1 := palette[floor(iteration)]
    color2 := palette[floor(iteration) + 1]
    // iteration % 1 = fractional part of iteration.
    color := linear_interpolate(color1, color2, iteration % 1)
    plot(Px, Py, color)
     */
    private fun palette(x: Double, y: Double, iteration: Int, maxIteration: Int): UInt {
        var i = iteration.toDouble()

        if (iteration < maxIteration) {
            val logZn = kotlin.math.ln(x * x + y * y) / 2.0
            val nu = kotlin.math.ln(logZn / kotlin.math.ln(2.0)) / kotlin.math.ln(2.0)
            i = iteration + 1 - nu
        }

        val c1 = Pixel.COLORS[kotlin.math.floor(i).toInt() % Pixel.COLORS.size]
        val c2 = Pixel.COLORS[(kotlin.math.floor(i) + 1.0).toInt() % Pixel.COLORS.size]
        return lerp(c1.n, c2.n, i.toUInt() % 1u)
    }

    private fun lerp(v0: UInt, v1: UInt, t: UInt): UInt {
        return (1u - t) * v0 + t * v1
    }

    private fun screenToWorldY(y: Int): Double {
        return world.first.y - (world.second.y / sprite.height * y)
    }

    private fun screenToWorldX(x: Int): Double {
        return world.first.x + (world.second.x / sprite.width * x)
    }

}

fun main() {
    val demo = MandelbrotDemo()
    val xFactor = 1
    if (demo.construct(640 / xFactor, 400 / xFactor, 2 * xFactor, 2 * xFactor, false) == RetCode.OK)
        demo.start()
}