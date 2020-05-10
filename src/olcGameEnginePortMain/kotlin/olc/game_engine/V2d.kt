package olc.game_engine

import kotlin.math.*

inline class Vi2d(val data: Vf2d) {
    operator fun times(value: Vi2d) = Vi2d(data.times(value.data))
    operator fun div(v: Vi2d) = Vi2d(data.div(v.data))

    constructor(x: Int, y: Int) : this(Vf2d(x, y))

    var x: Int
        get() = data.xi
        set(v) {
            data.x = v.toFloat()
        }
    var y: Int
        get() = data.yi
        set(v) {
            data.y = v.toFloat()
        }
}

data class Vf2d(
    var x: Float,
    var y: Float
) {
    val xi: Int
        get() = x.roundToInt()
    val yi: Int
        get() = y.roundToInt()

    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

    operator fun times(f: Float) = Vf2d(this.x * f, this.y * f)

    operator fun times(d: Double) = times(d.toFloat())

    operator fun plus(p: Vf2d) = Vf2d(this.x + p.x, this.y + p.y)

    operator fun minus(p: Vf2d) = Vf2d(this.x - p.x, this.y - p.y)

    operator fun times(p: Vf2d) = Vf2d(this.x * p.x, this.y * p.y)

    operator fun div(rhs: Vf2d) = Vf2d(this.x / rhs.x, this.y / rhs.y)

    infix fun dot(p: Vf2d) = (this.x * p.x + this.y * p.y)

    fun roundToInt() = Vf2d(this.x.roundToInt(), this.y.roundToInt())

    fun toPair(): Pair<Int, Int> {
        return this.x.toInt() to this.y.toInt()
    }

    fun mag() = magnitude(this)
    fun angle() = atan2(this.y, this.x)
    fun length() = distance(Vf2d(0f, 0f), this)

    fun toLength(length: Float): Vf2d {
        val m = this.mag()

        return if (m > 0)
            this * (length / m)
        else
            this
    }

    fun inBounds(topLeft: Vf2d, bottomRight: Vf2d): Boolean {
        return (this.x > topLeft.x && this.x < bottomRight.x && this.y > topLeft.y && this.y < bottomRight.y)
    }

    fun cross(rhs: Vf2d): Float {
        return this.x * rhs.y - this.y * rhs.x
    }

    companion object {
        fun pointTo(angle: Float, length: Float) = Vf2d(length * cos(angle), length * sin(angle))
        fun distance(p1: Vf2d, p2: Vf2d) = sqrt(
            (p1.x.toDouble() - p2.x.toDouble()).pow(2) + (p1.y.toDouble() - p2.y.toDouble()).pow(2)
        )

        fun magnitude(p: Vf2d) = sqrt(p.x.pow(2) + p.y.pow(2))

        fun angleBetween(p1: Vf2d, p2: Vf2d): Float {
            return acos(
                (p1 dot p2) / (magnitude(p1) * magnitude(p2))
            )
        }
    }
}
