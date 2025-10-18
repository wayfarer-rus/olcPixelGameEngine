package olc.game_engine

@ExperimentalUnsignedTypes
class LayerDesc {
    val id = nextId()
    val offset = Vf2d(0, 0)
    val scale = Vf2d(1, 1)
    var show = false
    var update = false
    var drawTarget: Sprite? = null
    var resId: UInt = 0u
    val decalInstanceList: MutableList<DecalInstance> = mutableListOf()
    val tint = Pixel.WHITE
    val funcHook: (() -> Unit)? = null

    companion object {
        private var counter = 0L

        private fun nextId(): Long {
            counter += 1
            return counter
        }
    }
}
