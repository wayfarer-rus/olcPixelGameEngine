package geometry_2d

import kotlin.math.cos
import kotlin.math.sin

data class Shape(val points: List<Point>) {
    constructor(vararg p: Point) : this(p.toList())

    fun rotate(angle: Float) = Shape(
        points.map {
            Point(
                it.x * cos(angle) - it.y * sin(angle),
                it.x * sin(angle) + it.y * cos(angle)
            )
        })

    operator fun get(i: Int) = points[i]

    fun translate(pos: Point) = Shape(
        points.map {
            it + pos
        }
    )

}
