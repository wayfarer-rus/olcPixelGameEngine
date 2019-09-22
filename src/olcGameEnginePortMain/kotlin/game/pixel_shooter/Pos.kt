package game.pixel_shooter

import kotlin.math.roundToInt

open class Pos<T>(val p: Pair<T, T>) {
    val x = p.first
    val y = p.second

    constructor(x: T, y: T) : this(Pair(x, y))

    override fun toString(): String {
        return "Pos(p=$p)"
    }
}

operator fun Pos<Float>.times(i: Int): Pos<Float> = Pos(this.x * i, this.y * i)
operator fun Pos<Float>.times(f: Float): Pos<Float> = Pos(this.x * f, this.y * f)
operator fun Pos<Int>.minus(pos: Pos<Float>): Pos<Float> = Pos(this.x.toFloat() - pos.x, this.y.toFloat() - pos.y)
fun Pos<Float>.roundToInt(): Pos<Int> = Pos(this.x.roundToInt(), this.y.roundToInt())
