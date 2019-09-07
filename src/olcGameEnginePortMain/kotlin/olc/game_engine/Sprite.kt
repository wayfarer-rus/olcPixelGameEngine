package olc.game_engine

import kotlinx.cinterop.*
import olc.game_engine.Sprite.Companion.nOverdrawCount
import platform.posix.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

/**

License (OLC-3)
~~~~~~~~~~~~~~~
Copyright 2018 - 2019 OneLoneCoder.com
Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
1. Redistributions or derivations of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions or derivative works in binary form must reproduce the above
copyright notice. This list of conditions and the following	disclaimer must be
reproduced in the documentation and/or other materials provided with the distribution.
3. Neither the name of the copyright holder nor the names of its contributors may
be used to endorse or promote products derived from this software without specific
prior written permission.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS	"AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT	HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL,	SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

Links
~~~~~
YouTube:	https://www.youtube.com/javidx9
https://www.youtube.com/javidx9extra
Discord:	https://discord.gg/WhwHUMV
Twitter:	https://www.twitter.com/javidx9
Twitch:		https://www.twitch.tv/javidx9
GitHub:		https://www.github.com/onelonecoder
Homepage:	https://www.onelonecoder.com
Patreon:	https://www.patreon.com/javidx9
 */

interface Sprite {
    val width: Int
    var height: Int

    fun LoadFromFile(imageFile: String, pack: ResourcePack? = null): rcode
    fun LoadFromPGESprFile(imageFile: String, pack: ResourcePack? = null): rcode
    fun SaveToPGESprFile(imageFile: String): rcode
    fun SetSampleMode(mode: Mode = Mode.NORMAL)
    @ExperimentalUnsignedTypes
    fun GetPixel(x: Int, y: Int): Pixel

    @ExperimentalUnsignedTypes
    fun SetPixel(x: Int, y: Int, p: Pixel): Boolean

    @ExperimentalUnsignedTypes
    fun Sample(x: Float, y: Float): Pixel

    @ExperimentalUnsignedTypes
    fun SampleBL(u: Float, v: Float): Pixel

    @ExperimentalUnsignedTypes
    fun GetData(): UIntArray

    @ExperimentalUnsignedTypes
    fun GetPixels(): Array<Pixel>

    @ExperimentalUnsignedTypes
    fun SetPixels(newColData: Array<Pixel>)

    enum class Mode { NORMAL, PERIODIC }

    @ThreadLocal
    companion object {
        var nOverdrawCount: Int = 0
    }

}

class SpriteImpl : Sprite {
    constructor()

    constructor(imageFile: String) {
        LoadFromFile(imageFile)
    }

    @ExperimentalUnsignedTypes
    constructor(imageFile: String, pack: ResourcePack?) {
        LoadFromPGESprFile(imageFile, pack)
    }

    @ExperimentalUnsignedTypes
    constructor(w: Int, h: Int) {
        width = w
        height = h
        pColData = Array(width * height) { Pixel() }
    }

    @ExperimentalUnsignedTypes
    constructor(nScreenWidth: UInt, nScreenHeight: UInt) : this(nScreenWidth.toInt(), nScreenHeight.toInt())

    override fun SetSampleMode(mode: Sprite.Mode) {
        modeSample = mode
    }

    @ExperimentalUnsignedTypes
    override fun GetPixel(x: Int, y: Int): Pixel {
        return if (modeSample == Sprite.Mode.NORMAL) {
            if (x in 0 until width && y in 0 until height)
                pColData[y * width + x]
            else
                Pixel(0u)
        } else {
            pColData[abs(y % height) * width + abs(x % width)]
        }
    }

    @ExperimentalUnsignedTypes
    override fun SetPixel(x: Int, y: Int, p: Pixel): Boolean {
        nOverdrawCount++
        return if (x in 0 until width && y in 0 until height) {
            pColData[y * width + x] = p
            true
        } else
            false
    }

    @ExperimentalUnsignedTypes
    override fun Sample(x: Float, y: Float): Pixel {
        val sx = min((x * width).toInt(), width - 1)
        val sy = min((y * height).toInt(), height - 1)
        return GetPixel(sx, sy)
    }

