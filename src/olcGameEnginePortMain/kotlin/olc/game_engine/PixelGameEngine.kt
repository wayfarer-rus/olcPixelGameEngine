package olc.game_engine

import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.system.getTimeNanos

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
interface PixelGameEngine {
    val appName: String

    fun construct(
        screen_w: Int = 256,
        screen_h: Int = 192,
        pixel_w: Int = 4,
        pixel_h: Int = 4,
        full_screen: Boolean = false,
        v_sync: Boolean = false
    ): RetCode

    fun start(): RetCode

    fun onUserCreate(): Boolean
    fun onUserUpdate(elapsedTime: Float): Boolean
    fun onUserDestroy() {}

    fun isFocused(): Boolean
    fun isMouseInWindow(): Boolean
    fun getKey(k: Key): HWButton
    fun getMouseKey(b: Int): HWButton
    fun getMouse(b: Int) = getMouseKey(b)
    fun getMouseX(): Int
    fun getMouseY(): Int
    fun getMouseWheel(): Int
    fun screenWidth(): Int
    fun screenHeight(): Int
    fun getDrawTargetWidth(): Int
    fun getDrawTargetHeight(): Int
    fun getDrawTarget(): Sprite
    fun setDrawTarget(target: Sprite?)
    fun setDrawTarget(layer: Int)
    fun resetDrawTarget()
    fun setPixelMode(m: Pixel.Mode)

    // fun SetPixelMode(fun)
    fun getPixelMode(): Pixel.Mode

    fun setPixelBlend(blend: Float)
    fun setSubPixelOffset(ox: Float, oy: Float)

    fun draw(x: Int, y: Int, p: Pixel = Pixel.WHITE)

    fun drawLine(start: Pair<Int, Int>, end: Pair<Int, Int>, p: Pixel = Pixel.WHITE, pattern: UInt = 0xFFFFFFFFu)
    fun drawCircle(x: Int, y: Int, radius: Int, p: Pixel = Pixel.WHITE, mask: UByte = 0xFFu)
    fun drawCircle(pos: Vi2d, radius: Int, p: Pixel = Pixel.WHITE, mask: UByte = 0xFFu) =
        drawCircle(pos.x, pos.y, radius, p, mask)

    fun fillCircle(x: Int, y: Int, radius: Int, p: Pixel = Pixel.WHITE)
    fun drawRect(x: Int, y: Int, w: Int, h: Int, p: Pixel = Pixel.WHITE)
    fun drawRect(pos: Vi2d, size: Vi2d, p: Pixel = Pixel.WHITE) = drawRect(pos.x, pos.y, size.x, size.y, p)
    fun fillRect(x: Int, y: Int, w: Int, h: Int, p: Pixel = Pixel.WHITE)
    fun drawTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun fillTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun drawSprite(x: Int, y: Int, sprite: Sprite, scale: Int = 1)
    fun drawSprite(pos: Vi2d, sprite: Sprite, scale: Int = 1) = drawSprite(pos.x, pos.y, sprite, scale)
    fun drawPartialSprite(x: Int, y: Int, sprite: Sprite, ox: Int, oy: Int, w: Int, h: Int, scale: Int = 1)

    // Draws a whole decal, with optional scale and tinting
    fun drawDecal(pos: Vf2d, decal: Decal, scale: Vf2d = Vf2d(1.0f, 1.0f), tint: Pixel = Pixel.WHITE)

    // Draws a region of a decal, with optional scale and tinting
    fun drawPartialDecal(
        pos: Vf2d,
        decal: Decal,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d = Vf2d(1.0f, 1.0f),
        tint: Pixel = Pixel.WHITE
    )

    fun drawWarpedDecal(decal: Decal, pos: Array<Vf2d>, tint: Pixel = Pixel.WHITE)
    fun drawWarpedDecal(decal: Decal, pos: List<Vf2d>, tint: Pixel = Pixel.WHITE)

    fun drawRotatedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d = Vf2d(0.0f, 0.0f),
        scale: Vf2d = Vf2d(1.0f, 1.0f),
        tint: Pixel = Pixel.WHITE
    )

    fun drawStringDecal(pos: Vf2d, text: String, col: Pixel = Pixel.WHITE, scale: Vf2d = Vf2d(1.0f, 1.0f))
    fun drawPartialRotatedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d = Vf2d(1.0f, 1.0f),
        tint: Pixel = Pixel.WHITE
    )

    fun drawPartialWarpedDecal(
        decal: Decal,
        pos: Array<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel = Pixel.WHITE
    )

    fun drawPartialWarpedDecal(
        decal: Decal,
        pos: List<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel = Pixel.WHITE
    )

    // Draws a single line of text
    fun drawString(x: Int, y: Int, text: String, col: Pixel = Pixel.WHITE, scale: Int = 1)
    fun drawString(pos: Vi2d, text: String, col: Pixel = Pixel.WHITE, scale: Int = 1)

    fun clear(p: Pixel = Pixel.BLACK)
    fun getFPS(): Int

    fun createDecal(sprite: Sprite): Decal
    fun updateDecal(decal: Decal): Decal
    fun deleteDecal(decal: Decal)

    fun createLayer(): Int
    fun deleteLayer(layerId: Int)
}

@ExperimentalUnsignedTypes
abstract class PixelGameEngineImpl : PixelGameEngine {
    private val spriteDecalInternalMap = mutableMapOf<Sprite, Decal>()

    // currently there is only GLFW platform and renderer
    // when I add another one, I will think about configuratoin option
    private val renderer: Renderer = RendererGlfwImpl()
    private val platform: Platform = PlatformGlfwImpl(this, renderer)

    override val appName: String = "Undefined"

