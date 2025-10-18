package game.pixel_shooter

import olc.game_engine.PixelGameEngine
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

abstract class Creature(
    var pos: Pos<Float> = Pos(0f, 0f),
    var velocityVector: Vector = Vector(0f, 0f),
    var angle: Float = 0.0f,
    var speed: Float = 1.0f,
    var screenPos: Pos<Float> = Pos(0f, 0f)
) {
    @ExperimentalUnsignedTypes
    open fun drawSelf(gfx: PixelGameEngine, offsetPos: Pos<Float>) {
    }

    @ExperimentalUnsignedTypes
    open fun updateState(gfx: PixelGameEngine, world: Map, elapsedTime: Float) {
    }

    protected fun toScreen(
        pos: Pos<Float>,
        offsetPos: Pos<Float>
    ) = (pos - offsetPos) * globalSpriteSize

    protected fun toWorld(
        pos: Pos<Float>,
        offsetPos: Pos<Float>
    ) = (pos / globalSpriteSize + offsetPos)
}

class Vector(vx: Float, vy: Float) : Pos<Float>(vx, vy) {
    constructor(point: Pos<Float>) : this(point.x, point.y)

    fun rotate(a: Double) = Vector((vx * cos(a) - vy * sin(a)).toFloat(), (vx * sin(a) - vy * cos(a)).toFloat())

    fun toPos(offset: Pos<Float>, length: Float): Pos<Float> {
        val m = mag()
        if (m == 0.0f) return offset
        val u = this * (length / m)
        return offset + u
    }

    private fun mag() = sqrt(vx.pow(2) + vy.pow(2))

    operator fun times(value: Float) = Vector(this.x * value, this.y * value)

    override fun toString(): String {
        return "Vector(${super.toString()})"
    }

    val vx: Float
        get() = x
    val vy: Float
        get() = y
}

private operator fun Pos<Float>.plus(vector: Vector): Pos<Float> = Pos(this.x + vector.vx, this.y + vector.vy)
private operator fun Pos<Int>.plus(vector: Vector): Pos<Float> = Pos(this.x + vector.vx, this.y + vector.vy)