    @ExperimentalUnsignedTypes
    override fun SampleBL(u: Float, v: Float): Pixel {
        val U = u * width - 0.5f
        val V = v * height - 0.5f
        val x = floor(U).toInt() // cast to int rounds toward zero, not downward
        val y = floor(V).toInt() // Thanks @joshinils
        val u_ratio = U - x
        val v_ratio = V - y
        val u_opposite = 1 - u_ratio
        val v_opposite = 1 - v_ratio

        val p1 = GetPixel(max(x, 0), max(y, 0))
        val p2 = GetPixel(min(x + 1, width - 1), max(y, 0))
        val p3 = GetPixel(max(x, 0), min(y + 1, height - 1))
        val p4 = GetPixel(min(x + 1, width - 1), min(y + 1, height - 1))

        return Pixel(
            ((p1.rf * u_opposite + p2.rf * u_ratio) * v_opposite + (p3.rf * u_opposite + p4.rf * u_ratio) * v_ratio).toUInt().toUByte(),
            ((p1.gf * u_opposite + p2.gf * u_ratio) * v_opposite + (p3.gf * u_opposite + p4.gf * u_ratio) * v_ratio).toUInt().toUByte(),
            ((p1.bf * u_opposite + p2.bf * u_ratio) * v_opposite + (p3.bf * u_opposite + p4.bf * u_ratio) * v_ratio).toUInt().toUByte()
        )
    }

    @ExperimentalUnsignedTypes
    override fun GetData(): UIntArray {
        return pColData.map { it.n }.toUIntArray()
    }

    @ExperimentalUnsignedTypes
    override fun GetPixels(): Array<Pixel> {
        return pColData
    }

    @ExperimentalUnsignedTypes
    override fun SetPixels(newColData: Array<Pixel>) {
        pColData = newColData
    }

    override fun LoadFromFile(imageFile: String, pack: ResourcePack?): rcode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalUnsignedTypes
    override fun LoadFromPGESprFile(imageFile: String, pack: ResourcePack?): rcode {
        val readData: (buffer: ByteArray) -> rcode = { buffer ->
            println("size: " + buffer.size)

            if (buffer.size < 12) {
                println("Failed because minimal size for file isn't met")
                rcode.FAIL
            } else {
                width = buffer.sliceArray(0..3).toInt()
                height = buffer.sliceArray(4..7).toInt()
                println("width and height: $width, $height")

                if (width <= 0 || height <= 0 || (width * height * UInt.SIZE_BYTES) > (buffer.size - Int.SIZE_BYTES * 2)) {
                    println("Failed because of the inconsistent file size")
                    rcode.FAIL
                } else {
                    pColData = buffer.slice(8 until buffer.size).take(width * height * UInt.SIZE_BYTES)
                        .map { it.toUByte() }
                        .chunked(4)
                        .map { (r, g, b, a) ->
                            Pixel(r, g, b, a)
                        }.toTypedArray()

                    rcode.OK
                }
            }
        }

        return if (pack == null) {
            val fh: CPointer<FILE> = fopen(imageFile, "r") ?: return rcode.FAIL
            try {
                readData(fh.fileToByteArray())
            } finally {
                fclose(fh)
            }
        } else {
            pack.GetStreamBuffer(imageFile)?.let { readData(it) } ?: rcode.FAIL
        }
    }

    @ExperimentalUnsignedTypes
    override fun SaveToPGESprFile(imageFile: String): rcode {
        val file: CPointer<FILE>? = fopen(imageFile, "w") ?: return rcode.FAIL

        try {
            fwrite(intArrayOf(width, height).refTo(0), (Int.SIZE_BYTES * 2).toULong(), 1, file)
            val data = pColData.map { it.n }.toUIntArray()
            fwrite(data.refTo(0), (data.size * UInt.SIZE_BYTES).toULong(), 1, file)
            fflush(file)
        } finally {
            fclose(file)
        }

        return rcode.OK
    }

    override var width = 0
    override var height = 0
    var modeSample: Sprite.Mode = Sprite.Mode.NORMAL
    @ExperimentalUnsignedTypes
    lateinit var pColData: Array<Pixel>
}

@ExperimentalUnsignedTypes
fun CPointer<FILE>.fileToByteArray(): ByteArray {
    val self = this
    var result = ByteArray(0)
    fseek(this, 0, SEEK_END)
    val fileSize = ftell(this)
    fseek(this, 0, SEEK_SET)

    memScoped {
        val buffer = allocArray<ByteVar>(fileSize)
        fread(buffer, fileSize.toULong(), 1, self)
        result = buffer.readBytes(fileSize.toInt())
    }

    return result
}

fun ByteArray.toInt(): Int {
    if (this.size != Int.SIZE_BYTES) return 0

    return this.take(4).chunked(4).map { (hh, hl, lh, ll) ->
        (ll.toInt() shl 24) or (lh.toInt() shl 16) or (hl.toInt() shl 8) or hh.toInt()
    }.first()
}
