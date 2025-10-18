package olc.game_engine

import cglfw.*
import cnames.structs.GLFWwindow
import kotlinx.cinterop.*
import platform.OpenGL3.*
import kotlin.math.*
import kotlin.time.TimeSource

// Shaders for texture mapping. Work in conjunction with 'vertexBufferData' in EngineThread
// language=glsl
const val vertexShaderSource = """
#version 330 core
layout (location = 0) in vec4 vertex; // <vec2 position, vec2 texCoords>

out vec2 TexCoords;

void main()
{
    TexCoords = vertex.zw;
    gl_Position = vec4(vertex.xy, 0.0, 1.0);
}
"""

// language=glsl
const val fragmentShaderSource = """
#version 330 core
in vec2 TexCoords;
out vec4 color;

uniform sampler2D renderedTexture;

void main()
{    
    color = texture(renderedTexture, TexCoords);
}     
"""

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
        full_screen: Boolean = false
    ): rcode

    fun start(): rcode

    fun onUserCreate(): Boolean
    fun onUserUpdate(elapsedTime: Float): Boolean
    fun onUserDestroy() {
        // optional override hook
    }

    fun isFocused(): Boolean
    fun isMouseInWindow(): Boolean
    fun getKey(k: Key): HWButton
    fun getMouseKey(b: Int): HWButton
    fun getMouse(b: Int): HWButton = getMouseKey(b)
    fun getMouseX(): Int
    fun getMouseY(): Int
    fun getMouseWheel(): Int
    fun screenWidth(): Int
    fun screenHeight(): Int
    fun getDrawTargetWidth(): Int
    fun getDrawTargetHeight(): Int
    fun getDrawTarget(): Sprite
    fun setDrawTarget(target: Sprite)
    fun setDrawTarget(layer: Int)
    fun resetDrawTarget()
    fun setPixelMode(m: Pixel.Mode)
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
    fun fillRect(pos: Vi2d, size: Vi2d, p: Pixel = Pixel.WHITE) = fillRect(pos.x, pos.y, size.x, size.y, p)
    fun drawTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun fillTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun drawSprite(x: Int, y: Int, sprite: Sprite, scale: Int = 1)
    fun drawSprite(pos: Vi2d, sprite: Sprite, scale: Int = 1) = drawSprite(pos.x, pos.y, sprite, scale)
    fun drawPartialSprite(x: Int, y: Int, sprite: Sprite, ox: Int, oy: Int, w: Int, h: Int, scale: Int = 1)
    fun drawString(x: Int, y: Int, text: String, col: Pixel = Pixel.WHITE, scale: Int = 1)
    fun drawString(pos: Vi2d, text: String, col: Pixel = Pixel.WHITE, scale: Int = 1) =
        drawString(pos.x, pos.y, text, col, scale)

    fun drawDecal(pos: Vf2d, decal: Decal, scale: Vf2d = Vf2d(1.0f, 1.0f), tint: Pixel = Pixel.WHITE)

    fun drawPartialDecal(
        pos: Vf2d,
        decal: Decal,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d = Vf2d(1.0f, 1.0f),
        tint: Pixel = Pixel.WHITE
    )

    fun drawWarpedDecal(decal: Decal, pos: Array<Vf2d>, tint: Pixel = Pixel.WHITE)
    fun drawWarpedDecal(decal: Decal, pos: List<Vf2d>, tint: Pixel = Pixel.WHITE) =
        drawWarpedDecal(decal, pos.toTypedArray(), tint)

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
    ) = drawPartialWarpedDecal(decal, pos.toTypedArray(), source_pos, source_size, tint)

    fun createDecal(sprite: Sprite): Decal
    fun updateDecal(decal: Decal): Decal
    fun deleteDecal(decal: Decal)

    fun createLayer(): Int
    fun deleteLayer(layerId: Int)

    fun clear(p: Pixel = Pixel.BLACK)
    fun getFPS(): Int
}

@ExperimentalUnsignedTypes
abstract class PixelGameEngineImpl : PixelGameEngine {
    override val appName: String = "Undefined"

    override fun construct(screen_w: Int, screen_h: Int, pixel_w: Int, pixel_h: Int, full_screen: Boolean): rcode {
        nScreenWidth = screen_w
        nScreenHeight = screen_h
        nPixelWidth = pixel_w
        nPixelHeight = pixel_h
        bFullScreen = full_screen
        fPixelX = 2.0f / nScreenWidth.toFloat()
        fPixelY = 2.0f / nScreenHeight.toFloat()

        if (nPixelWidth <= 0 || nPixelHeight <= 0 || nScreenWidth <= 0 || nScreenHeight <= 0)
            return rcode.FAIL

        decals.clear()
        nextDecalId = 1u
        layers.clear()
        layerVisible.clear()
        currentLayerIndex = -1
        fontDecal = null
        olcConstructFontsheet()
        pDefaultDrawTarget = Sprite(nScreenWidth, nScreenHeight)
        compositedFrame = UIntArray(pDefaultDrawTarget.data.size)
        setDrawTarget(pDefaultDrawTarget)
        return rcode.OK
    }

