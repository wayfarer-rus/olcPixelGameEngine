package demos.bejewelled_maybe

import olc.game_engine.Decal
import olc.game_engine.PixelGameEngine
import olc.game_engine.Vf2d
import olc.game_engine.Vi2d

enum class JewelState {
    NOT_ON_FIELD, STILL, MOVING, FALLING, COLLAPSING, ELIMINATED
}

enum class JewelType {
    UNDEFINED, EMERALD, AMBER, RUBY, SAPPHIRE, TOPAZ, DIAMOND
}

class JewelFactory(private val jewelSprite: Renderable) {
    fun createJewel(type: JewelType): Jewel {
        return when (type) {
            JewelType.AMBER -> Amber(jewelSprite)
            JewelType.DIAMOND -> Diamond(jewelSprite)
            JewelType.EMERALD -> Emerald(jewelSprite)
            JewelType.RUBY -> Ruby(jewelSprite)
            JewelType.SAPPHIRE -> Sapphire(jewelSprite)
            JewelType.TOPAZ -> Topaz(jewelSprite)
            else -> error("unsupported JewelType")
        }
    }

    fun ruby() = createJewel(JewelType.RUBY)
    fun diamond() = createJewel(JewelType.DIAMOND)
    fun amber() = createJewel(JewelType.AMBER)
    fun emerald() = createJewel(JewelType.EMERALD)
    fun sapphire() = createJewel(JewelType.SAPPHIRE)
    fun topaz() = createJewel(JewelType.TOPAZ)
}

class Topaz(jewelSprite: Renderable) : JewelBase(rubyPosOnDecal, jewelSprite.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(141f, 0f)
    }

    override val type: JewelType
        get() = JewelType.TOPAZ
}

class Sapphire(jewelSprite: Renderable) : JewelBase(rubyPosOnDecal, jewelSprite.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(105f, 0f)
    }

    override val type: JewelType
        get() = JewelType.SAPPHIRE
}

class Emerald(jewelSprite: Renderable) : JewelBase(rubyPosOnDecal, jewelSprite.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(0f, 0f)
    }

    override val type: JewelType
        get() = JewelType.EMERALD
}

class Diamond(jewelSprite: Renderable) : JewelBase(rubyPosOnDecal, jewelSprite.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(177f, 0f)
    }

    override val type: JewelType
        get() = JewelType.DIAMOND
}

class Amber(jewelSprite: Renderable) : JewelBase(rubyPosOnDecal, jewelSprite.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(36f, 0f)
    }

    override val type: JewelType
        get() = JewelType.AMBER
}

class Ruby(renderable: Renderable) : JewelBase(rubyPosOnDecal, renderable.decal) {
    companion object {
        private val rubyPosOnDecal = Vf2d(70f, 0f)
    }

    override val type: JewelType
        get() = JewelType.RUBY
}

interface Jewel {
    var pos: Vf2d
    val positionOnGrid: Vi2d
    var state: JewelState
    val type: JewelType
    fun render(pge: PixelGameEngine, elapsedTime: Float)
    fun setStill()
    fun fall(elapsedTime: Float)

    companion object {
        const val JEWEL_SIZE = 35f
        const val JEWEL_SPRITE_FILEPATH = "jewels/jewels.png"
        const val JEWEL_FALLING_SPEED = 300f
        const val JEWEL_ROTATION_SPEED = 10f
    }
}

abstract class JewelBase(
    private val posOnDecal: Vf2d,
    private val decal: Decal
) : Jewel {
    override var pos = Vf2d(0, 0)
    override var state = JewelState.STILL

    private var angle = 0f
    private var scale = 1f

    override val positionOnGrid: Vi2d
        get() = Vi2d((pos.x / Jewel.JEWEL_SIZE).toInt(), (pos.y / Jewel.JEWEL_SIZE).toInt())

    override fun render(pge: PixelGameEngine, elapsedTime: Float) {
        when (state) {
            JewelState.COLLAPSING -> {
                angle += Jewel.JEWEL_ROTATION_SPEED * elapsedTime
                scale -= elapsedTime

                if (scale < 0f) state = JewelState.ELIMINATED

                pge.drawPartialRotatedDecal(
                    pos = pos + jewelDimentions / 2f,
                    decal = decal,
                    source_pos = posOnDecal,
                    source_size = jewelDimentions,
                    angle = angle,
                    center = jewelDimentions / 2f,
                    scale = Vf2d(scale, scale)
                )
            }
            else -> pge.drawPartialDecal(pos, decal, posOnDecal, jewelDimentions)
        }
    }

    override fun setStill() {
        val p = positionOnGrid
        pos.x = p.x * Jewel.JEWEL_SIZE
        pos.y = p.y * Jewel.JEWEL_SIZE
        state = JewelState.STILL
    }

    override fun fall(elapsedTime: Float) {
        pos.y += elapsedTime * Jewel.JEWEL_FALLING_SPEED
        state = JewelState.FALLING
    }

    override fun toString(): String {
        return "JewelBase(pos=$pos, state=$state)"
    }

    companion object {
        private val jewelDimentions = Vf2d(Jewel.JEWEL_SIZE, Jewel.JEWEL_SIZE)
    }
}
