package olc.game_engine

import kotlin.math.*

value class Vi2d(val packed: Long) {
    constructor(x: Int, y: Int) : this(
        (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL)
    )

    val x: Int get() = (packed shr 32).toInt()
    val y: Int get() = packed.toInt()

    operator fun times(v: Vi2d) = Vi2d(x * v.x, y * v.y)
    operator fun times(f: Float) = Vi2d((x * f).toInt(), (y * f).toInt())
    operator fun div(v: Vi2d) = Vi2d(x / v.x, y / v.y)
    operator fun div(f: Float) = Vi2d((x / f).toInt(), (y / f).toInt())
    operator fun minus(v: Vi2d) = Vi2d(x - v.x, y - v.y)
    operator fun plus(v: Vi2d) = Vi2d(x + v.x, y + v.y)
}

value class Vf2d(val packed: Long) {
    constructor(x: Float, y: Float) : this(
        (x.toRawBits().toLong() shl 32) or (y.toRawBits().toLong() and 0xFFFFFFFFL)
    )
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())

    val x: Float get() = Float.fromBits((packed shr 32).toInt())
    val y: Float get() = Float.fromBits(packed.toInt())
    val xi: Int get() = x.roundToInt()
    val yi: Int get() = y.roundToInt()

    operator fun div(v: Vf2d) = Vf2d(x / v.x, y / v.y)
    operator fun div(f: Float) = Vf2d(x / f, y / f)
    operator fun times(v: Vf2d) = Vf2d(x * v.x, y * v.y)
    operator fun times(f: Float) = Vf2d(x * f, y * f)
    operator fun times(d: Double) = Vf2d((x * d).toFloat(), (y * d).toFloat())
    operator fun plus(p: Vf2d) = Vf2d(x + p.x, y + p.y)
    operator fun minus(p: Vf2d) = Vf2d(x - p.x, y - p.y)
    operator fun unaryMinus() = Vf2d(-x, -y)

    infix fun dot(p: Vf2d): Float = x * p.x + y * p.y
    fun mag(): Float = sqrt(x * x + y * y)
    fun cross(rhs: Vf2d): Float = x * rhs.y - y * rhs.x

    override fun toString(): String {
        return "Vf2d(x=$x, y=$y)"
    }
}

fun Vf2d.toVd2d() = Vd2d(x.toDouble(), y.toDouble())
fun Vf2d.toVi2d() = Vi2d(x.toInt(), y.toInt())
fun Vi2d.toVf2d() = Vf2d(x.toFloat(), y.toFloat())
fun Vd2d.toVf2d() = Vf2d(x.toFloat(), y.toFloat())

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
    operator fun plusAssign(p: Vd2d) {
        x += p.x
        y += p.y
    }
    operator fun minus(p: Vd2d) = Vd2d(this.x - p.x, this.y - p.y)
    operator fun div(rhs: Vd2d) = Vd2d(this.x / rhs.x, this.y / rhs.y)
    operator fun div(f: Float) = Vd2d(this.x / f, this.y / f)
    operator fun unaryMinus() = Vd2d(-this.x, -this.y)

    infix fun dot(p: Vd2d) = (this.x * p.x + this.y * p.y)

    fun roundToInt() = Vd2d(this.x.roundToInt(), this.y.roundToInt())

    fun toPair(): Pair<Int, Int> {
        return this.x.toInt() to this.y.toInt()
    }

    fun mag() = sqrt(x * x + y * y)
    fun angle() = atan2(this.y, this.x)
    fun length() = mag()

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

        fun distance(p1: Vd2d, p2: Vd2d): Double {
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            return sqrt(dx * dx + dy * dy)
        }

        fun magnitude(p: Vd2d) = p.mag()

        fun angleBetween(p1: Vd2d, p2: Vd2d): Double {
            return acos(
                (p1 dot p2) / (magnitude(p1) * magnitude(p2))
            )
        }
    }
}
