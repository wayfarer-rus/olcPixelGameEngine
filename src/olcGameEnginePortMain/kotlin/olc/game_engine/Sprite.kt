package olc.game_engine

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

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

open class Sprite @ExperimentalUnsignedTypes constructor(var data: UIntArray = UIntArray(0)) {
    enum class Mode { NORMAL, PERIODIC }

    @ThreadLocal
    companion object {
        var nOverdrawCount: Int = 0
    }

    @ExperimentalUnsignedTypes
    constructor(imageFile: String) : this() {
        loadFromFile(imageFile)
    }

    @ExperimentalUnsignedTypes
    constructor(imageFile: String, pack: ResourcePack?) : this() {
        loadFromPGESprFile(imageFile, pack)
    }

    @ExperimentalUnsignedTypes
    constructor(w: Int, h: Int) : this(UIntArray(w * h)) {
        width = w
        height = h
    }

    fun setSampleMode(mode: Mode) {
        modeSample = mode
    }

    @ExperimentalUnsignedTypes
    fun getPixel(x: Int, y: Int): Pixel? {
        return if (modeSample == Mode.NORMAL) {
            if (x < 0 || x >= width || y < 0 || y >= height)
                null
            else
                Pixel(data[y * width + x])
        } else {
            Pixel(data[abs(y % height) * width + abs(x % width)])
        }
    }

    @ExperimentalUnsignedTypes
    fun setPixel(x: Int, y: Int, p: Pixel): Boolean {
        nOverdrawCount++

        if (x < 0 || x >= width || y < 0 || y >= height)
            return false

        data[y * width + x] = p.n
        return true
    }

    @ExperimentalUnsignedTypes
    fun sample(x: Float, y: Float): Pixel? {
        val sx = min((x * width).toInt(), width - 1)
        val sy = min((y * height).toInt(), height - 1)
        return getPixel(sx, sy)
    }

    @ExperimentalUnsignedTypes
    fun sampleBL(u: Float, v: Float): Pixel {
        val U = u * width - 0.5f
        val V = v * height - 0.5f
        val x = floor(U).toInt() // cast to int rounds toward zero, not downward
        val y = floor(V).toInt() // Thanks @joshinils
        val uRatio = U - x
        val vRatio = V - y
        val uOpposite = 1 - uRatio
        val vOpposite = 1 - vRatio

        val p1 = getPixel(max(x, 0), max(y, 0)) ?: Pixel.BLANK
        val p2 = getPixel(min(x + 1, width - 1), max(y, 0)) ?: Pixel.BLANK
        val p3 = getPixel(max(x, 0), min(y + 1, height - 1)) ?: Pixel.BLANK
        val p4 = getPixel(min(x + 1, width - 1), min(y + 1, height - 1)) ?: Pixel.BLANK

        return Pixel(
            ((p1.rf * uOpposite + p2.rf * uRatio) * vOpposite + (p3.rf * uOpposite + p4.rf * uRatio) * vRatio).toUInt().toUByte(),
            ((p1.gf * uOpposite + p2.gf * uRatio) * vOpposite + (p3.gf * uOpposite + p4.gf * uRatio) * vRatio).toUInt().toUByte(),
            ((p1.bf * uOpposite + p2.bf * uRatio) * vOpposite + (p3.bf * uOpposite + p4.bf * uRatio) * vRatio).toUInt().toUByte()
        )
    }

    fun loadFromFile(imageFile: String, pack: ResourcePack? = null): rcode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @ExperimentalUnsignedTypes
    fun loadFromPGESprFile(imageFile: String, pack: ResourcePack? = null): rcode {
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
                    data = buffer.slice(8 until buffer.size).take(width * height * UInt.SIZE_BYTES)
                        .map { it.toUByte() }
                        .chunked(4)
                        .map { (r, g, b, a) ->
                            Pixel(r, g, b, a).n
                        }.toUIntArray()

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
    fun saveToPGESprFile(imageFile: String): rcode {
        val file: CPointer<FILE> = fopen(imageFile, "w") ?: return rcode.FAIL

        try {
            fwrite(intArrayOf(width, height).refTo(0), (Int.SIZE_BYTES * 2).toULong(), 1UL, file)
            fwrite(data.refTo(0), (data.size * UInt.SIZE_BYTES).toULong(), 1UL, file)
            fflush(file)
        } finally {
            fclose(file)
        }

        return rcode.OK
    }

    var width = 0
    var height = 0
    private var modeSample = Mode.NORMAL
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
        fread(buffer, fileSize.toULong(), 1UL, self)
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