    override fun construct(
        screen_w: Int,
        screen_h: Int,
        pixel_w: Int,
        pixel_h: Int,
        full_screen: Boolean,
        v_sync: Boolean
    ): RetCode {
        println("pge::construct called")
        screenSize = Vi2d(screen_w, screen_h)
        vInvScreenSize = Vf2d(1.0f / screen_w.toFloat(), 1.0f / screen_h.toFloat())
        pixelSize = Vi2d(pixel_w, pixel_h)
        windowSize = screenSize * pixelSize
        fullScreen = full_screen
        enableVsync = v_sync

        fPixelX = 2.0f / nScreenWidth.toFloat()
        fPixelY = 2.0f / nScreenHeight.toFloat()

        if (nPixelWidth <= 0 || nPixelHeight <= 0 || nScreenWidth <= 0 || nScreenHeight <= 0)
            return RetCode.FAIL

        return RetCode.OK
    }

    override fun start(): RetCode {
        println("pge::start called")
        if (platform.applicationStartUp() != RetCode.OK) return RetCode.FAIL

        println("Construct window")
        if (platform.createWindowPane(Vi2d(30, 30), this.windowSize, fullScreen) != RetCode.OK)
            return RetCode.FAIL

        platform.startSystemEventLoop()

        engineMainLoop()



        return platform.applicationCleanUp()
    }

    // Utility methods and flow control
    override fun setDrawTarget(target: Sprite?) {
        if (target != null) {
            pDrawTarget = target
        } else {
            targetLayer = 0
            pDrawTarget = layers[0].drawTarget!!
        }
    }

    override fun setDrawTarget(layer: Int) {
        if (layer < layers.size) {
            pDrawTarget = layers[layer].drawTarget!!
            layers[layer].update = true
            targetLayer = layer
        }
    }

    override fun resetDrawTarget() {
        pDrawTarget = pDefaultDrawTarget
    }

    override fun getFPS(): Int {
        return fps
    }

    override fun isFocused(): Boolean {
        return focusState.bHasInputFocus
    }

    override fun isMouseInWindow(): Boolean {
        return focusState.bHasMouseFocus
    }

    override fun getKey(k: Key): HWButton {
        return pKeyboardState[k.ordinal]
    }

    override fun getMouseKey(b: Int): HWButton {
        return pMouseState[b]
    }

    override fun getMouseX(): Int {
        return nMousePosX
    }

    override fun getMouseY(): Int {
        return nMousePosY
    }

    override fun getMouseWheel(): Int {
        return nMouseWheelDelta
    }

    override fun screenWidth(): Int {
        return nScreenWidth
    }

    override fun screenHeight(): Int {
        return nScreenHeight
    }

    override fun getDrawTargetWidth(): Int {
        return pDrawTarget.width
    }

    override fun getDrawTargetHeight(): Int {
        return pDrawTarget.height
    }

    override fun getDrawTarget(): Sprite {
        return pDrawTarget
    }

    override fun setPixelMode(m: Pixel.Mode) {
        nPixelMode = m
    }

    override fun getPixelMode(): Pixel.Mode {
        return nPixelMode
    }

    override fun setPixelBlend(blend: Float) {
        fBlendFactor = blend
        if (fBlendFactor < 0.0f) fBlendFactor = 0.0f
        if (fBlendFactor > 1.0f) fBlendFactor = 1.0f
    }

    override fun setSubPixelOffset(ox: Float, oy: Float) {
        fSubPixelOffsetX = ox * fPixelX
        fSubPixelOffsetY = oy * fPixelY
    }

    // Drawing commands
    override fun draw(x: Int, y: Int, p: Pixel) {
        pDrawTarget.apply {
            when (nPixelMode) {
                Pixel.Mode.NORMAL -> setPixel(x, y, p)
                Pixel.Mode.MASK -> if (255.toUByte() == p.a) setPixel(x, y, p)
                Pixel.Mode.ALPHA -> {
                    getPixel(x, y)?.let { d ->
                        val a = (p.af / 255.0f) * fBlendFactor
                        val c = 1.0f - a
                        val r = a * p.rf + c * d.rf
                        val g = a * p.gf + c * d.gf
                        val b = a * p.bf + c * d.bf
                        Pixel(r, g, b)
                    }?.let { setPixel(x, y, it) }
                }
                Pixel.Mode.CUSTOM -> this.setPixel(
                    x, y, getPixel(x, y)?.let { funcPixelMode?.invoke(x, y, p, it) } ?: Pixel(0u)
                )
            }
        }
    }

    override fun drawString(pos: Vi2d, text: String, col: Pixel, scale: Int) {
        drawString(pos.x, pos.y, text, col, scale)
    }

    override fun drawString(x: Int, y: Int, text: String, col: Pixel, scale: Int) {
        var sx = 0
        var sy = 0
        val m = nPixelMode

        if (col.a != 255.toUByte()) setPixelMode(Pixel.Mode.ALPHA)
        else setPixelMode(Pixel.Mode.MASK)

        for (c in text) {
            if (c == '\n') {
                sx = 0
                sy += 8 * scale
            } else {
                val ox = (c.toInt() - 32) % 16
                val oy = (c.toInt() - 32) / 16

                if (scale > 1) {
                    for (i in 0 until 8)
                        for (j in 0 until 8)
                            if (fontSprite.getPixel(i + ox * 8, j + oy * 8)!!.r > 0.toUByte())
                                for (`is` in 0 until scale)
                                    for (js in 0 until scale)
                                        draw(
                                            x + sx + (i * scale) + `is`,
                                            y + sy + (j * scale) + js,
                                            col
                                        )
                } else {
                    for (i in 0 until 8)
                        for (j in 0 until 8)
                            if (fontSprite.getPixel(i + ox * 8, j + oy * 8)!!.r > 0.toUByte())
                                draw(x + sx + i, y + sy + j, col)
                }
                sx += 8 * scale
            }
        }

        setPixelMode(m)
    }

