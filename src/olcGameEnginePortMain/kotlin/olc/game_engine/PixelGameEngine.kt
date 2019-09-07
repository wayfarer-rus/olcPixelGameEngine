package olc.game_engine

import cglfw.GLFW_CURSOR
import cglfw.GLFW_CURSOR_HIDDEN
import cglfw.GLFW_STICKY_KEYS
import cglfw.glfwSetInputMode
import com.kgl.glfw.*
import com.kgl.opengl.*
import copengl.GLuint
import copengl.GLuintVar
import kotlinx.cinterop.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.native.concurrent.AtomicInt
import kotlin.system.getTimeNanos

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

    fun construct(screen_w: Int, screen_h: Int, pixel_w: Int, pixel_h: Int, full_screen: Boolean = false): rcode
    fun start(): rcode

    fun onUserCreate(): Boolean
    fun onUserUpdate(elapsedTime: Long): Boolean
//    fun onUserDestroy(): Boolean

    fun isFocused(): Boolean
    fun isMouseInWindow(): Boolean
    fun getKey(k: Key): HWButton
    fun getMouseKey(b: Int): HWButton
    fun getMouseX(): Int
    fun getMouseY(): Int
    fun getMouseWheel(): Int
    fun screenWidth(): Int
    fun screenHeight(): Int
    fun getDrawTargetWidth(): Int
    fun getDrawTargetHeight(): Int
    fun getDrawTarget(): Sprite
    fun setDrawTarget(target: Sprite)
    fun setPixelMode(m: Pixel.Mode)
    // fun SetPixelMode(fun)
    fun getPixelMode(): Pixel.Mode

    fun setPixelBlend(blend: Float)
    fun setSubPixelOffset(ox: Float, oy: Float)

    fun draw(x: Int, y: Int, p: Pixel = Pixel.WHITE)

    fun drawLine(start: Pair<Int, Int>, end: Pair<Int, Int>, p: Pixel, pattern: UInt = 0xFFFFFFFFu)
    fun drawCircle(x: Int, y: Int, radius: Int, p: Pixel = Pixel.WHITE, mask: UByte = 0xFFu)
    fun fillCircle(x: Int, y: Int, radius: Int, p: Pixel = Pixel.WHITE)
    fun drawRect(x: Int, y: Int, w: Int, h: Int, p: Pixel = Pixel.WHITE)
    fun fillRect(x: Int, y: Int, w: Int, h: Int, p: Pixel = Pixel.WHITE)
    fun drawTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun fillTriangle(point1: Pair<Int, Int>, point2: Pair<Int, Int>, point3: Pair<Int, Int>, p: Pixel = Pixel.WHITE)
    fun drawSprite(x: Int, y: Int, sprite: Sprite, scale: Int = 1)
    fun drawPartialSprite(x: Int, y: Int, sprite: Sprite, ox: Int, oy: Int, w: Int, h: Int, scale: Int = 1)
    fun drawString(x: Int, y: Int, text: String, col: Pixel = Pixel.WHITE, scale: Int = 1)
    fun clear(p: Pixel)
    fun getFPS(): Int
}

@ExperimentalUnsignedTypes
abstract class PixelGameEngineImpl : PixelGameEngine {
    override val appName: String = "Undefined"

    // shady init method
    // why do we need that global object?
    init {
        PGEX.pge = this
    }

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