    override fun start(): rcode {
        println("Start called")

        if (glfwInit() != GLFW_TRUE) {
            println("GLFW initialization failed")
            return rcode.FAIL
        }

        if (!olcWindowCreate()) {
            glfwTerminate()
            return rcode.FAIL
        }

        olcOpenGlCreate()

        if (onUserCreate()) {
            bAtomActive = 1
            engineMainLoop()
            onUserDestroy()
        }

        window?.let { glfwDestroyWindow(it) }
        glfwTerminate()
        stableRefList.forEach { it.dispose() }
        return rcode.OK
    }

    // Utility methods and flow control
    override fun setDrawTarget(target: Sprite) {
        currentLayerIndex = -1
        pDrawTarget = target
    }

    override fun setDrawTarget(layer: Int) {
        if (layer == 0) {
            currentLayerIndex = -1
            pDrawTarget = pDefaultDrawTarget
            return
        }
        val idx = layer - 1
        if (idx in layers.indices) {
            currentLayerIndex = idx
            layerVisible[idx] = true
            pDrawTarget = layers[idx]
        }
    }

    override fun resetDrawTarget() {
        currentLayerIndex = -1
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
        if (scale > 1) {
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
        }
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
                    } catch (err: IndexOutOfBoundsException) {
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
        drawPartialDecal(
            pos = pos,
            decal = decal,
            source_pos = Vf2d(0f, 0f),
            source_size = Vf2d(decal.sprite.width.toFloat(), decal.sprite.height.toFloat()),
            scale = scale,
            tint = tint
        )
    }

    override fun drawPartialDecal(
        pos: Vf2d,
        decal: Decal,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        val sprite = decal.sprite
        if (sprite.width == 0 || sprite.height == 0) return
        val srcX = source_pos.x.toInt()
        val srcY = source_pos.y.toInt()
        val srcW = source_size.x.toInt()
        val srcH = source_size.y.toInt()
        if (srcW <= 0 || srcH <= 0) return

        val scaleX = if (scale.x == 0f) 1f else scale.x
        val scaleY = if (scale.y == 0f) 1f else scale.y
        val absScaleX = abs(scaleX)
        val absScaleY = abs(scaleY)
        val destWidth = max(1, ceil(srcW * absScaleX.toDouble()).toInt())
        val destHeight = max(1, ceil(srcH * absScaleY.toDouble()).toInt())
        val invScaleX = 1f / absScaleX
        val invScaleY = 1f / absScaleY
        val originX = pos.x
        val originY = pos.y
        val dirX = sign(scaleX).takeIf { it != 0f } ?: 1f
        val dirY = sign(scaleY).takeIf { it != 0f } ?: 1f

        for (dy in 0 until destHeight) {
            val srcRow = srcY + floor((dy * invScaleY).toDouble()).toInt()
            if (srcRow !in srcY until (srcY + srcH)) continue
            val destY = floor((originY + dy * dirY).toDouble()).toInt()
            for (dx in 0 until destWidth) {
                val srcCol = srcX + floor((dx * invScaleX).toDouble()).toInt()
                if (srcCol !in srcX until (srcX + srcW)) continue
                val destX = floor((originX + dx * dirX).toDouble()).toInt()
                val pixel = sprite.getPixel(srcCol, srcRow) ?: continue
                if (pixel.a.toInt() == 0) continue
                draw(destX, destY, applyTint(pixel, tint))
            }
        }
    }

