package demos.bejewelled_maybe

import io.ktor.utils.io.core.*
import olc.game_engine.Decal
import olc.game_engine.PixelGameEngine
import olc.game_engine.Sprite

class Renderable(
    sFile: String,
    private val pge: PixelGameEngine
) : Closeable {
    val sprite: Sprite = Sprite(sFile)
    val decal: Decal = pge.createDecal(sprite)

    override fun close() {
        pge.deleteDecal(decal)
    }

    companion object {
        fun load(file: String, pge: PixelGameEngine): Renderable {
            return Renderable(file, pge)
        }
    }
}