        olcConstructFontsheet()
        pDefaultDrawTarget = SpriteImpl(nScreenWidth, nScreenHeight)
        setDrawTarget(pDefaultDrawTarget)
        return rcode.OK
    }

    override fun start(): rcode {
        println("Start called")
        Glfw.init()

        if (!olcWindowCreate()) return rcode.FAIL

        olcOpenGlCreate()

        if (onUserCreate()) {
            bAtomActive.value = 1
            engineMainLoop()
        }

        window.close()
        Glfw.terminate()
        stableRefList.forEach { it.dispose() }
        return rcode.OK
    }

    // Utility methods and flow control
    override fun setDrawTarget(target: Sprite) {
        pDrawTarget = target
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
                Pixel.Mode.NORMAL -> this.SetPixel(x, y, p)
                Pixel.Mode.MASK -> if (255.toUByte() == p.a) this.SetPixel(x, y, p)
                Pixel.Mode.ALPHA -> {
                    val d = this.GetPixel(x, y).let { d ->
                        val a = (p.af / 255.0f) * fBlendFactor
                        val c = 1.0f - a
                        val r = a * p.rf + c * d.rf
                        val g = a * p.gf + c * d.gf
                        val b = a * p.bf + c * d.bf
                        Pixel(r, g, b)
                    }

                    this.SetPixel(x, y, d)
                }
                Pixel.Mode.CUSTOM -> this.SetPixel(
                    x, y, funcPixelMode?.invoke(x, y, p, this.GetPixel(x, y)) ?: Pixel(0u)
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
                            if (fontSprite.GetPixel(i + ox * 8, j + oy * 8).r > 0.toUByte())
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
                            if (fontSprite.GetPixel(i + ox * 8, j + oy * 8).r > 0.toUByte())
                                draw(x + sx + i, y + sy + j, col)
                }
                sx += 8 * scale
            }
        }

        setPixelMode(m)
    }

    override fun clear(p: Pixel) {
        pDrawTarget.SetPixels(Array(pDrawTarget.GetPixels().size) { Pixel(p.n) })
        Sprite.nOverdrawCount += pDrawTarget.GetPixels().size
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

        val drawline: (Int, Int, Int) -> Unit = { sx, ex, ny ->
            for (i in sx..ex) draw(i, ny, p)
        }

        while (y0 >= x0) {
            // Modified to draw scan-lines instead of edges
            drawline(x - x0, x + x0, y - y0)
            drawline(x - y0, x + y0, y - x0)
            drawline(x - x0, x + x0, y + y0)
            drawline(x - y0, x + y0, y + x0)
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
        val x1 = probeRange(x, 0, nScreenWidth)
        val y1 = probeRange(y, 0, nScreenHeight)
        val x2 = probeRange(x + w, 0, nScreenWidth)
        val y2 = probeRange(y + h, 0, nScreenHeight)

        for (i in x1 until x2)
            for (j in y1 until y2)
                draw(i, j, p)
    }

    private fun probeRange(x: Int, begin: Int, end: Int): Int {
        return when {
            x < begin -> begin
            x >= end -> end
            else -> x
        }
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
                                sprite.GetPixel(i, j)
                            )
        } else {
            for (i in 0 until sprite.width)
                for (j in 0 until sprite.height)
                    draw(x + i, y + j, sprite.GetPixel(i, j))
        }
    }

    override fun drawPartialSprite(x: Int, y: Int, sprite: Sprite, ox: Int, oy: Int, w: Int, h: Int, scale: Int) {
        if (scale > 1) {
            for (i in 0 until w)
                for (j in 0 until h)
                    for (`is` in 0 until scale)
                        for (js in 0 until scale)
                            draw(
                                x + (i * scale) + `is`,
                                y + (j * scale) + js,
                                sprite.GetPixel(i + ox, j + oy)
                            )
        } else {
            for (i in 0 until w)
                for (j in 0 until h)
                    draw(x + i, y + j, sprite.GetPixel(i + ox, j + oy))
        }
    }

    // main loop
    private fun engineMainLoop() {
        println("EngineThread() called")
        println("programId = $programId")

        val texID = glGetUniformLocation(programId, "renderedTexture".cstr)
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
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)

        vertexBufferData.usePinned {
            glBufferData(GL_ARRAY_BUFFER, vertexBufferData.size.toLong() * 4, it.addressOf(0), GL_STATIC_DRAW)
        }

        glBindVertexArray(vertexArrayID)
        glEnableVertexAttribArray(0U)
        glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer)
        glVertexAttribPointer(0U, 4, GL_FLOAT, false.toByte().toUByte(), 0, 0L.toCPointer<CPointed>())
        glBindBuffer(GL_ARRAY_BUFFER, 0u)
        glBindVertexArray(0u)

        var tp1 = getTimeNanos()
        var tp2: Long

        do {
            glViewport(nViewX, nViewY, nViewW, nViewH)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT or GL_STENCIL_BUFFER_BIT)
            glUseProgram(programId)

            tp2 = getTimeNanos()
            val elapsedTime = tp2 - tp1
            tp1 = tp2

            olcRefreshKeyboardAndMouseState()

            Sprite.nOverdrawCount = 0

            // Handle Frame Update
            if (!onUserUpdate(elapsedTime))
                bAtomActive.value = 0

            // Copy pixel array into texture
            glActiveTexture(GL_TEXTURE0)
            glBindTexture(GL_TEXTURE_2D, glBuffer)

            glTexSubImage2D(
                GL_TEXTURE_2D,
                0,
                0,
                0,
                nScreenWidth,
                nScreenHeight,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                pDrawTarget.GetData().toCValues()
            )

            glUniform1i(texID, 0)
            glBindVertexArray(vertexArrayID)
            glDrawArrays(GL_TRIANGLES, 0, 6)
            glBindVertexArray(0u)

            pollEvents()
            window.swapBuffers()

            // Update Title Bar
            fFrameTimer += elapsedTime / 1_000_000_000.0F
            nFrameCount++

            if (fFrameTimer >= 1.0f) {
                fFrameTimer -= 1.0f
                val sTitle = "OneLoneCoder.com - Pixel Game Engine - $appName - FPS: $nFrameCount"
                fps = nFrameCount
                window.setTitle(sTitle)
                nFrameCount = 0
            }
        } while (!window.shouldClose && bAtomActive.value == 1)

