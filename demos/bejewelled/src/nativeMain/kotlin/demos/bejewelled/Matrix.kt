package demos.bejewelled

class Matrix<T>(val xSize: Int, val ySize: Int, val array: Array<Array<T>>) {

    companion object {
        inline operator fun <reified T> invoke() = Matrix(0, 0, Array(0) { emptyArray<T>() })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int) =
            Matrix(xWidth, yWidth, Array(xWidth) { arrayOfNulls<T>(yWidth) })

        inline operator fun <reified T> invoke(xWidth: Int, yWidth: Int, operator: (Int, Int) -> (T)): Matrix<T> {
            val array = Array(xWidth) { column ->
                Array(yWidth) { operator(column, it) }
            }
            return Matrix(xWidth, yWidth, array)
        }
    }

    operator fun get(x: Int, y: Int): T? {
        return if (x in 0 until xSize && y in 0 until ySize)
            array[x][y]
        else {
            null
        }
    }

    operator fun set(x: Int, y: Int, t: T) {
        if (x in 0 until xSize && y in 0 until ySize)
            array[x][y] = t
        else
            println("Matrix: attempt to set at [$x, $y] failed")
    }

    inline fun forEach(operation: (T) -> Unit) {
        array.forEach { it.forEach { operation.invoke(it) } }
    }

    inline fun forEachIndexed(operation: (x: Int, y: Int, T) -> Unit) {
        array.forEachIndexed { x, p -> p.forEachIndexed { y, t -> operation.invoke(x, y, t) } }
    }

    override fun toString(): String {
        val result = StringBuilder()

        for (y in 0 until ySize) {
            val row = mutableListOf<T?>()

            for (x in 0 until xSize) {
                row.add(this[x, y])
            }

            result.append(row.joinToString(separator = ",\t", postfix = "\n"))
        }

        return result.toString()
    }
}