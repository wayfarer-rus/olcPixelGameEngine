package geometry_2d

class Matrix(
    val data: List<Vector>
) {
    fun row(i: Int) = Vector(data.map { it[i] })

    constructor(vararg v: Vector) : this(v.toList())
}