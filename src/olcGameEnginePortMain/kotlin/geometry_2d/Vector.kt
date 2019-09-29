package geometry_2d

class Vector(
    val data: List<Float>
) {
    operator fun times(m: Matrix) = Vector(
        data.mapIndexed { i: Int, _: Float ->
            m.row(i).data.foldRightIndexed(0f) { ind, fl, acc -> acc + fl * data[ind] }
        }
    )

    operator fun get(i: Int): Float {
        return try {
            data[i]
        } catch (_: Exception) {
            0f
        }
    }

    constructor(vararg f: Float) : this(f.toList())
    constructor(p: Point) : this(p.x, p.y)
}