//        onUserDestroy()

        println("EngineThread() return")
    }

    // internal handlers and functions
    private fun olcWindowCreate(): Boolean {
        println("olc_WindowCreate() called")
        window = Window(
            (nScreenWidth * nPixelWidth),
            (nScreenHeight * nPixelHeight),
            appName,
            null,
            null
        ) {
            samples = 4
            contextVersionMajor = 3
            contextVersionMinor = 3
            openGLForwardCompat = true
            openGLProfile = OpenGLProfile.Core
            resizable = false
        }

        // Have to take values from framebuffer to make little macbook happy
        nWindowWidth = window.frameBufferSize.first
        nWindowHeight = window.frameBufferSize.second

        nViewW = nWindowWidth
        nViewH = nWindowHeight

        Glfw.currentContext = window

        // Ensure we can capture the escape key being pressed below
        glfwSetInputMode(window.ptr, GLFW_STICKY_KEYS, GL_TRUE.toInt())
        // Hide the mouse and enable unlimited mouvement
        glfwSetInputMode(window.ptr, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)

        // Keyboard keys handler
        val keyNewStateStableRef = StableRef.create(pKeyNewState).also { stableRefList.add(it) }.asCPointer()
        val mapKeysStableRef = StableRef.create(keyboardKeysLocalMap).also { stableRefList.add(it) }.asCPointer()

        window.setKeyCallback { _, keyboardKey, _, action, _ ->
            val keyNewState = keyNewStateStableRef.asStableRef<Array<Boolean>>().get()
            val mapKeys = mapKeysStableRef.asStableRef<Map<KeyboardKey, Int>>().get()
            mapKeys[keyboardKey]?.also {
                when (action) {
                    Action.Press -> keyNewState[it] = true
                    Action.Release -> keyNewState[it] = false
                    Action.Repeat -> keyNewState[it] = true
                }
            } ?: println("Unsupported key-code: $keyboardKey ")
        }

        // Mouse handler
        val mouseNewStateStableRef = StableRef.create(pMouseNewState).also { stableRefList.add(it) }.asCPointer()
        val mouseKeysStableRef = StableRef.create(mouseKeysMap).also { stableRefList.add(it) }.asCPointer()
        val mouseMove = StableRef.create(this::olcUpdateMouse).also { stableRefList.add(it) }.asCPointer()
        val mouseScroll = StableRef.create(this::olcUpdateMouseWheel).also { stableRefList.add(it) }.asCPointer()
        val focusStableRef = StableRef.create(focusState).also { stableRefList.add(it) }.asCPointer()

        window.setMouseButtonCallback { _, mouseButton, action, _ ->
            val mouseNewState = mouseNewStateStableRef.asStableRef<Array<Boolean>>().get()
            val mouseKeysMap = mouseKeysStableRef.asStableRef<Map<MouseButton, Int>>().get()
            mouseKeysMap[mouseButton]?.also {
                when (action) {
                    Action.Press -> mouseNewState[it] = true
                    Action.Release -> mouseNewState[it] = false
                    Action.Repeat -> mouseNewState[it] = true
                }
            } ?: println("Unsupported mouse-key: $mouseButton ")
        }
        window.setCursorPosCallback { _, x, y ->
            val mouseUpdateFunction = mouseMove.asStableRef<(Double, Double) -> Unit>().get()
            mouseUpdateFunction(x, y)
        }
        window.setScrollCallback { _, _, yo ->
            val mouseScrollUpdateFunction = mouseScroll.asStableRef<(Double) -> Unit>().get()
            // we support only vertical scroll
            mouseScrollUpdateFunction(yo)
        }
        window.setCursorEnterCallback { _, b ->
            val focusState = focusStableRef.asStableRef<FocusState>().get()
            focusState.bHasMouseFocus = b
        }
        window.setFocusCallback { _, b ->
            val focusState = focusStableRef.asStableRef<FocusState>().get()
            focusState.bHasInputFocus = b
        }

        // Initialization complete. Print OpenGL version and update the viewport values
        println(glGetString(GL_VERSION)!!.reinterpret<ByteVar>().toKString())
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

    private fun olcOpenGlCreate(): Boolean {
        println("olc_OpenGLCreate() called")

        // Create Screen Texture - disable filtering
        glEnable(GL_TEXTURE_2D)

        glBuffer = memScoped {
            val output = alloc<GLuintVar>()
            glGenTextures(1, output.ptr)
            output.value
        }

        println("glBuffer = $glBuffer")

        glBindTexture(GL_TEXTURE_2D, glBuffer)

        // this call will create our "canvas" to draw into
        glTexImage2D(
            GL_TEXTURE_2D, 0, GL_RGBA.toInt(),
            nScreenWidth,
            nScreenHeight,
            0, GL_RGBA, GL_UNSIGNED_BYTE,
            this.pDefaultDrawTarget.GetData().toCValues()
        )

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST.toInt())
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toInt())

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

        fontSprite = SpriteImpl(128, 48)
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

                fontSprite.SetPixel(px, py, Pixel(k, k, k, k))

                if (++py == 48) {
                    px++
                    py = 0
                }
            }
        }
    }

    private fun olcLoadShaders(): GLuint {
        return memScoped {
            val vertexShaderId = glCreateShader(GL_VERTEX_SHADER)
            val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER)

            glShaderSource(vertexShaderId, 1, arrayOf(vertexShaderSource).toCStringArray(memScope), null)
            glCompileShader(vertexShaderId)

            glGetShaderInfoLog(vertexShaderId).also {
                if (it.isNotBlank()) println(it)
            }

            glShaderSource(fragmentShaderId, 1, arrayOf(fragmentShaderSource).toCStringArray(memScope), null)
            glCompileShader(fragmentShaderId)

            glGetShaderInfoLog(fragmentShaderId).also {
                if (it.isNotBlank()) println(it)
            }

            val programId = glCreateProgram()
            glAttachShader(programId, vertexShaderId)
            glAttachShader(programId, fragmentShaderId)
            glLinkProgram(programId)

            glGetProgramInfoLog(programId).also {
                if (it.isNotBlank()) println(it)
            }

            glDetachShader(programId, vertexShaderId)
            glDetachShader(programId, fragmentShaderId)

            glDeleteShader(vertexShaderId)
            glDeleteShader(fragmentShaderId)

            programId
        }
    }

    private lateinit var window: Window
    private var programId: UInt = 0u
    private var glBuffer = 0u
    private lateinit var pDefaultDrawTarget: Sprite
    private lateinit var pDrawTarget: Sprite
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

    private val stableRefList = mutableListOf<StableRef<Any>>()
    private val keyboardKeysLocalMap =
        mapOf(
            KeyboardKey.UNKNOWN to Key.NONE.ordinal,
            KeyboardKey.A to Key.A.ordinal,
            KeyboardKey.B to Key.B.ordinal,
            KeyboardKey.C to Key.C.ordinal,
            KeyboardKey.D to Key.D.ordinal,
            KeyboardKey.E to Key.E.ordinal,
            KeyboardKey.F to Key.F.ordinal,
            KeyboardKey.G to Key.G.ordinal,
            KeyboardKey.H to Key.H.ordinal,
            KeyboardKey.I to Key.I.ordinal,
            KeyboardKey.J to Key.J.ordinal,
            KeyboardKey.K to Key.K.ordinal,
            KeyboardKey.L to Key.L.ordinal,
            KeyboardKey.M to Key.M.ordinal,
            KeyboardKey.N to Key.N.ordinal,
            KeyboardKey.O to Key.O.ordinal,
            KeyboardKey.P to Key.P.ordinal,
            KeyboardKey.Q to Key.Q.ordinal,
            KeyboardKey.R to Key.R.ordinal,
            KeyboardKey.S to Key.S.ordinal,
            KeyboardKey.T to Key.T.ordinal,
            KeyboardKey.U to Key.U.ordinal,
            KeyboardKey.V to Key.V.ordinal,
            KeyboardKey.W to Key.W.ordinal,
            KeyboardKey.X to Key.X.ordinal,
            KeyboardKey.Y to Key.Y.ordinal,
            KeyboardKey.Z to Key.Z.ordinal,

            KeyboardKey.F1 to Key.F1.ordinal,
            KeyboardKey.F2 to Key.F2.ordinal,
            KeyboardKey.F3 to Key.F3.ordinal,
            KeyboardKey.F4 to Key.F4.ordinal,
            KeyboardKey.F5 to Key.F5.ordinal,
            KeyboardKey.F6 to Key.F6.ordinal,
            KeyboardKey.F7 to Key.F7.ordinal,
            KeyboardKey.F8 to Key.F8.ordinal,
            KeyboardKey.F9 to Key.F9.ordinal,
            KeyboardKey.F10 to Key.F10.ordinal,
            KeyboardKey.F11 to Key.F11.ordinal,
            KeyboardKey.F12 to Key.F12.ordinal,

            KeyboardKey.DOWN to Key.DOWN.ordinal,
            KeyboardKey.LEFT to Key.LEFT.ordinal,
            KeyboardKey.RIGHT to Key.RIGHT.ordinal,
            KeyboardKey.UP to Key.UP.ordinal,

            KeyboardKey.ENTER to Key.ENTER.ordinal,
            KeyboardKey.BACKSPACE to Key.BACK.ordinal,
            KeyboardKey.ESCAPE to Key.ESCAPE.ordinal,
            KeyboardKey.SPACE to Key.SPACE.ordinal,
            KeyboardKey.TAB to Key.TAB.ordinal,
            KeyboardKey.SCROLL_LOCK to Key.SCROLL.ordinal,

            KeyboardKey.INSERT to Key.INS.ordinal,
            KeyboardKey.DELETE to Key.DEL.ordinal,
            KeyboardKey.HOME to Key.HOME.ordinal,
            KeyboardKey.END to Key.END.ordinal,
            KeyboardKey.PAGE_UP to Key.PGUP.ordinal,
            KeyboardKey.PAGE_DOWN to Key.PGDN.ordinal,
            KeyboardKey.PAUSE to Key.PAUSE.ordinal,

            KeyboardKey.LEFT_SHIFT to Key.SHIFT.ordinal,
            KeyboardKey.RIGHT_SHIFT to Key.SHIFT.ordinal,
            KeyboardKey.LEFT_CONTROL to Key.CTRL.ordinal,
            KeyboardKey.RIGHT_CONTROL to Key.CTRL.ordinal,

            KeyboardKey._0 to Key.K0.ordinal,
            KeyboardKey._1 to Key.K1.ordinal,
            KeyboardKey._2 to Key.K2.ordinal,
            KeyboardKey._3 to Key.K3.ordinal,
            KeyboardKey._4 to Key.K4.ordinal,
            KeyboardKey._5 to Key.K5.ordinal,
            KeyboardKey._6 to Key.K6.ordinal,
            KeyboardKey._7 to Key.K7.ordinal,
            KeyboardKey._8 to Key.K8.ordinal,
            KeyboardKey._9 to Key.K9.ordinal,

            KeyboardKey.KP_0 to Key.NP0.ordinal,
            KeyboardKey.KP_1 to Key.NP1.ordinal,
            KeyboardKey.KP_2 to Key.NP2.ordinal,
            KeyboardKey.KP_3 to Key.NP3.ordinal,
            KeyboardKey.KP_4 to Key.NP4.ordinal,
            KeyboardKey.KP_5 to Key.NP5.ordinal,
            KeyboardKey.KP_6 to Key.NP6.ordinal,
            KeyboardKey.KP_7 to Key.NP7.ordinal,
            KeyboardKey.KP_8 to Key.NP8.ordinal,
            KeyboardKey.KP_9 to Key.NP9.ordinal,
            KeyboardKey.KP_MULTIPLY to Key.NP_MUL.ordinal,
            KeyboardKey.KP_ADD to Key.NP_ADD.ordinal,
            KeyboardKey.KP_DIVIDE to Key.NP_DIV.ordinal,
            KeyboardKey.KP_SUBTRACT to Key.NP_SUB.ordinal,
            KeyboardKey.KP_DECIMAL to Key.NP_DECIMAL.ordinal,
            KeyboardKey.KP_ENTER to Key.ENTER.ordinal
        )
    private val mouseKeysMap =
        mapOf(MouseButton.LEFT to 1, MouseButton.RIGHT to 2, MouseButton.MIDDLE to 3, MouseButton.LAST to 4)

    companion object {
        var bAtomActive: AtomicInt = AtomicInt(0)
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

// by default this object is frozen
// to change it, it must be @ThreadLocal
@ThreadLocal
object PGEX {
    @ExperimentalUnsignedTypes
    var pge: PixelGameEngineImpl? = null
}