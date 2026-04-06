package olc.game_engine

import kotlinx.cinterop.*
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFURLCreateFromFileSystemRepresentation
import platform.CoreGraphics.*
import platform.ImageIO.CGImageSourceCreateImageAtIndex
import platform.ImageIO.CGImageSourceCreateWithURL
import platform.posix.*
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.native.concurrent.ThreadLocal

private const val BITMAP_INFO_BYTE_ORDER_32_BIG_INT: Int = (4 shl 12)

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
    fun getPixel(x: Int, y: Int): Pixel {
        return if (modeSample == Mode.NORMAL) {
            if (x < 0 || x >= width || y < 0 || y >= height)
                Pixel.BLANK
            else
                Pixel(data[y * width + x])
        } else {
            Pixel(data[abs(y % height) * width + abs(x % width)])
        }
    }

    @ExperimentalUnsignedTypes
    inline fun getPixelUnchecked(x: Int, y: Int): Pixel =
        Pixel(data[y * width + x])

    @ExperimentalUnsignedTypes
    inline fun getPixelClamped(x: Int, y: Int): Pixel =
        Pixel(data[max(0, min(y, height - 1)) * width + max(0, min(x, width - 1))])

    @ExperimentalUnsignedTypes
    fun setPixel(x: Int, y: Int, p: Pixel): Boolean {
        nOverdrawCount++

        if (x < 0 || x >= width || y < 0 || y >= height)
            return false

        data[y * width + x] = p.n
        return true
    }

    @ExperimentalUnsignedTypes
    inline fun setPixelUnchecked(x: Int, y: Int, p: Pixel) {
        data[y * width + x] = p.n
    }

    @ExperimentalUnsignedTypes
    fun sample(x: Float, y: Float): Pixel {
        val sx = min((x * width).toInt(), width - 1)
        val sy = min((y * height).toInt(), height - 1)
        return getPixel(sx, sy)
    }

    @ExperimentalUnsignedTypes
    fun sampleBL(u: Float, v: Float): Pixel {
        val U = u * width - 0.5f
        val V = v * height - 0.5f
        val x = floor(U).toInt()
        val y = floor(V).toInt()
        val uFrac = ((U - x) * 256).toInt().coerceIn(0, 256)
        val vFrac = ((V - y) * 256).toInt().coerceIn(0, 256)
        val uOpp = 256 - uFrac
        val vOpp = 256 - vFrac

        val p1 = getPixelClamped(x, y)
        val p2 = getPixelClamped(x + 1, y)
        val p3 = getPixelClamped(x, y + 1)
        val p4 = getPixelClamped(x + 1, y + 1)

        val r = ((p1.ri * uOpp + p2.ri * uFrac) * vOpp + (p3.ri * uOpp + p4.ri * uFrac) * vFrac) shr 16
        val g = ((p1.gi * uOpp + p2.gi * uFrac) * vOpp + (p3.gi * uOpp + p4.gi * uFrac) * vFrac) shr 16
        val b = ((p1.bi * uOpp + p2.bi * uFrac) * vOpp + (p3.bi * uOpp + p4.bi * uFrac) * vFrac) shr 16
        return Pixel(r, g, b)
    }

    @OptIn(kotlin.experimental.ExperimentalNativeApi::class)
    fun loadFromFile(imageFile: String, pack: ResourcePack? = null): rcode {
        if (imageFile.endsWith(".spr", ignoreCase = true)) {
            return loadFromPGESprFile(imageFile, pack)
        }

        if (pack != null) {
            println("Sprite.loadFromFile: ResourcePack loading for images is not supported for '$imageFile'")
            return rcode.FAIL
        }

        val resolvedPath = resolveResourcePath(imageFile) ?: run {
            println("Sprite.loadFromFile: unable to locate '$imageFile'")
            return rcode.NO_FILE
        }

        return when (Platform.osFamily) {
            OsFamily.MACOSX -> loadFromFileMac(resolvedPath)
            else -> {
                println("Sprite.loadFromFile: PNG loading is not implemented for ${Platform.osFamily}.")
                rcode.FAIL
            }
        }
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
                    val pixelCount = width * height
                    data = UIntArray(pixelCount) { i ->
                        val base = 8 + i * 4
                        val r = buffer[base].toInt() and 0xFF
                        val g = buffer[base + 1].toInt() and 0xFF
                        val b = buffer[base + 2].toInt() and 0xFF
                        val a = buffer[base + 3].toInt() and 0xFF
                        ((a shl 24) or (b shl 16) or (g shl 8) or r).toUInt()
                    }

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

private fun Sprite. loadFromFileMac(path: String): rcode = memScoped {
    val pathBytes = path.cstr.getPointer(this)
    val url = CFURLCreateFromFileSystemRepresentation(
        allocator = null,
        buffer = pathBytes.reinterpret(),
        bufLen = path.length.convert(),
        isDirectory = false
    ) ?: return rcode.FAIL

    val imageSource = CGImageSourceCreateWithURL(url, null)
    if (imageSource == null) {
        CFRelease(url)
        return rcode.FAIL
    }

    val image = CGImageSourceCreateImageAtIndex(imageSource, 0u, null)
    if (image == null) {
        CFRelease(imageSource)
        CFRelease(url)
        return rcode.FAIL
    }

    val width = CGImageGetWidth(image).toInt()
    val height = CGImageGetHeight(image).toInt()
    if (width <= 0 || height <= 0) {
        CGImageRelease(image)
        CFRelease(imageSource)
        CFRelease(url)
        return rcode.FAIL
    }

    val bytesPerRow = width * 4
    val colorSpace = CGColorSpaceCreateDeviceRGB()
    if (colorSpace == null) {
        CGImageRelease(image)
        CFRelease(imageSource)
        CFRelease(url)
        return rcode.FAIL
    }

    val bitmapInfo = CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value or BITMAP_INFO_BYTE_ORDER_32_BIG_INT.toUInt()
    val context = CGBitmapContextCreate(
        data = null,
        width = width.toULong(),
        height = height.toULong(),
        bitsPerComponent = 8u,
        bytesPerRow = bytesPerRow.toULong(),
        space = colorSpace,
        bitmapInfo = bitmapInfo
    )

    if (context == null) {
        CGColorSpaceRelease(colorSpace)
        CGImageRelease(image)
        CFRelease(imageSource)
        CFRelease(url)
        return rcode.FAIL
    }

    val drawRect = CGRectMake(0.0, 0.0, width.toDouble(), height.toDouble())
    CGContextDrawImage(context, drawRect, image)

    val dataPointer = CGBitmapContextGetData(context)?.reinterpret<UByteVar>()
    if (dataPointer == null) {
        CGContextRelease(context)
        CGColorSpaceRelease(colorSpace)
        CGImageRelease(image)
        CFRelease(imageSource)
        CFRelease(url)
        return rcode.FAIL
    }

    val bufferSize = bytesPerRow * height
    val rawBytes = ByteArray(bufferSize)
    rawBytes.usePinned { pinned ->
        memcpy(pinned.addressOf(0), dataPointer, bufferSize.convert())
    }

    val pixelCount = width * height
    val pixels = UIntArray(pixelCount)
    for (i in 0 until pixelCount) {
        val base = i * 4
        val r = rawBytes[base].toInt() and 0xFF
        val g = rawBytes[base + 1].toInt() and 0xFF
        val b = rawBytes[base + 2].toInt() and 0xFF
        val a = rawBytes[base + 3].toInt() and 0xFF
        pixels[i] = ((a shl 24) or (b shl 16) or (g shl 8) or r).toUInt()
    }

    this@loadFromFileMac.width = width
    this@loadFromFileMac.height = height
    this@loadFromFileMac.data = pixels

    CGContextRelease(context)
    CGColorSpaceRelease(colorSpace)
    CGImageRelease(image)
    CFRelease(imageSource)
    CFRelease(url)

    return rcode.OK
}

private fun resolveResourcePath(imageFile: String): String? {
    val envPath = getenv("OLC_RESOURCE_DIR")?.toKString()

    val primaryRoots = listOf(
        "",
        "resources",
        "engine/src/nativeMain/resources",
        "engine/src/nativeTest/resources",
        "demos/shared-assets/src/nativeMain/resources",
        "demos/bejewelled/src/nativeMain/resources",
        "demos/dungeon_warping/src/nativeMain/resources",
        "games/pixel_shooter/src/nativeMain/resources"
    )

    val prefixes = generateSequence("") { current -> "$current../" }.take(6).toList()

    val candidates = mutableListOf<String>()
    if (!envPath.isNullOrBlank()) {
        candidates += if (envPath.endsWith('/')) envPath + imageFile else "$envPath/$imageFile"
    }

    for (prefix in prefixes) {
        for (root in primaryRoots) {
            val partial = if (root.isEmpty()) imageFile else "$root/$imageFile"
            candidates += prefix + partial
        }
    }

    for (candidate in candidates.distinct()) {
        if (fileExists(candidate)) return candidate
    }

    return null
}

private fun fileExists(path: String): Boolean = access(path, F_OK) == 0

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
    if (size != Int.SIZE_BYTES) return 0
    return ((this[3].toInt() and 0xFF) shl 24) or
           ((this[2].toInt() and 0xFF) shl 16) or
           ((this[1].toInt() and 0xFF) shl 8) or
           (this[0].toInt() and 0xFF)
}
