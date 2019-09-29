package geometry_2d

import kotlin.math.*

data class Point(
    val x: Float,
    val y: Float
) {
    constructor(x: Int, y: Int) : this(x.toFloat(), y.toFloat())
    constructor(v: Vector) : this(v[0], v[1])

    operator fun times(f: Float) = Point(this.x * f, this.y * f)

    operator fun times(d: Double) = times(d.toFloat())

    operator fun plus(p: Point) = Point(this.x + p.x, this.y + p.y)

    operator fun minus(p: Point) = Point(this.x - p.x, this.y - p.y)

    operator fun times(p: Point) = (this.x * p.x + this.y * p.y)

    fun roundToInt() = Point(this.x.roundToInt(), this.y.roundToInt())

    fun toPair(): Pair<Int, Int> {
        return this.x.toInt() to this.y.toInt()
    }

    fun mag() = magnitude(this)
    fun angle() = atan2(this.y, this.x)
    fun length() = distance(Point(0f, 0f), this)

    fun toLength(length: Float): Point {
        val m = this.mag()

        return if (m > 0)
            this * (length / m)
        else
            this
    }

    companion object {
        fun pointTo(angle: Float, length: Float) = Point(length * cos(angle), length * sin(angle))
        fun distance(p1: Point, p2: Point) = sqrt(
            (p1.x.toDouble() - p2.x.toDouble()).pow(2) + (p1.y.toDouble() - p2.y.toDouble()).pow(2)
        )

        fun magnitude(p: Point) = sqrt(p.x.pow(2) + p.y.pow(2))

        fun angleBetween(p1: Point, p2: Point): Float {
            return acos(
                (p1 * p2) / (magnitude(p1) * magnitude(p2))
            )
        }
    }
}