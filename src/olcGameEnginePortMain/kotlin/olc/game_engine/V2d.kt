package olc.game_engine

import kotlin.math.*

inline class Vi2d(val data: Vf2d) {
    operator fun times(value: Vi2d) = Vi2d(data.times(value.data))
    operator fun times(f: Float) = Vi2d(data.times(f))
    operator fun div(v: Vi2d) = Vi2d(data.div(v.data))
    operator fun div(f: Float) = Vi2d(data.div(f))

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

inline class Vf2d(val data: Vd2d) {
    constructor(x: Int, y: Int) : this(Vd2d(x, y))
    constructor(x: Float, y: Float) : this(Vd2d(x.toDouble(), y.toDouble()))

    var x: Float
        get() = data.x.toFloat()
        set(v) {
            data.x = v.toDouble()
        }
    var y: Float
        get() = data.y.toFloat()
        set(v) {
            data.y = v.toDouble()
        }

    val xi: Int
        get() = data.xi
    val yi: Int
        get() = data.yi

    operator fun div(v: Vf2d) = Vf2d(data.div(v.data))
    operator fun div(f: Float) = Vf2d(data.div(f))
    operator fun times(v: Vf2d) = Vf2d(data.times(v.data))
    operator fun times(f: Float) = Vf2d(data.times(f))
    operator fun times(d: Double) = Vf2d(data.times(d))
    operator fun plus(p: Vf2d) = Vf2d(data.plus(p.data))
    operator fun minus(p: Vf2d) = Vf2d(data.minus(p.data))
    operator fun unaryMinus() = Vf2d(-data)

    infix fun dot(p: Vf2d) = data.dot(p.data).toFloat()

    fun mag() = data.mag().toFloat()
    fun cross(rhs: Vf2d) = data.cross(rhs.data).toFloat()

    override fun toString(): String {
        return "Vf2d(x=$x, y=$y)"
    }
}

data class Vd2d(
    var x: Double,
    var y: Double
) {
    val xi: Int
        get() = x.roundToInt()
    val yi: Int
        get() = y.roundToInt()

    constructor(x: Int, y: Int) : this(x.toDouble(), y.toDouble())

    operator fun times(d: Double) = Vd2d(this.x * d, this.y * d)
    operator fun times(f: Float) = times(f.toDouble())
    operator fun times(p: Vd2d) = Vd2d(this.x * p.x, this.y * p.y)
    operator fun plus(p: Vd2d) = Vd2d(this.x + p.x, this.y + p.y)
    operator fun minus(p: Vd2d) = Vd2d(this.x - p.x, this.y - p.y)
    operator fun div(rhs: Vd2d) = Vd2d(this.x / rhs.x, this.y / rhs.y)
    operator fun div(f: Float) = Vd2d(this.x / f, this.y / f)
    operator fun unaryMinus() = Vd2d(-this.x, -this.y)

    infix fun dot(p: Vd2d) = (this.x * p.x + this.y * p.y)

    fun roundToInt() = Vd2d(this.x.roundToInt(), this.y.roundToInt())

    fun toPair(): Pair<Int, Int> {
        return this.x.toInt() to this.y.toInt()
    }

    fun mag() = magnitude(this)
    fun angle() = atan2(this.y, this.x)
    fun length() = distance(Vd2d(0.0, 0.0), this)

    fun toLength(length: Float): Vd2d {
        val m = this.mag()

        return if (m > 0)
            this * (length / m)
        else
            this
    }

    fun inBounds(topLeft: Vd2d, bottomRight: Vd2d): Boolean {
        return (this.x > topLeft.x && this.x < bottomRight.x && this.y > topLeft.y && this.y < bottomRight.y)
    }

    fun cross(rhs: Vd2d): Double {
        return this.x * rhs.y - this.y * rhs.x
    }

    companion object {
        fun pointTo(angle: Float, length: Float) = Vd2d(length.toDouble() * cos(angle), length.toDouble() * sin(angle))

        fun distance(p1: Vd2d, p2: Vd2d) = sqrt(
            (p1.x - p2.x).pow(2) + (p1.y - p2.y).pow(2)
        )

        fun magnitude(p: Vd2d) = sqrt(p.x.pow(2) + p.y.pow(2))

        fun angleBetween(p1: Vd2d, p2: Vd2d): Double {
            return acos(
                (p1 dot p2) / (magnitude(p1) * magnitude(p2))
            )
        }
    }
}