    override fun clear(p: Pixel) {
        pDrawTarget.data.fill(p.n)
        Sprite.nOverdrawCount += pDrawTarget.data.size
    }

    override fun drawLine(start: Pair<Int, Int>, end: Pair<Int, Int>, p: Pixel, pattern: UInt) {
        var (x1, y1) = start
        var (x2, y2) = end
        var pn = pattern
        val dx = x2 - x1
        val dy = y2 - y1

        val rol: () -> Boolean = {
            pn = (pn shl 1) or (pn shr 31)
            (pn and 1u) > 0u
        }

        // straight lines idea by gurkanctn
        if (dx == 0) // Line is vertical
        {
            if (y2 < y1) y1 = y2.also { y2 = y1 }
            for (y in y1..y2)
                if (rol()) draw(x1, y, p)
            return
        }

        if (dy == 0) // Line is horizontal
        {
            if (x2 < x1) x1 = x2.also { x2 = x1 }
            for (x in x1..x2)
                if (rol()) draw(x, y1, p)
            return
        }

        // Line is Funk-aye
        val dx1 = abs(dx)
        val dy1 = abs(dy)
        var px = 2 * dy1 - dx1
        var py = 2 * dx1 - dy1
        var x: Int
        var y: Int
        val xe: Int
        val ye: Int

        if (dy1 <= dx1) {
            if (dx >= 0) {
                x = x1; y = y1; xe = x2
            } else {
                x = x2; y = y2; xe = x1
            }

            if (rol()) draw(x, y, p)

            while (x < xe) {
                x += 1
                if (px < 0)
                    px += 2 * dy1
                else {
                    y = if ((dx < 0 && dy < 0) || (dx > 0 && dy > 0)) y + 1; else y - 1
                    px += 2 * (dy1 - dx1)
                }
                if (rol()) draw(x, y, p)
            }
        } else {
            if (dy >= 0) {
                x = x1; y = y1; ye = y2
            } else {
                x = x2; y = y2; ye = y1
            }

            if (rol()) draw(x, y, p)

            while (y < ye) {
                y += 1
                if (py <= 0)
                    py += 2 * dx1
                else {
                    x = if ((dx < 0 && dy < 0) || (dx > 0 && dy > 0)) x + 1; else x - 1
                    py += 2 * (dx1 - dy1)
                }
                if (rol()) draw(x, y, p)
            }
        }
    }

    override fun drawCircle(x: Int, y: Int, radius: Int, p: Pixel, mask: UByte) {
        var x0 = 0
        var y0 = radius
        var d = 3 - 2 * radius
        if (radius <= 0) return

        while (y0 >= x0) // only formulate 1/8 of circle
        {
            if (mask and 0x01u > 0u) draw(x + x0, y - y0, p)
            if (mask and 0x02u > 0u) draw(x + y0, y - x0, p)
            if (mask and 0x04u > 0u) draw(x + y0, y + x0, p)
            if (mask and 0x08u > 0u) draw(x + x0, y + y0, p)
            if (mask and 0x10u > 0u) draw(x - x0, y + y0, p)
            if (mask and 0x20u > 0u) draw(x - y0, y + x0, p)
            if (mask and 0x40u > 0u) draw(x - y0, y - x0, p)
            if (mask and 0x80u > 0u) draw(x - x0, y - y0, p)
            d += if (d < 0) (4 * x0++ + 6)
            else (4 * (x0++ - y0--) + 10)
        }
    }

    override fun fillCircle(x: Int, y: Int, radius: Int, p: Pixel) {
        // Taken from wikipedia
        var x0 = 0
        var y0 = radius
        var d = 3 - 2 * radius
        if (radius <= 0) return

        val drawLine: (Int, Int, Int) -> Unit = { sx, ex, ny ->
            for (i in sx..ex) draw(i, ny, p)
        }

        while (y0 >= x0) {
            // Modified to draw scan-lines instead of edges
            drawLine(x - x0, x + x0, y - y0)
            drawLine(x - y0, x + y0, y - x0)
            drawLine(x - x0, x + x0, y + y0)
            drawLine(x - y0, x + y0, y + x0)
            d += if (d < 0) 4 * x0++ + 6
            else 4 * (x0++ - y0--) + 10
        }
    }

    override fun drawRect(x: Int, y: Int, w: Int, h: Int, p: Pixel) {
        drawLine(x to y, x + w to y, p)
        drawLine(x + w to y, x + w to y + h, p)
        drawLine(x + w to y + h, x to y + h, p)
        drawLine(x to y + h, x to y, p)
    }

    override fun fillRect(x: Int, y: Int, w: Int, h: Int, p: Pixel) {
        val probeRange: (Int, Int, Int) -> Int = { value, begin, end ->
            when {
                value < begin -> begin
                value >= end -> end
                else -> value
            }
        }

        val x1 = probeRange(x, 0, nScreenWidth)
        val y1 = probeRange(y, 0, nScreenHeight)
        val x2 = probeRange(x + w, 0, nScreenWidth)
        val y2 = probeRange(y + h, 0, nScreenHeight)

        for (i in x1 until x2)
            for (j in y1 until y2)
                draw(i, j, p)
    }

