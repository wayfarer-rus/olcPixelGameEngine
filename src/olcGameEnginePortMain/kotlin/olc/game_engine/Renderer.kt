package olc.game_engine

import com.kgl.glfw.Glfw
import com.kgl.opengl.*
import kotlinx.cinterop.*

inline val Float.Companion.SIZE_BYTES get() = Int.SIZE_BYTES

@ExperimentalUnsignedTypes
interface Renderer {
    fun prepareDevice()
    fun createDevice(params: List<Any>, fullScreen: Boolean, vSync: Boolean): RetCode
    fun destroyDevice(): RetCode
    fun displayFrame()
    fun prepareDrawing()
    fun drawLayerQuad(offset: Vf2d, scale: Vf2d, tint: Pixel)
    fun drawDecalQuad(decal: DecalInstance)
    fun createTexture(width: UInt, height: UInt): UInt
    fun updateTexture(id: UInt, spr: Sprite)
    fun deleteTexture(id: UInt): UInt
    fun applyTexture(id: UInt)
    fun updateViewport(pos: Vf2d, size: Vf2d) // Vi2d
    fun clearBuffer(p: Pixel, depth: Boolean)
    fun createTexture(width: Int, height: Int): UInt
}

@ExperimentalUnsignedTypes
class RendererGlfwImpl : Renderer {
    private var layerShadersProgramId: UInt = 0u
    private var textureShadersProgramId: UInt = 0u

    override fun prepareDevice() {
        // nothing to do here
    }

    override fun createDevice(params: List<Any>, fullScreen: Boolean, vSync: Boolean): RetCode {
        println("pge::createDevice called")
        glEnable(GL_TEXTURE_2D)
        loadLayerShaders()
        loadTextureShaders()

        return RetCode.OK
    }

    private fun loadTextureShaders() {
        this.textureShadersProgramId = loadShaders(textureVertexShaderSource, textureFragmentShaderSource)
    }

    private fun loadLayerShaders() {
        this.layerShadersProgramId = loadShaders(layerVertexShaderSource, layerFragmentShaderSource)
    }

    override fun destroyDevice() = RetCode.OK

    override fun displayFrame() {
        Glfw.currentContext?.swapBuffers()
    }

    override fun prepareDrawing() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun drawLayerQuad(offset: Vf2d, scale: Vf2d, tint: Pixel) {
        val color = tint.asFloatArray().map { it / 255.0f }.toFloatArray()
        val vertices = floatArrayOf(
            // positions          // colors           // texture coords
            1f, 1f, 0.0f, *color, 1.0f * scale.x + offset.x, 0.0f * scale.y + offset.y, // top right
            1f, -1f, 0.0f, *color, 1.0f * scale.x + offset.x, 1.0f * scale.y + offset.y, // bottom right
            -1f, -1f, 0.0f, *color, 0.0f * scale.x + offset.x, 1.0f * scale.y + offset.y, // bottom left
            -1f, 1f, 0.0f, *color, 0.0f * scale.x + offset.x, 0.0f * scale.y + offset.y  // top left
        )
        val indices = intArrayOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
        )

