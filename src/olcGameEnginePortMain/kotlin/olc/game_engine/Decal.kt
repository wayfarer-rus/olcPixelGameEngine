package olc.game_engine

@ExperimentalUnsignedTypes
data class Decal(
    val id: UInt,
    val sprite: Sprite,
    val uvScale: Vf2d
)

@ExperimentalUnsignedTypes
class DecalInstance(
    val decal: Decal,
    val pos: Array<Vf2d> = arrayOf(Vf2d(0.0f, 0.0f), Vf2d(0.0f, 0.0f), Vf2d(0.0f, 0.0f), Vf2d(0.0f, 0.0f)),
    val uv: Array<Vf2d> = arrayOf(Vf2d(0.0f, 0.0f), Vf2d(0.0f, 1.0f), Vf2d(1.0f, 1.0f), Vf2d(1.0f, 0.0f)),
    val w: Array<Float> = arrayOf(1f, 1f, 1f, 1f),
    val tint: Pixel
)