    override fun drawTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel) {
        drawLine(point1, point2, p)
        drawLine(point2, point3, p)
        drawLine(point3, point1, p)
    }

    override fun fillTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel) {
        val drawLine: (Int, Int, Int) -> Unit = { sx, ex, ny ->
            for (i in sx..ex) draw(i, ny, p)
        }

        val (A, B, C) = listOf(point1, point2, point3).sortedBy { it.second }

        val dx1 = if (B.second - A.second > 0) (B.first - A.first) / (B.second - A.second) else 0
        val dx2 = if (C.second - A.second > 0) (C.first - A.first) / (C.second - A.second) else 0
        val dx3 = if (C.second - B.second > 0) (C.first - B.first) / (C.second - B.second) else 0

        var s = A
        var e = A

        if (dx1 > dx2) {
            while (s.second <= B.second) {
                drawLine(s.first, e.first, s.second)
                s = Pair(s.first + dx2, s.second + 1)
                e = Pair(e.first + dx1, e.second + 1)
            }

            e = B
            while (s.second <= C.second) {
                drawLine(s.first, e.first, s.second)
                s = Pair(s.first + dx2, s.second + 1)
                e = Pair(e.first + dx3, e.second + 1)
            }
        } else {
            while (s.second <= B.second) {
                drawLine(s.first, e.first, s.second)
                s = Pair(s.first + dx1, s.second + 1)
                e = Pair(e.first + dx2, e.second + 1)
            }

            s = B
            while (s.second <= C.second) {
                drawLine(s.first, e.first, s.second)
                s = Pair(s.first + dx3, s.second + 1)
                e = Pair(e.first + dx2, e.second + 1)
            }
        }
    }

    override fun drawSprite(x: Int, y: Int, sprite: Sprite, scale: Int) {
        val decal = if (this.spriteDecalInternalMap.containsKey(sprite))
            this.spriteDecalInternalMap[sprite]!!
        else {
            val decal = createDecal(sprite)
            this.spriteDecalInternalMap[sprite] = decal
            decal
        }

        drawDecal(Vf2d(x, y), decal.apply { dirty = true }, Vf2d(scale, scale))

        /*if (scale > 1) {
            for (i in 0 until sprite.width)
                for (j in 0 until sprite.height)
                    for (`is` in 0 until scale)
                        for (js in 0 until scale)
                            draw(
                                x + (i * scale) + `is`,
                                y + (j * scale) + js,
                                sprite.getPixel(i, j)!!
                            )
        } else {
            for (i in 0 until sprite.width)
                for (j in 0 until sprite.height)
                    draw(x + i, y + j, sprite.getPixel(i, j)!!)
        }*/
    }

    override fun drawPartialSprite(x: Int, y: Int, sprite: Sprite, ox: Int, oy: Int, w: Int, h: Int, scale: Int) {
        if (nPixelMode == Pixel.Mode.NORMAL) {
            // try to speed up things a bit ;)
            if (scale > 1) {
                TODO("not implemented")
            } else {
                for (j in 0 until h) {
                    try { // safety first =)
                        sprite.data.copyInto(
                            pDrawTarget.data,
                            (y + j) * pDrawTarget.width + x,
                            (oy + j) * sprite.width + ox,
                            (oy + j) * sprite.width + ox + w
                        )
                    } catch (err: ArrayIndexOutOfBoundsException) {
                        println(err)
                        println("------- $j -------")
                        println("target length: ${pDrawTarget.data.size}")
                        println("target offset: " + ((y + j) * pDrawTarget.width + x))
                        println("source length: " + sprite.data.size)
                        println("source start: " + ((oy + j) * sprite.width + ox))
                        println("source end: " + ((oy + j) * sprite.width + ox + w))
                    }
                }
            }
        } else {
            // some kind of blending enabled...
            // performance will degrade
            if (scale > 1) {
                for (i in 0 until w)
                    for (j in 0 until h)
                        for (`is` in 0 until scale)
                            for (js in 0 until scale)
                                sprite.getPixel(i + ox, j + oy)?.let {
                                    draw(x + (i * scale) + `is`, y + (j * scale) + js, it)
                                }
            } else {
                for (i in 0 until w)
                    for (j in 0 until h)
                        sprite.getPixel(i + ox, j + oy)?.let { draw(x + i, y + j, it) }
            }
        }
    }

    override fun drawDecal(pos: Vf2d, decal: Decal, scale: Vf2d, tint: Pixel) {
        val vScreenSpacePos = Vf2d(
            (pos.x * vInvScreenSize.x) * 2.0f - 1.0f,
            ((pos.y * vInvScreenSize.y) * 2.0f - 1.0f) * -1.0f
        )

        val vScreenSpaceDim = Vf2d(
            vScreenSpacePos.x + (2.0f * ((decal.sprite.width) * vInvScreenSize.x)) * scale.x,
            vScreenSpacePos.y - (2.0f * ((decal.sprite.height) * vInvScreenSize.y)) * scale.y
        )

        val di = DecalInstance(
            decal = decal,
            tint = tint,
            pos = arrayOf(
                Vf2d(vScreenSpacePos.x, vScreenSpacePos.y),
                Vf2d(vScreenSpacePos.x, vScreenSpaceDim.y),
                Vf2d(vScreenSpaceDim.x, vScreenSpaceDim.y),
                Vf2d(vScreenSpaceDim.x, vScreenSpacePos.y)
            )
        )

        layers[targetLayer].decalInstanceList.add(di)
    }

    // Draws a region of a decal, with optional scale and tinting
    override fun drawPartialDecal(
        pos: Vf2d,
        decal: Decal,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        val vScreenSpacePos = Vf2d(
            (pos.x * vInvScreenSize.x) * 2.0f - 1.0f,
            ((pos.y * vInvScreenSize.y) * 2.0f - 1.0f) * -1.0f
        )

        val vScreenSpaceDim = Vf2d(
            vScreenSpacePos.x + (2.0f * source_size.x * vInvScreenSize.x) * scale.x,
            vScreenSpacePos.y - (2.0f * source_size.y * vInvScreenSize.y) * scale.y
        )

        val uvtl: Vf2d = source_pos * decal.uvScale
        val uvbr: Vf2d = uvtl + (source_size * decal.uvScale)
        val di = DecalInstance(
            decal = decal,
            tint = tint,
            pos = arrayOf(
                Vf2d(vScreenSpacePos.x, vScreenSpacePos.y),
                Vf2d(vScreenSpacePos.x, vScreenSpaceDim.y),
                Vf2d(vScreenSpaceDim.x, vScreenSpaceDim.y),
                Vf2d(vScreenSpaceDim.x, vScreenSpacePos.y)
            ),
            uv = arrayOf(
                Vf2d(uvtl.x, uvtl.y),
                Vf2d(uvtl.x, uvbr.y),
                Vf2d(uvbr.x, uvbr.y),
                Vf2d(uvbr.x, uvtl.y)
            )
        )

        layers[targetLayer].decalInstanceList.add(di)
    }

    override fun drawWarpedDecal(decal: Decal, pos: Array<Vf2d>, tint: Pixel) {
        val di = DecalInstance(
            decal = decal,
            tint = tint
        )
        var center = Vf2d(0, 0)

        var rd = ((pos[2].x - pos[0].x) * (pos[3].y - pos[1].y) - (pos[3].x - pos[1].x) * (pos[2].y - pos[0].y))

        if (rd != 0.0f) {
            rd = 1.0f / rd
            val rn =
                ((pos[3].x - pos[1].x) * (pos[0].y - pos[1].y) - (pos[3].y - pos[1].y) * (pos[0].x - pos[1].x)) * rd
            val sn =
                ((pos[2].x - pos[0].x) * (pos[0].y - pos[1].y) - (pos[2].y - pos[0].y) * (pos[0].x - pos[1].x)) * rd

            if (!(rn < 0.0f || rn > 1.0f || sn < 0.0f || sn > 1.0f)) center = pos[0] + (pos[2] - pos[0]) * rn

            val d = (0 until 4).map { (pos[it] - center).mag() }

            for (i in 0 until 4) {
                val q =
                    if (d[i] == 0.0f) 1.0f
                    else (d[i] + d[(i + 2) and 3]) / d[(i + 2) and 3]

                di.uv[i] = di.uv[i] * q
                di.w[i] *= q
                di.pos[i] = Vf2d(
                    (pos[i].x * vInvScreenSize.x) * 2.0f - 1.0f,
                    ((pos[i].y * vInvScreenSize.y) * 2.0f - 1.0f) * -1.0f
                )
            }

            layers[targetLayer].decalInstanceList.add(di)
        }
    }

    override fun drawWarpedDecal(decal: Decal, pos: List<Vf2d>, tint: Pixel) {
        drawWarpedDecal(decal, pos.toTypedArray(), tint)
    }

    override fun drawRotatedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        val di = DecalInstance(
            decal = decal,
            tint = tint
        )
        di.pos[0] = (Vf2d(0.0f, 0.0f) - center) * scale
        di.pos[1] = (Vf2d(0.0f, (decal.sprite.height).toFloat()) - center) * scale
        di.pos[2] = (Vf2d((decal.sprite.width.toFloat()), (decal.sprite.height.toFloat())) - center) * scale
        di.pos[3] = (Vf2d((decal.sprite.width.toFloat()), 0.0f) - center) * scale
        val c = kotlin.math.cos(angle)
        val s = kotlin.math.sin(angle)

        for (i in 0 until 4) {
            di.pos[i] = pos + Vf2d(di.pos[i].x * c - di.pos[i].y * s, di.pos[i].x * s + di.pos[i].y * c)
            di.pos[i] = di.pos[i] * vInvScreenSize * 2.0f - Vf2d(1.0f, 1.0f)
            di.pos[i].y *= -1.0f
        }

        layers[targetLayer].decalInstanceList.add(di)
    }

    override fun drawStringDecal(pos: Vf2d, text: String, col: Pixel, scale: Vf2d) {
        val spos = Vf2d(0.0f, 0.0f)

        text.toCharArray().forEach { c ->
            if (c == '\n') {
                spos.x = 0.0f
                spos.y += 8.0f * scale.y
            } else {
                val ox = (c.toShort() - 32).rem(16)
                val oy = (c.toShort() - 32) / 16
                drawPartialDecal(
                    pos = pos + spos,
                    decal = fontDecal,
                    source_pos = Vf2d((ox.toFloat()) * 8.0f, (oy.toFloat()) * 8.0f),
                    source_size = Vf2d(8.0f, 8.0f),
                    scale = scale,
                    tint = col
                )
                spos.x += 8.0f * scale.x
            }
        }
    }

    override fun drawPartialRotatedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        val di = DecalInstance(
            decal = decal,
            tint = tint
        )
        di.pos[0] = (Vf2d(0.0f, 0.0f) - center) * scale
        di.pos[1] = (Vf2d(0.0f, source_size.y) - center) * scale
        di.pos[2] = (Vf2d(source_size.x, source_size.y) - center) * scale
        di.pos[3] = (Vf2d(source_size.x, 0.0f) - center) * scale
        val c = kotlin.math.cos(angle)
        val s = kotlin.math.sin(angle)

        for (i in 0 until 4) {
            di.pos[i] = pos + Vf2d(di.pos[i].x * c - di.pos[i].y * s, di.pos[i].x * s + di.pos[i].y * c)
            di.pos[i] = di.pos[i] * vInvScreenSize * 2.0f - Vf2d(1.0f, 1.0f)
            di.pos[i].y *= -1.0f
        }

        val uvtl = source_pos * decal.uvScale
        val uvbr = uvtl + (source_size * decal.uvScale)
        di.uv[0] = Vf2d(uvtl.x, uvtl.y)
        di.uv[1] = Vf2d(uvtl.x, uvbr.y)
        di.uv[2] = Vf2d(uvbr.x, uvbr.y)
        di.uv[3] = Vf2d(uvbr.x, uvtl.y)

        layers[targetLayer].decalInstanceList.add(di)
    }

    override fun drawPartialWarpedDecal(
        decal: Decal,
        pos: Array<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel
    ) {
        val di = DecalInstance(
            decal = decal,
            tint = tint
        )
        var center = Vf2d(0, 0)
        var rd = ((pos[2].x - pos[0].x) * (pos[3].y - pos[1].y) - (pos[3].x - pos[1].x) * (pos[2].y - pos[0].y))
        if (rd != 0.0f) {
            val uvtl = source_pos * decal.uvScale
            val uvbr = uvtl + (source_size * decal.uvScale)
            di.uv[0] = Vf2d(uvtl.x, uvtl.y)
            di.uv[1] = Vf2d(uvtl.x, uvbr.y)
            di.uv[2] = Vf2d(uvbr.x, uvbr.y)
            di.uv[3] = Vf2d(uvbr.x, uvtl.y)

            rd = 1.0f / rd
            val rn =
                ((pos[3].x - pos[1].x) * (pos[0].y - pos[1].y) - (pos[3].y - pos[1].y) * (pos[0].x - pos[1].x)) * rd
            val sn =
                ((pos[2].x - pos[0].x) * (pos[0].y - pos[1].y) - (pos[2].y - pos[0].y) * (pos[0].x - pos[1].x)) * rd

            if (!(rn < 0.0f || rn > 1.0f || sn < 0.0f || sn > 1.0f)) center = pos[0] + (pos[2] - pos[0]) * rn

            val d = (0 until 4).map { (pos[it] - center).mag() }

            for (i in 0 until 4) {
                val q =
                    if (d[i] == 0.0f) 1.0f
                    else (d[i] + d[(i + 2) and 3]) / d[(i + 2) and 3]

                di.uv[i] = di.uv[i] * q
                di.w[i] *= q
                di.pos[i] = Vf2d(
                    (pos[i].x * vInvScreenSize.x) * 2.0f - 1.0f,
                    ((pos[i].y * vInvScreenSize.y) * 2.0f - 1.0f) * -1.0f
                )
            }

            layers[targetLayer].decalInstanceList.add(di)
        }
    }

    override fun drawPartialWarpedDecal(
        decal: Decal,
        pos: List<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel
    ) {
        drawPartialWarpedDecal(decal, pos.toTypedArray(), source_pos, source_size, tint)
    }

    /////////////////////
    override fun createDecal(sprite: Sprite): Decal {
        val id = this.renderer.createTexture(sprite.width, sprite.height)
        val uvScale = Vf2d(1.0f / (sprite.width).toFloat(), 1.0f / (sprite.height).toFloat())
        renderer.applyTexture(id)
        renderer.updateTexture(id, sprite)
        return Decal(id, sprite, uvScale)
    }

    override fun updateDecal(decal: Decal): Decal {
        val id = decal.id
        val sprite = decal.sprite
        val uvScale = Vf2d(1.0f / (sprite.width).toFloat(), 1.0f / (sprite.height).toFloat())
        renderer.applyTexture(id)
        renderer.updateTexture(id, sprite)
        return Decal(id, sprite, uvScale)
    }

    override fun deleteDecal(decal: Decal) {
        renderer.deleteTexture(decal.id)
    }

    // main loop
    private fun engineMainLoop() {
        println("pge::engineMainLoop called")

        try {
            if (platform.threadStartUp() == RetCode.FAIL) return

            prepareEngine()

            if (!onUserCreate()) return

            while (bAtomActive && !platform.shouldClose()) {
                coreUpdate()
            }
        } finally {
            onUserDestroy()
            this.spriteDecalInternalMap.values.forEach { deleteDecal(it) }
            platform.threadCleanUp()
            println("EngineThread() return")
        }
    }

    private fun coreUpdate() {
        // Handle Timing
        tp2 = getTimeNanos()
        val elapsedTime = tp2 - tp1
        tp1 = tp2
        val elapsedTimeFloat = elapsedTime.toFloat() / 1_000_000_000f
//        println("0: single frame update time: $elapsedTimeFloat")

        platform.handleSystemEvent()
        renderer.clearBuffer(Pixel.BLACK, true)

//        var t = getTimeNanos()
        // Handle Frame Update
        if (!onUserUpdate(elapsedTimeFloat)) {
            bAtomActive = false
        }
//        println("1: single frame userUpdate time: ${(getTimeNanos() - t)/1_000_000_000f}")

        // Display Frame
        renderer.updateViewport(vViewPos.data, vViewSize.data)
        renderer.clearBuffer(Pixel.BLACK, true)

        // Layer 0 must always exist
        layers[0].update = true
        layers[0].show = true
        renderer.prepareDrawing()

        layers.filter { it.show }
            .forEach { layer: LayerDesc ->
                if (layer.funcHook == null) {
                    renderer.applyTexture(layer.resId)

                    if (layer.update) {
                        renderer.updateTexture(layer.resId, layer.drawTarget!!)
                        renderer.drawLayerQuad(layer)
                        layer.update = false
                    }


//                    t = getTimeNanos()
                    // Display Decals in order for this layer
                    layer.decalInstanceList.forEach {
                        renderer.drawDecalQuad(it)
                    }
                    layer.decalInstanceList.clear()
//                    println("2: single frame decalInstanceList draw time: ${(getTimeNanos() - t)/1_000_000_000f}")
                } else {
                    val func = layer.funcHook
                    func()
                }
            }

        // Present Graphics to screen
        renderer.displayFrame()

        // Update Title Bar
        fFrameTimer += elapsedTimeFloat
        ++nFrameCount

        if (fFrameTimer >= 1.0f) {
            fps = nFrameCount
            fFrameTimer -= 1.0f
            val title = "$appName - FPS: $nFrameCount"
            platform.setWindowTitle(title)
            nFrameCount = 0
        }
    }

    private fun prepareEngine() {
        println("pge::prepareEngine called")
        if (platform.createGraphics(fullScreen, enableVsync, vViewPos, vViewSize) == RetCode.FAIL) return
        // Construct default font sheet
        olcConstructFontsheet()
        // Create Primary Layer "0"
        createLayer()
        layers[0].update = true
        layers[0].show = true
        setDrawTarget(null)
        tp1 = getTimeNanos()
        tp2 = getTimeNanos()
    }

    override fun createLayer(): Int {
        val ld = LayerDesc()
        ld.drawTarget = Sprite(screenSize.x, screenSize.y)
        ld.resId = renderer.createTexture(screenSize.x, screenSize.y)
        renderer.updateTexture(ld.resId, ld.drawTarget!!)
        layers.add(ld)
        return layers.size - 1
    }

    override fun deleteLayer(layerId: Int) {
        if (layerId in layers.indices) {
            val ld = layers.removeAt(layerId)
            renderer.deleteTexture(ld.resId)
        }
    }

    internal fun olcUpdateViewport() {
        println("olc_UpdateViewport() called [$nScreenWidth, $nScreenHeight] [$nPixelWidth, $nPixelHeight]")
        val ww = nScreenWidth * nPixelWidth
        val wh = nScreenHeight * nPixelHeight
        val wasp = ww.toFloat() / wh.toFloat()
        println("ww = $ww, wh = $wh, wasp = $wasp")

        nViewW = nWindowWidth
        nViewH = (nViewW.toFloat() / wasp).toInt()

        if (nViewH > nWindowHeight) {
            nViewH = nWindowHeight
            nViewW = (nViewH.toFloat() * wasp).toInt()
        }

        nViewX = (nWindowWidth - nViewW) / 2
        nViewY = (nWindowHeight - nViewH) / 2
        println("olc_UpdateViewport() return ($nViewX, $nViewY, $nViewW, $nViewH)")
    }

    internal fun olcRefreshKeyboardAndMouseState() {
        for (i in 0..255) {
            pKeyboardState[i].bPressed = false
            pKeyboardState[i].bReleased = false

            if (keyNewState[i] != pKeyOldState[i]) {
                if (keyNewState[i]) {
                    pKeyboardState[i].bPressed = !pKeyboardState[i].bHeld
                    pKeyboardState[i].bHeld = true
                } else {
                    pKeyboardState[i].bReleased = true
                    pKeyboardState[i].bHeld = false
                }
            }

            pKeyOldState[i] = keyNewState[i]
        }

        // Handle User Input - Mouse
        for (i in 0..4) {
            pMouseState[i].bPressed = false
            pMouseState[i].bReleased = false

            if (mouseNewState[i] != pMouseOldState[i]) {
                if (mouseNewState[i]) {
                    pMouseState[i].bPressed = !pMouseState[i].bHeld
                    pMouseState[i].bHeld = true
                } else {
                    pMouseState[i].bReleased = true
                    pMouseState[i].bHeld = false
                }
            }

            pMouseOldState[i] = mouseNewState[i]
        }

        // Cache mouse coordinates so they remain
        // consistent during frame
        nMousePosX = nMousePosXcache
        nMousePosY = nMousePosYcache

        nMouseWheelDelta = nMouseWheelDeltaCache
        nMouseWheelDeltaCache = 0
    }

    internal fun updateMouse(x: Double, y: Double) {
        // Mouse coords come in screen space
        // But leave in pixel space

        // we'll calculate against given resolution
        // (real resolution on macbook is weird)
        val ww: Int = (nScreenWidth * nPixelWidth)
        val wh: Int = (nScreenHeight * nPixelHeight)

        //if (bFullScreen)
//		{
        // Full Screen mode may have a weird viewport we must clamp to
//			x -= nViewX;
//			y -= nViewY;
//		}

        nMousePosXcache = (x / (ww - (nViewX * 2)) * nScreenWidth).roundToInt()
        nMousePosYcache = (y / (wh - (nViewY * 2)) * nScreenHeight).roundToInt()

        if (nMousePosXcache >= nScreenWidth)
            nMousePosXcache = nScreenWidth - 1
        if (nMousePosYcache >= nScreenHeight)
            nMousePosYcache = nScreenHeight - 1

        if (nMousePosXcache < 0)
            nMousePosXcache = 0
        if (nMousePosYcache < 0)
            nMousePosYcache = 0
    }

    internal fun updateMouseWheel(delta: Double) {
        nMouseWheelDeltaCache += delta.roundToInt()
    }

    private fun olcConstructFontsheet() {
        println("pge::olcConstructFontsheet called")
        var data = "?Q`0001oOch0o01o@F40o0<AGD4090LAGD<090@A7ch0?00O7Q`0600>00000000"
        data += "O000000nOT0063Qo4d8>?7a14Gno94AA4gno94AaOT0>o3`oO400o7QN00000400"
        data += "Of80001oOg<7O7moBGT7O7lABET024@aBEd714AiOdl717a_=TH013Q>00000000"
        data += "720D000V?V5oB3Q_HdUoE7a9@DdDE4A9@DmoE4A;Hg]oM4Aj8S4D84@`00000000"
        data += "OaPT1000Oa`^13P1@AI[?g`1@A=[OdAoHgljA4Ao?WlBA7l1710007l100000000"
        data += "ObM6000oOfMV?3QoBDD`O7a0BDDH@5A0BDD<@5A0BGeVO5ao@CQR?5Po00000000"
        data += "Oc``000?Ogij70PO2D]??0Ph2DUM@7i`2DTg@7lh2GUj?0TO0C1870T?00000000"
        data += "70<4001o?P<7?1QoHg43O;`h@GT0@:@LB@d0>:@hN@L0@?aoN@<0O7ao0000?000"
        data += "OcH0001SOglLA7mg24TnK7ln24US>0PL24U140PnOgl0>7QgOcH0K71S0000A000"
        data += "00H00000@Dm1S007@DUSg00?OdTnH7YhOfTL<7Yh@Cl0700?@Ah0300700000000"
        data += "<008001QL00ZA41a@6HnI<1i@FHLM81M@@0LG81?O`0nC?Y7?`0ZA7Y300080000"
        data += "O`082000Oh0827mo6>Hn?Wmo?6HnMb11MP08@C11H`08@FP0@@0004@000000000"
        data += "00P00001Oab00003OcKP0006@6=PMgl<@440MglH@000000`@000001P00000000"
        data += "Ob@8@@00Ob@8@Ga13R@8Mga172@8?PAo3R@827QoOb@820@0O`0007`0000007P0"
        data += "O`000P08Od400g`<3V=P0G`673IP0`@3>1`00P@6O`P00g`<O`000GP800000000"
        data += "?P9PL020O`<`N3R0@E4HC7b0@ET<ATB0@@l6C4B0O`H3N7b0?P01L3R000000020"

        fontSprite = Sprite(128, 48)
        var px = 0
        var py = 0

        for (b in 0 until 1024 step 4) {
            val sym1 = data[b + 0].toLong().toUInt() - 48u
            val sym2 = data[b + 1].toLong().toUInt() - 48u
            val sym3 = data[b + 2].toLong().toUInt() - 48u
            val sym4 = data[b + 3].toLong().toUInt() - 48u
            val r = (sym1 shl 18) or (sym2 shl 12) or (sym3 shl 6) or sym4

            for (i in 0 until 24) {
                val k = if ((r and (1 shl i).toUInt()) > 0U) 255 else 0

                fontSprite.setPixel(px, py, Pixel(k, k, k, k))

                if (++py == 48) {
                    px++
                    py = 0
                }
            }
        }

        fontDecal = createDecal(fontSprite)
    }

    private val layers: MutableList<LayerDesc> = mutableListOf()
    private var targetLayer: Int = 0
    private var tp1: Long = 0
    private var tp2: Long = 0
    private lateinit var pDefaultDrawTarget: Sprite
    private lateinit var pDrawTarget: Sprite
    private var nPixelMode = Pixel.Mode.NORMAL
    private var fBlendFactor: Float = 1.0f

    private var screenSize: Vi2d = Vi2d(256, 240)
    private inline var nScreenWidth: Int
        inline get() = screenSize.x
        inline set(v) {
            screenSize.x = v
        }
    private inline var nScreenHeight: Int
        inline get() = screenSize.y
        inline set(v) {
            screenSize.y = v
        }

    private var vInvScreenSize: Vf2d = Vf2d(1.0f / screenSize.x.toFloat(), 1.0f / screenSize.y.toFloat())

    private var pixelSize: Vi2d = Vi2d(4, 4)
    private inline var nPixelWidth: Int
        inline get() = pixelSize.x
        inline set(v) {
            pixelSize.x = v
        }
    private inline var nPixelHeight: Int
        inline get() = pixelSize.y
        inline set(v) {
            pixelSize.y = v
        }

    private var nMousePosX: Int = 0
    private var nMousePosY: Int = 0
    private var nMouseWheelDelta: Int = 0
    private var nMousePosXcache: Int = 0
    private var nMousePosYcache: Int = 0
    private var nMouseWheelDeltaCache: Int = 0

    internal var windowSize: Vi2d = Vi2d(0, 0)
    private inline var nWindowWidth: Int
        inline get() = windowSize.x
        inline set(v) {
            windowSize.x = v
        }
    private inline var nWindowHeight: Int
        inline get() = windowSize.y
        inline set(v) {
            windowSize.y = v
        }

    private var vViewPos: Vi2d = Vi2d(0, 0)
    private inline var nViewX: Int
        inline get() = vViewPos.x
        inline set(v) {
            vViewPos.x = v
        }
    private inline var nViewY: Int
        inline get() = vViewPos.y
        inline set(v) {
            vViewPos.y = v
        }

    internal var vViewSize: Vi2d = Vi2d(0, 0)
    private var nViewW: Int
        inline get() = vViewSize.x
        inline set(v) {
            vViewSize.x = v
        }
    private inline var nViewH: Int
        inline get() = vViewSize.y
        inline set(v) {
            vViewSize.y = v
        }

    private var fullScreen: Boolean = false
    private var enableVsync: Boolean = false
    private var fPixelX: Float = 1.0f
    private var fPixelY: Float = 1.0f
    private var fSubPixelOffsetX: Float = 0.0f
    private var fSubPixelOffsetY: Float = 0.0f

    internal val focusState = FocusState()

    class FocusState {
        var bHasInputFocus: Boolean = true
        var bHasMouseFocus: Boolean = false
    }

    private var fFrameTimer: Float = 1.0f
    private var nFrameCount: Int = 0
    private var fps: Int = 0
    private lateinit var fontSprite: Sprite
    private lateinit var fontDecal: Decal
    private var funcPixelMode: ((Int, Int, Pixel, Pixel) -> Pixel)? = null

    internal var keyNewState: Array<Boolean> = Array(256) { false }
    private var pKeyOldState: Array<Boolean> = Array(256) { false }
    private var pKeyboardState: Array<HWButton> = Array(256) { HWButton() }

    internal var mouseNewState: Array<Boolean> = Array(5) { false }
    private var pMouseOldState: Array<Boolean> = Array(5) { false }
    private var pMouseState: Array<HWButton> = Array(5) { HWButton() }

    private var bAtomActive: Boolean = true
}

private operator fun <T> Array<T>.component6(): T {
    return get(5)
}

private operator fun <T> Array<T>.component7(): T {
    return get(6)
}

private operator fun <T> Array<T>.component8(): T {
    return get(7)
}

private operator fun <T> Array<T>.component9(): T {
    return get(8)
}