    override fun drawRotatedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        drawTransformedDecal(
            pos = pos,
            decal = decal,
            angle = angle,
            center = center,
            source_pos = Vf2d(0f, 0f),
            source_size = Vf2d(decal.sprite.width.toFloat(), decal.sprite.height.toFloat()),
            scale = scale,
            tint = tint
        )
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
        drawTransformedDecal(pos, decal, angle, center, source_pos, source_size, scale, tint)
    }

    override fun drawWarpedDecal(decal: Decal, pos: Array<Vf2d>, tint: Pixel) {
        drawWarpedDecalInternal(
            decal = decal,
            dest = pos,
            source_pos = Vf2d(0f, 0f),
            source_size = Vf2d(decal.sprite.width.toFloat(), decal.sprite.height.toFloat()),
            tint = tint
        )
    }

    override fun drawPartialWarpedDecal(
        decal: Decal,
        pos: Array<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel
    ) {
        drawWarpedDecalInternal(decal, pos, source_pos, source_size, tint)
    }

    override fun drawStringDecal(pos: Vf2d, text: String, col: Pixel, scale: Vf2d) {
        val fontDecalLocal = fontDecal
        if (fontDecalLocal == null) {
            drawString(
                pos.x.roundToInt(),
                pos.y.roundToInt(),
                text,
                col,
                max(scale.x, scale.y).roundToInt().coerceAtLeast(1)
            )
            return
        }
        var cursor = Vf2d(0f, 0f)
        text.forEach { c ->
            if (c == '\n') {
                cursor = Vf2d(0f, cursor.y + 8f * scale.y)
            } else if (c.code >= 32 && c.code <= 127) {
                val ox = (c.code - 32) % 16
                val oy = (c.code - 32) / 16
                drawPartialDecal(
                    pos = pos + cursor,
                    decal = fontDecalLocal,
                    source_pos = Vf2d(ox * 8f, oy * 8f),
                    source_size = Vf2d(8f, 8f),
                    scale = scale,
                    tint = col
                )
                cursor = Vf2d(cursor.x + 8f * scale.x, cursor.y)
            }
        }
    }

    override fun createDecal(sprite: Sprite): Decal {
        val uvScale = Vf2d(
            if (sprite.width != 0) 1.0f / sprite.width.toFloat() else 0f,
            if (sprite.height != 0) 1.0f / sprite.height.toFloat() else 0f
        )
        val decal = Decal(nextDecalId++, sprite, uvScale)
        decals[decal.id] = decal
        return decal
    }

    override fun updateDecal(decal: Decal): Decal {
        decal.dirty = true
        decals[decal.id] = decal
        return decal
    }

    override fun deleteDecal(decal: Decal) {
        decals.remove(decal.id)
    }

    override fun createLayer(): Int {
        val sprite = Sprite(nScreenWidth, nScreenHeight)
        layers.add(sprite)
        layerVisible.add(true)
        return layers.size // zero is reserved for default layer
    }

    override fun deleteLayer(layerId: Int) {
        if (layerId <= 0) return
        val idx = layerId - 1
        if (idx !in layers.indices) return
        layers.removeAt(idx)
        layerVisible.removeAt(idx)
        when {
            currentLayerIndex == idx -> resetDrawTarget()
            currentLayerIndex > idx -> currentLayerIndex--
        }
    }

    private fun drawTransformedDecal(
        pos: Vf2d,
        decal: Decal,
        angle: Float,
        center: Vf2d,
        source_pos: Vf2d,
        source_size: Vf2d,
        scale: Vf2d,
        tint: Pixel
    ) {
        val sprite = decal.sprite
        if (sprite.width == 0 || sprite.height == 0) return
        if (source_size.x <= 0f || source_size.y <= 0f) return

        val scaleX = if (scale.x == 0f) 1f else scale.x
        val scaleY = if (scale.y == 0f) 1f else scale.y
        val absScaleX = abs(scaleX)
        val absScaleY = abs(scaleY)
        val invScaleX = 1f / scaleX
        val invScaleY = 1f / scaleY
        val cosA = cos(angle)
        val sinA = sin(angle)
        val destWidth = max(1, ceil(source_size.x * absScaleX.toDouble()).toInt())
        val destHeight = max(1, ceil(source_size.y * absScaleY.toDouble()).toInt())
        val scaledCenter = Vf2d(center.x * scaleX, center.y * scaleY)
        val destOriginX = pos.x - scaledCenter.x
        val destOriginY = pos.y - scaledCenter.y

        for (dy in 0 until destHeight) {
            val screenY = destOriginY + dy
            val destY = floor(screenY.toDouble()).toInt()
            for (dx in 0 until destWidth) {
                val screenX = destOriginX + dx
                val destX = floor(screenX.toDouble()).toInt()
                val relX = screenX - pos.x
                val relY = screenY - pos.y
                val localX = (relX * cosA + relY * sinA) * invScaleX + center.x
                val localY = (-relX * sinA + relY * cosA) * invScaleY + center.y
                if (localX < 0f || localX >= source_size.x || localY < 0f || localY >= source_size.y) continue
                val srcX = floor((source_pos.x + localX).toDouble()).toInt()
                val srcY = floor((source_pos.y + localY).toDouble()).toInt()
                if (srcX !in 0 until sprite.width || srcY !in 0 until sprite.height) continue
                val pixel = sprite.getPixel(srcX, srcY) ?: continue
                if (pixel.a.toInt() == 0) continue
                draw(destX, destY, applyTint(pixel, tint))
            }
        }
    }

    private fun drawWarpedDecalInternal(
        decal: Decal,
        dest: Array<Vf2d>,
        source_pos: Vf2d,
        source_size: Vf2d,
        tint: Pixel
    ) {
        val sprite = decal.sprite
        if (dest.size < 4 || sprite.width == 0 || sprite.height == 0) return
        if (source_size.x <= 0f || source_size.y <= 0f) return

        val spriteWidth = sprite.width.toFloat()
        val spriteHeight = sprite.height.toFloat()
        val uv = arrayOf(
            Vf2d(source_pos.x / spriteWidth, source_pos.y / spriteHeight),
            Vf2d(source_pos.x / spriteWidth, (source_pos.y + source_size.y) / spriteHeight),
            Vf2d((source_pos.x + source_size.x) / spriteWidth, (source_pos.y + source_size.y) / spriteHeight),
            Vf2d((source_pos.x + source_size.x) / spriteWidth, source_pos.y / spriteHeight)
        )

        val minX = floor(dest.minOf { it.x }.toDouble()).toInt()
        val maxX = ceil(dest.maxOf { it.x }.toDouble()).toInt()
        val minY = floor(dest.minOf { it.y }.toDouble()).toInt()
        val maxY = ceil(dest.maxOf { it.y }.toDouble()).toInt()

        drawWarpedTriangle(sprite, dest[0], dest[1], dest[2], uv[0], uv[1], uv[2], tint, minX, maxX, minY, maxY)
        drawWarpedTriangle(sprite, dest[0], dest[2], dest[3], uv[0], uv[2], uv[3], tint, minX, maxX, minY, maxY)
    }

    private fun drawWarpedTriangle(
        sprite: Sprite,
        p0: Vf2d,
        p1: Vf2d,
        p2: Vf2d,
        uv0: Vf2d,
        uv1: Vf2d,
        uv2: Vf2d,
        tint: Pixel,
        minX: Int,
        maxX: Int,
        minY: Int,
        maxY: Int
    ) {
        val area = (p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y)
        if (abs(area) < 1e-6f) return

        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val bary = barycentric(p0, p1, p2, x + 0.5f, y + 0.5f) ?: continue
                val u = uv0.x * bary.first + uv1.x * bary.second + uv2.x * bary.third
                val v = uv0.y * bary.first + uv1.y * bary.second + uv2.y * bary.third

                val sampleX = floor((u * sprite.width).toDouble()).toInt()
                val sampleY = floor((v * sprite.height).toDouble()).toInt()
                if (sampleX !in 0 until sprite.width || sampleY !in 0 until sprite.height) continue

                val pixel = sprite.getPixel(sampleX, sampleY) ?: continue
                if (pixel.a.toInt() == 0) continue
                draw(x, y, applyTint(pixel, tint))
            }
        }
    }

    private fun barycentric(p0: Vf2d, p1: Vf2d, p2: Vf2d, px: Float, py: Float): Triple<Float, Float, Float>? {
        val denom = (p1.y - p2.y) * (p0.x - p2.x) + (p2.x - p1.x) * (p0.y - p2.y)
        if (abs(denom) < 1e-6f) return null
        val w1 = ((p1.y - p2.y) * (px - p2.x) + (p2.x - p1.x) * (py - p2.y)) / denom
        val w2 = ((p2.y - p0.y) * (px - p2.x) + (p0.x - p2.x) * (py - p2.y)) / denom
        val w3 = 1f - w1 - w2
        if (w1 < 0f || w2 < 0f || w3 < 0f) return null
        return Triple(w1, w2, w3)
    }

    private fun applyTint(source: Pixel, tint: Pixel): Pixel {
        if (tint == Pixel.WHITE) return source
        val r = (source.rf * tint.rf / 255f).roundToInt().coerceIn(0, 255)
        val g = (source.gf * tint.gf / 255f).roundToInt().coerceIn(0, 255)
        val b = (source.bf * tint.bf / 255f).roundToInt().coerceIn(0, 255)
        val a = (source.af * tint.af / 255f).roundToInt().coerceIn(0, 255)
        return Pixel(r, g, b, a)
    }

    private fun buildFrameBuffer(): UIntArray {
        if (compositedFrame.size != pDefaultDrawTarget.data.size) {
            compositedFrame = UIntArray(pDefaultDrawTarget.data.size)
        }
        pDefaultDrawTarget.data.copyInto(compositedFrame)
        for (index in layers.indices) {
            if (index >= layerVisible.size) continue
            if (!layerVisible[index]) continue
            blendLayerIntoFrame(layers[index], compositedFrame)
        }
        return compositedFrame
    }

    private fun blendLayerIntoFrame(layerSprite: Sprite, target: UIntArray) {
        val src = layerSprite.data
        for (idx in src.indices) {
            val pixel = Pixel(src[idx])
            if (pixel.a.toInt() == 0) continue
            target[idx] = pixel.n
        }
    }


    // main loop
    private fun engineMainLoop() {
        println("EngineThread() called")
        println("programId = $programId")

        val windowPtr = window ?: return
        val texID = glGetUniformLocation(programId, "renderedTexture")
        println("texID = $texID")

        val vertexArrayID = memScoped {
            val output = alloc<UIntVar>()
            glGenVertexArrays(1, output.ptr)
            output.value
        }

        val vertexBufferData = floatArrayOf(
            // Pos      // Tex
            -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 1.0f,

            -1.0f, 1.0f, 0.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 1.0f
        )

        val vertexBuffer = memScoped {
            val output = alloc<UIntVar>()
            glGenBuffers(1, output.ptr)
            output.value
        }
        glBindBuffer(GL_ARRAY_BUFFER.toUInt(), vertexBuffer)

        vertexBufferData.usePinned {
            glBufferData(
                GL_ARRAY_BUFFER.toUInt(),
                vertexBufferData.size.toLong() * 4,
                it.addressOf(0),
                GL_STATIC_DRAW.toUInt()
            )
        }

        glBindVertexArray(vertexArrayID)
        glEnableVertexAttribArray(0U)
        glBindBuffer(GL_ARRAY_BUFFER.toUInt(), vertexBuffer)
        glVertexAttribPointer(0U, 4, GL_FLOAT.toUInt(), false.toByte().toUByte(), 0, 0L.toCPointer<CPointed>())
        glBindBuffer(GL_ARRAY_BUFFER.toUInt(), 0u)
        glBindVertexArray(0u)

        var lastTime = TimeSource.Monotonic.markNow()
        var elapsedTime: Float

        do {
            glViewport(nViewX, nViewY, nViewW, nViewH)
            glClear((GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT).toUInt())
            glUseProgram(programId)

            elapsedTime = lastTime.elapsedNow().inWholeNanoseconds / 1_000_000_000.0F
            lastTime = TimeSource.Monotonic.markNow()

            olcRefreshKeyboardAndMouseState()

            Sprite.nOverdrawCount = 0

            // Handle Frame Update
            if (!onUserUpdate(elapsedTime))
                bAtomActive = 0

            // Copy pixel array into texture
            glActiveTexture(GL_TEXTURE0.toUInt())
            glBindTexture(GL_TEXTURE_2D.toUInt(), glBuffer)

            val frame = buildFrameBuffer()
            frame.usePinned {
                glTexSubImage2D(
                    GL_TEXTURE_2D.toUInt(),
                    0,
                    0,
                    0,
                    nScreenWidth,
                    nScreenHeight,
                    GL_RGBA.toUInt(),
                    GL_UNSIGNED_BYTE.toUInt(),
                    it.addressOf(0)
                )
            }

            glUniform1i(texID, 0)
            glBindVertexArray(vertexArrayID)
            glDrawArrays(GL_TRIANGLES.toUInt(), 0, 6)
            glBindVertexArray(0u)

            glfwPollEvents()
            glfwSwapBuffers(windowPtr)

            // Update Title Bar
            fFrameTimer += elapsedTime
            nFrameCount++

            if (fFrameTimer >= 1.0f) {
                fFrameTimer -= 1.0f
                val sTitle = "OneLoneCoder.com - Pixel Game Engine - $appName - FPS: $nFrameCount"
                fps = nFrameCount
                glfwSetWindowTitle(windowPtr, sTitle)
                nFrameCount = 0
            }
        } while (glfwWindowShouldClose(windowPtr) == GLFW_FALSE && bAtomActive == 1)

        println("EngineThread() return")
    }

    // internal handlers and functions
    private fun olcWindowCreate(): Boolean {
        println("olc_WindowCreate() called")

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_SAMPLES, 4)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)

        val createdWindow = glfwCreateWindow(
            nScreenWidth * nPixelWidth,
            nScreenHeight * nPixelHeight,
            appName,
            null,
            null
        ) ?: run {
            println("Failed to create GLFW window")
            return false
        }

        window = createdWindow

        memScoped {
            val w = alloc<IntVar>()
            val h = alloc<IntVar>()
            glfwGetFramebufferSize(createdWindow, w.ptr, h.ptr)
            nWindowWidth = w.value
            nWindowHeight = h.value
        }

        nViewW = nWindowWidth
        nViewH = nWindowHeight

        glfwMakeContextCurrent(createdWindow)

        glfwSetInputMode(createdWindow, GLFW_STICKY_KEYS, GL_TRUE)
        glfwSetInputMode(createdWindow, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)

        val contextRef = StableRef.create(
            WindowCallbackContext(
                keyNewState = pKeyNewState,
                mouseNewState = pMouseNewState,
                focusState = focusState,
                updateMouse = this::olcUpdateMouse,
                updateMouseWheel = this::olcUpdateMouseWheel,
                keyMap = keyboardKeyMap,
                mouseMap = mouseButtonMap
            )
        )
        stableRefList.add(contextRef)
        glfwSetWindowUserPointer(createdWindow, contextRef.asCPointer())

        glfwSetKeyCallback(createdWindow, keyCallback)
        glfwSetMouseButtonCallback(createdWindow, mouseButtonCallback)
        glfwSetCursorPosCallback(createdWindow, cursorPosCallback)
        glfwSetScrollCallback(createdWindow, scrollCallback)
        glfwSetCursorEnterCallback(createdWindow, cursorEnterCallback)
        glfwSetWindowFocusCallback(createdWindow, focusCallback)

        println(glGetString(GL_VERSION.toUInt())?.reinterpret<ByteVar>()?.toKString())
        olcUpdateViewport()

        println("olc_WindowCreate() return")
        return true
    }

    private fun olcUpdateViewport() {
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

    private fun setShaderSource(shader: UInt, source: String) = memScoped {
        val cString = source.cstr
        val pointerArray = allocArray<CPointerVar<ByteVar>>(1)
        pointerArray[0] = cString.ptr
        val lengthArray = allocArray<IntVar>(1)
        lengthArray[0] = source.length
        glShaderSource(shader, 1, pointerArray, lengthArray)
    }

    private fun readShaderInfoLog(shader: UInt): String = memScoped {
        val length = alloc<IntVar>()
        glGetShaderiv(shader, GL_INFO_LOG_LENGTH.toUInt(), length.ptr)
        val size = length.value
        if (size <= 1) return ""
        val buffer = ByteArray(size)
        buffer.usePinned {
            glGetShaderInfoLog(shader, size, null, it.addressOf(0).reinterpret())
        }
        buffer.decodeToString().trimEnd('\u0000')
    }

    private fun readProgramInfoLog(program: UInt): String = memScoped {
        val length = alloc<IntVar>()
        glGetProgramiv(program, GL_INFO_LOG_LENGTH.toUInt(), length.ptr)
        val size = length.value
        if (size <= 1) return ""
        val buffer = ByteArray(size)
        buffer.usePinned {
            glGetProgramInfoLog(program, size, null, it.addressOf(0).reinterpret())
        }
        buffer.decodeToString().trimEnd('\u0000')
    }

    private fun olcOpenGlCreate(): Boolean {
        println("olc_OpenGLCreate() called")

        // Create Screen Texture - disable filtering
        glEnable(GL_TEXTURE_2D.toUInt())

        glBuffer = memScoped {
            val output = alloc<UIntVar>()
            glGenTextures(1, output.ptr)
            output.value
        }

        println("glBuffer = $glBuffer")

        glBindTexture(GL_TEXTURE_2D.toUInt(), glBuffer)

        // this call will create our "canvas" to draw into
        glTexImage2D(
            GL_TEXTURE_2D.toUInt(), 0, GL_RGBA.toInt(),
            nScreenWidth,
            nScreenHeight,
            0, GL_RGBA.toUInt(), GL_UNSIGNED_BYTE.toUInt(),
            this.pDefaultDrawTarget.data.toCValues()
        )

        glTexParameteri(GL_TEXTURE_2D.toUInt(), GL_TEXTURE_MAG_FILTER.toUInt(), GL_NEAREST.toInt())
        glTexParameteri(GL_TEXTURE_2D.toUInt(), GL_TEXTURE_MIN_FILTER.toUInt(), GL_NEAREST.toInt())

        // more configuration
        programId = olcLoadShaders()

        println("olc_OpenGLCreate() return")
        return true
    }

    private fun olcRefreshKeyboardAndMouseState() {
        for (i in 0..255) {
            pKeyboardState[i].bPressed = false
            pKeyboardState[i].bReleased = false

            if (pKeyNewState[i] != pKeyOldState[i]) {
                if (pKeyNewState[i]) {
                    pKeyboardState[i].bPressed = !pKeyboardState[i].bHeld
                    pKeyboardState[i].bHeld = true
                } else {
                    pKeyboardState[i].bReleased = true
                    pKeyboardState[i].bHeld = false
                }
            }

            pKeyOldState[i] = pKeyNewState[i]
        }

        // Handle User Input - Mouse
        for (i in 0..4) {
            pMouseState[i].bPressed = false
            pMouseState[i].bReleased = false

            if (pMouseNewState[i] != pMouseOldState[i]) {
                if (pMouseNewState[i]) {
                    pMouseState[i].bPressed = !pMouseState[i].bHeld
                    pMouseState[i].bHeld = true
                } else {
                    pMouseState[i].bReleased = true
                    pMouseState[i].bHeld = false
                }
            }

            pMouseOldState[i] = pMouseNewState[i]
        }

        // Cache mouse coordinates so they remain
        // consistent during frame
        nMousePosX = nMousePosXcache
        nMousePosY = nMousePosYcache

        nMouseWheelDelta = nMouseWheelDeltaCache
        nMouseWheelDeltaCache = 0
    }

    private fun olcUpdateMouse(x: Double, y: Double) {
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

    private fun olcUpdateMouseWheel(delta: Double) {
        nMouseWheelDeltaCache += delta.roundToInt()
    }

    private fun olcConstructFontsheet() {
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

    private fun olcLoadShaders(): UInt {
        return memScoped {
            val vertexShaderId = glCreateShader(GL_VERTEX_SHADER.toUInt())
            val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER.toUInt())

            setShaderSource(vertexShaderId, vertexShaderSource)
            glCompileShader(vertexShaderId)

            readShaderInfoLog(vertexShaderId).also {
                if (it.isNotBlank()) println(it)
            }

            setShaderSource(fragmentShaderId, fragmentShaderSource)
            glCompileShader(fragmentShaderId)

            readShaderInfoLog(fragmentShaderId).also {
                if (it.isNotBlank()) println(it)
            }

            val programId = glCreateProgram()
            glAttachShader(programId, vertexShaderId)
            glAttachShader(programId, fragmentShaderId)
            glLinkProgram(programId)

            readProgramInfoLog(programId).also {
                if (it.isNotBlank()) println(it)
            }

            glDetachShader(programId, vertexShaderId)
            glDetachShader(programId, fragmentShaderId)

            glDeleteShader(vertexShaderId)
            glDeleteShader(fragmentShaderId)

            programId
        }
    }

    private var window: CPointer<GLFWwindow>? = null
    private var programId: UInt = 0u
    private var glBuffer = 0u
    private lateinit var pDefaultDrawTarget: Sprite
    private lateinit var pDrawTarget: Sprite
    private val decals = mutableMapOf<UInt, Decal>()
    private var nextDecalId: UInt = 1u
    private val layers: MutableList<Sprite> = mutableListOf()
    private val layerVisible: MutableList<Boolean> = mutableListOf()
    private var currentLayerIndex: Int = -1
    private var compositedFrame: UIntArray = UIntArray(0)
    private var fontDecal: Decal? = null
    private var nPixelMode = Pixel.Mode.NORMAL
    private var fBlendFactor: Float = 1.0f
    private var nScreenWidth: Int = 256
    private var nScreenHeight: Int = 240
    private var nPixelWidth: Int = 4
    private var nPixelHeight: Int = 4
    private var nMousePosX: Int = 0
    private var nMousePosY: Int = 0
    private var nMouseWheelDelta: Int = 0
    private var nMousePosXcache: Int = 0
    private var nMousePosYcache: Int = 0
    private var nMouseWheelDeltaCache: Int = 0
    private var nWindowWidth: Int = 0
    private var nWindowHeight: Int = 0
    private var nViewX: Int = 0
    private var nViewY: Int = 0
    private var nViewW: Int = 0
    private var nViewH: Int = 0
    private var bFullScreen: Boolean = false
    private var fPixelX: Float = 1.0f
    private var fPixelY: Float = 1.0f
    private var fSubPixelOffsetX: Float = 0.0f
    private var fSubPixelOffsetY: Float = 0.0f

    private val focusState = FocusState()

    class FocusState {
        var bHasInputFocus: Boolean = true
        var bHasMouseFocus: Boolean = false
    }

    private var fFrameTimer: Float = 1.0f
    private var nFrameCount: Int = 0
    private var fps: Int = 0
    private lateinit var fontSprite: Sprite
    private var funcPixelMode: ((Int, Int, Pixel, Pixel) -> Pixel)? = null

    private var pKeyNewState: Array<Boolean> = Array(256) { false }
    private var pKeyOldState: Array<Boolean> = Array(256) { false }
    private var pKeyboardState: Array<HWButton> = Array(256) { HWButton() }

    private var pMouseNewState: Array<Boolean> = Array(5) { false }
    private var pMouseOldState: Array<Boolean> = Array(5) { false }
    private var pMouseState: Array<HWButton> = Array(5) { HWButton() }

    private val stableRefList = mutableListOf<StableRef<*>>()
    private val keyboardKeyMap =
        mapOf(
            GLFW_KEY_UNKNOWN to Key.NONE.ordinal,
            GLFW_KEY_A to Key.A.ordinal,
            GLFW_KEY_B to Key.B.ordinal,
            GLFW_KEY_C to Key.C.ordinal,
            GLFW_KEY_D to Key.D.ordinal,
            GLFW_KEY_E to Key.E.ordinal,
            GLFW_KEY_F to Key.F.ordinal,
            GLFW_KEY_G to Key.G.ordinal,
            GLFW_KEY_H to Key.H.ordinal,
            GLFW_KEY_I to Key.I.ordinal,
            GLFW_KEY_J to Key.J.ordinal,
            GLFW_KEY_K to Key.K.ordinal,
            GLFW_KEY_L to Key.L.ordinal,
            GLFW_KEY_M to Key.M.ordinal,
            GLFW_KEY_N to Key.N.ordinal,
            GLFW_KEY_O to Key.O.ordinal,
            GLFW_KEY_P to Key.P.ordinal,
            GLFW_KEY_Q to Key.Q.ordinal,
            GLFW_KEY_R to Key.R.ordinal,
            GLFW_KEY_S to Key.S.ordinal,
            GLFW_KEY_T to Key.T.ordinal,
            GLFW_KEY_U to Key.U.ordinal,
            GLFW_KEY_V to Key.V.ordinal,
            GLFW_KEY_W to Key.W.ordinal,
            GLFW_KEY_X to Key.X.ordinal,
            GLFW_KEY_Y to Key.Y.ordinal,
            GLFW_KEY_Z to Key.Z.ordinal,

            GLFW_KEY_F1 to Key.F1.ordinal,
            GLFW_KEY_F2 to Key.F2.ordinal,
            GLFW_KEY_F3 to Key.F3.ordinal,
            GLFW_KEY_F4 to Key.F4.ordinal,
            GLFW_KEY_F5 to Key.F5.ordinal,
            GLFW_KEY_F6 to Key.F6.ordinal,
            GLFW_KEY_F7 to Key.F7.ordinal,
            GLFW_KEY_F8 to Key.F8.ordinal,
            GLFW_KEY_F9 to Key.F9.ordinal,
            GLFW_KEY_F10 to Key.F10.ordinal,
            GLFW_KEY_F11 to Key.F11.ordinal,
            GLFW_KEY_F12 to Key.F12.ordinal,

            GLFW_KEY_DOWN to Key.DOWN.ordinal,
            GLFW_KEY_LEFT to Key.LEFT.ordinal,
            GLFW_KEY_RIGHT to Key.RIGHT.ordinal,
            GLFW_KEY_UP to Key.UP.ordinal,

            GLFW_KEY_ENTER to Key.ENTER.ordinal,
            GLFW_KEY_BACKSPACE to Key.BACK.ordinal,
            GLFW_KEY_ESCAPE to Key.ESCAPE.ordinal,
            GLFW_KEY_SPACE to Key.SPACE.ordinal,
            GLFW_KEY_TAB to Key.TAB.ordinal,
            GLFW_KEY_SCROLL_LOCK to Key.SCROLL.ordinal,

            GLFW_KEY_INSERT to Key.INS.ordinal,
            GLFW_KEY_DELETE to Key.DEL.ordinal,
            GLFW_KEY_HOME to Key.HOME.ordinal,
            GLFW_KEY_END to Key.END.ordinal,
            GLFW_KEY_PAGE_UP to Key.PGUP.ordinal,
            GLFW_KEY_PAGE_DOWN to Key.PGDN.ordinal,
            GLFW_KEY_PAUSE to Key.PAUSE.ordinal,

            GLFW_KEY_LEFT_SHIFT to Key.SHIFT.ordinal,
            GLFW_KEY_RIGHT_SHIFT to Key.SHIFT.ordinal,
            GLFW_KEY_LEFT_CONTROL to Key.CTRL.ordinal,
            GLFW_KEY_RIGHT_CONTROL to Key.CTRL.ordinal,

            GLFW_KEY_0 to Key.K0.ordinal,
            GLFW_KEY_1 to Key.K1.ordinal,
            GLFW_KEY_2 to Key.K2.ordinal,
            GLFW_KEY_3 to Key.K3.ordinal,
            GLFW_KEY_4 to Key.K4.ordinal,
            GLFW_KEY_5 to Key.K5.ordinal,
            GLFW_KEY_6 to Key.K6.ordinal,
            GLFW_KEY_7 to Key.K7.ordinal,
            GLFW_KEY_8 to Key.K8.ordinal,
            GLFW_KEY_9 to Key.K9.ordinal,

            GLFW_KEY_KP_0 to Key.NP0.ordinal,
            GLFW_KEY_KP_1 to Key.NP1.ordinal,
            GLFW_KEY_KP_2 to Key.NP2.ordinal,
            GLFW_KEY_KP_3 to Key.NP3.ordinal,
            GLFW_KEY_KP_4 to Key.NP4.ordinal,
            GLFW_KEY_KP_5 to Key.NP5.ordinal,
            GLFW_KEY_KP_6 to Key.NP6.ordinal,
            GLFW_KEY_KP_7 to Key.NP7.ordinal,
            GLFW_KEY_KP_8 to Key.NP8.ordinal,
            GLFW_KEY_KP_9 to Key.NP9.ordinal,
            GLFW_KEY_KP_MULTIPLY to Key.NP_MUL.ordinal,
            GLFW_KEY_KP_ADD to Key.NP_ADD.ordinal,
            GLFW_KEY_KP_DIVIDE to Key.NP_DIV.ordinal,
            GLFW_KEY_KP_SUBTRACT to Key.NP_SUB.ordinal,
            GLFW_KEY_KP_DECIMAL to Key.NP_DECIMAL.ordinal,
            GLFW_KEY_KP_ENTER to Key.ENTER.ordinal
        )
    private val mouseButtonMap =
        mapOf(GLFW_MOUSE_BUTTON_LEFT to 1, GLFW_MOUSE_BUTTON_RIGHT to 2, GLFW_MOUSE_BUTTON_MIDDLE to 3)

    private data class WindowCallbackContext(
        val keyNewState: Array<Boolean>,
        val mouseNewState: Array<Boolean>,
        val focusState: FocusState,
        val updateMouse: (Double, Double) -> Unit,
        val updateMouseWheel: (Double) -> Unit,
        val keyMap: Map<Int, Int>,
        val mouseMap: Map<Int, Int>
    )

    companion object {
        var bAtomActive: Int = 0

        private fun windowContext(windowPtr: CPointer<GLFWwindow>?): WindowCallbackContext? {
            val pointer = windowPtr?.let { glfwGetWindowUserPointer(it) } ?: return null
            return pointer.asStableRef<WindowCallbackContext>().get()
        }

        private val keyCallback =
            staticCFunction { windowPtr: CPointer<GLFWwindow>?, key: Int, scancode: Int, action: Int, mods: Int ->
                val context = windowContext(windowPtr) ?: return@staticCFunction
                val index = context.keyMap[key] ?: return@staticCFunction
                when (action) {
                    GLFW_PRESS, GLFW_REPEAT -> context.keyNewState[index] = true
                    GLFW_RELEASE -> context.keyNewState[index] = false
                }
            }

        private val mouseButtonCallback =
            staticCFunction { windowPtr: CPointer<GLFWwindow>?, button: Int, action: Int, mods: Int ->
                val context = windowContext(windowPtr) ?: return@staticCFunction
                val index = context.mouseMap[button] ?: return@staticCFunction
                when (action) {
                    GLFW_PRESS, GLFW_REPEAT -> context.mouseNewState[index] = true
                    GLFW_RELEASE -> context.mouseNewState[index] = false
                }
            }

        private val cursorPosCallback = staticCFunction { windowPtr: CPointer<GLFWwindow>?, x: Double, y: Double ->
            val context = windowContext(windowPtr) ?: return@staticCFunction
            context.updateMouse(x, y)
        }

        private val scrollCallback =
            staticCFunction { windowPtr: CPointer<GLFWwindow>?, xoffset: Double, yoffset: Double ->
                val context = windowContext(windowPtr) ?: return@staticCFunction
                context.updateMouseWheel(yoffset)
            }

        private val cursorEnterCallback = staticCFunction { windowPtr: CPointer<GLFWwindow>?, entered: Int ->
            val context = windowContext(windowPtr) ?: return@staticCFunction
            context.focusState.bHasMouseFocus = entered == GLFW_TRUE
        }

        private val focusCallback = staticCFunction { windowPtr: CPointer<GLFWwindow>?, focused: Int ->
            val context = windowContext(windowPtr) ?: return@staticCFunction
            context.focusState.bHasInputFocus = focused == GLFW_TRUE
        }
    }
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
