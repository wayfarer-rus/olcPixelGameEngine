package olc.game_engine

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

@ExperimentalUnsignedTypes
inline class Pixel(inline val n: UInt = 0xFF000000u) {

    constructor(red: UByte, green: UByte, blue: UByte, alpha: UByte = 255u) :
            this((alpha.toUInt() shl 24) or (blue.toUInt() shl 16) or (green.toUInt() shl 8) or red.toUInt())

    constructor(red: Int, green: Int, blue: Int, alpha: Int = 0xFF) : this(
        red.toUByte(),
        green.toUByte(),
        blue.toUByte(),
        alpha.toUByte()
    )

    constructor(red: Float, green: Float, blue: Float) : this(red.toInt(), green.toInt(), blue.toInt())

    val rf: Float
        inline get() = r.toFloat()
    val gf: Float
        inline get() = g.toFloat()
    val bf: Float
        inline get() = b.toFloat()
    val af: Float
        inline get() = a.toFloat()

    val r: UByte
        inline get() {
            return (0x000000FFu and n).toUByte()
        }
    val g: UByte
        inline get() {
            return (0x0000FF00u and n shr 8).toUByte()
        }
    val b: UByte
        inline get() {
            return (0x00FF0000u and n shr 16).toUByte()
        }
    val a: UByte
        inline get() {
            return (0xFF000000u and n shr 24).toUByte()
        }
    val ni: Int
        inline get() {
            return n.toInt()
        }

    enum class Mode {
        NORMAL, MASK, ALPHA, CUSTOM
    }

    companion object {
        val WHITE = Pixel(255u, 255u, 255u)
        val GREY = Pixel(192u, 192u, 192u)
        val DARK_GREY = Pixel(128u, 128u, 128u)
        val VERY_DARK_GREY = Pixel(64u, 64u, 64u)
        val RED = Pixel(255u, 0u, 0u)
        val DARK_RED = Pixel(128u, 0u, 0u)
        val VERY_DARK_RED = Pixel(64u, 0u, 0u)
        val YELLOW = Pixel(255u, 255u, 0u)
        val DARK_YELLOW = Pixel(128u, 128u, 0u)
        val VERY_DARK_YELLOW = Pixel(64u, 64u, 0u)
        val GREEN = Pixel(0u, 255u, 0u)
        val DARK_GREEN = Pixel(0u, 128u, 0u)
        val VERY_DARK_GREEN = Pixel(0u, 64u, 0u)
        val CYAN = Pixel(0u, 255u, 255u)
        val DARK_CYAN = Pixel(0u, 128u, 128u)
        val VERY_DARK_CYAN = Pixel(0u, 64u, 64u)
        val BLUE = Pixel(0u, 0u, 255u)
        val DARK_BLUE = Pixel(0u, 0u, 128u)
        val VERY_DARK_BLUE = Pixel(0u, 0u, 64u)
        val MAGENTA = Pixel(255u, 0u, 255u)
        val DARK_MAGENTA = Pixel(128u, 0u, 128u)
        val VERY_DARK_MAGENTA = Pixel(64u, 0u, 64u)
        val BLACK = Pixel(0u, 0u, 0u)
        val BLANK = Pixel(0u, 0u, 0u, 0u)
    }

    override fun toString(): String {
        return "Pixel[$r, $g, $b, $a]"
    }
}