        val vao = glGenVertexArray()
        val vbo = glGenBuffer()
        val ebo = glGenBuffer()

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, (vertices.size * Float.SIZE_BYTES).convert(), vertices.refTo(0), GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            (indices.size * UInt.SIZE_BYTES).convert(),
            indices.refTo(0),
            GL_STATIC_DRAW
        )

        // position attribute
        glVertexAttribPointer(0u, 3, GL_FLOAT, false, 9 * Float.SIZE_BYTES, null)
        glEnableVertexAttribArray(0u)
        // color attribute
        glVertexAttribPointer(
            1u,
            4,
            GL_FLOAT,
            false,
            9 * Float.SIZE_BYTES,
            (3L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(1u)
        // texture coord attribute
        glVertexAttribPointer(
            2u,
            2,
            GL_FLOAT,
            false,
            9 * Float.SIZE_BYTES,
            (7L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(2u)

        glUseProgram(this.layerShadersProgramId)
        glUniform1i(glGetUniformLocation(this.layerShadersProgramId, "ourTexture"), 0)

        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, null)

        // release memory
        glDisableVertexAttribArray(0U)
        glDisableVertexAttribArray(1U)
        glDisableVertexAttribArray(2U)
        glDeleteVertexArrays(1, cValuesOf(vao))
        glDeleteBuffers(1, cValuesOf(vbo))
        glDeleteBuffers(1, cValuesOf(ebo))
    }

    override fun drawDecalQuad(decal: DecalInstance) {
        val color = decal.tint.asFloatArray().map { it / 255.0f }.toFloatArray()
        val vertices = floatArrayOf(
            // positions                        // colors   // texture coords
            decal.pos[2].x, decal.pos[2].y, *color, decal.uv[2].x, decal.uv[2].y, 0.0f, decal.w[2], // top right
            decal.pos[1].x, decal.pos[1].y, *color, decal.uv[1].x, decal.uv[1].y, 0.0f, decal.w[1], // bottom right
            decal.pos[0].x, decal.pos[0].y, *color, decal.uv[0].x, decal.uv[0].y, 0.0f, decal.w[0], // bottom left
            decal.pos[3].x, decal.pos[3].y, *color, decal.uv[3].x, decal.uv[3].y, 0.0f, decal.w[3]  // top left
        )
        val strideLen = 2 + 4 + 4
        val indices = intArrayOf(
            0, 1, 3, // first triangle
            1, 2, 3  // second triangle
        )

        val vao = glGenVertexArray()
        val vbo = glGenBuffer()
        val ebo = glGenBuffer()

        glBindVertexArray(vao)

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBufferData(GL_ARRAY_BUFFER, (vertices.size * Float.SIZE_BYTES).convert(), vertices.refTo(0), GL_STATIC_DRAW)

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            (indices.size * UInt.SIZE_BYTES).convert(),
            indices.refTo(0),
            GL_STATIC_DRAW
        )

        // position attribute
        glVertexAttribPointer(0u, 2, GL_FLOAT, false, strideLen * Float.SIZE_BYTES, null)
        glEnableVertexAttribArray(0u)
        // color attribute
        glVertexAttribPointer(
            1u,
            4,
            GL_FLOAT,
            false,
            strideLen * Float.SIZE_BYTES,
            (2L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(1u)
        // texture coord attribute
        glVertexAttribPointer(
            2u,
            4,
            GL_FLOAT,
            false,
            strideLen * Float.SIZE_BYTES,
            (6L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(2u)

        glBindTexture(GL_TEXTURE_2D, decal.decal.id)
        glUseProgram(this.textureShadersProgramId)
        glUniform1i(glGetUniformLocation(this.textureShadersProgramId, "ourTexture"), 0)

        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, null)

        // release memory
        glDisableVertexAttribArray(0U)
        glDisableVertexAttribArray(1U)
        glDisableVertexAttribArray(2U)
        glDeleteVertexArrays(1, cValuesOf(vao))
        glDeleteBuffers(1, cValuesOf(vbo))
        glDeleteBuffers(1, cValuesOf(ebo))
    }

    override fun createTexture(width: Int, height: Int) =
        this.createTexture(width.toUInt(), height.toUInt())

    override fun createTexture(width: UInt, height: UInt): UInt {
        val id = glGenTexture()
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, id)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST.toInt())
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST.toInt())
        return id
    }

    override fun updateTexture(id: UInt, spr: Sprite) {
        spr.data.usePinned {
            glTexImage2D(
                GL_TEXTURE_2D,
                0,
                GL_RGBA.toInt(),
                spr.width,
                spr.height,
                0,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                it.addressOf(0)
            )
        }
    }

    override fun deleteTexture(id: UInt): UInt {
        glDeleteTexture(id)
        return id
    }

    override fun applyTexture(id: UInt) {
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, id)
    }

    override fun updateViewport(pos: Vf2d, size: Vf2d) {
        // not sure whether we need this or not
        // window.setFrameBufferCallback should do the trick
        glViewport(pos.xi, pos.yi, size.xi, size.yi)
    }

    override fun clearBuffer(p: Pixel, depth: Boolean) {
        glClearColor(p.rf / 255.0f, p.gf / 255.0f, p.bf / 255.0f, p.af / 255.0f)
        glClear(GL_COLOR_BUFFER_BIT)
        if (depth) glClear(GL_DEPTH_BUFFER_BIT)
    }

    private fun loadShaders(vertexShaderSource: String, fragmentShaderSource: String): UInt {
        return run {
            val vertexShaderId = glCreateShader(GL_VERTEX_SHADER)
            val fragmentShaderId = glCreateShader(GL_FRAGMENT_SHADER)

            glShaderSource(vertexShaderId, vertexShaderSource)
            glCompileShader(vertexShaderId)

            glGetShaderInfoLog(vertexShaderId).also {
                if (it.isNotBlank()) println(it)
            }

            glShaderSource(fragmentShaderId, fragmentShaderSource)
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

}