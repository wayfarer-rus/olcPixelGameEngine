package olc.game_engine

import com.kgl.glfw.Glfw
import com.kgl.opengl.*
import copengl.GLuint
import io.ktor.utils.io.core.Closeable
import kotlinx.cinterop.*

inline val Float.Companion.SIZE_BYTES get() = Int.SIZE_BYTES

@ExperimentalUnsignedTypes
interface Renderer {
    fun prepareDevice()
    fun createDevice(params: List<Any>, fullScreen: Boolean, vSync: Boolean): RetCode
    fun destroyDevice(): RetCode
    fun displayFrame()
    fun prepareDrawing()

    //    fun drawLayerQuad(offset: Vf2d, scale: Vf2d, tint: Pixel)
    fun drawLayerQuad(layer: LayerDesc)
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

    private lateinit var layerQuadOpenGlData: OpenGlData
    private lateinit var decalQuadOpenGlData: OpenGlData
    private var ebo: GLuint = 0u
    private val indices = intArrayOf(
        0, 1, 3, // first triangle
        1, 2, 3  // second triangle
    )

    override fun prepareDevice() {
        // nothing to do here
    }

    override fun createDevice(params: List<Any>, fullScreen: Boolean, vSync: Boolean): RetCode {
        println("pge::createDevice called")
        glEnable(GL_TEXTURE_2D)
        loadLayerShaders()
        loadTextureShaders()

        this.ebo = glGenBuffer()
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo)
        glBufferData(
            GL_ELEMENT_ARRAY_BUFFER,
            (indices.size * UInt.SIZE_BYTES).convert(),
            indices.refTo(0),
            GL_STATIC_DRAW
        )

        this.decalQuadOpenGlData = OpenGlData(
            vao = glGenVertexArray(),
            vbo = glGenBuffer()
        )
        this.layerQuadOpenGlData = OpenGlData(
            vao = glGenVertexArray(),
            vbo = glGenBuffer()
        )

        return RetCode.OK
    }

    private class OpenGlData(val vao: GLuint, val vbo: GLuint) : Closeable {
        var vertices: MutableMap<Any, FloatArray> = mutableMapOf()

        override fun close() {
            glDeleteVertexArrays(1, cValuesOf(vao))
            glDeleteBuffers(1, cValuesOf(vbo))
        }

        operator fun component1(): GLuint {
            return vao
        }

        operator fun component2(): GLuint {
            return vbo
        }
    }

    private fun loadTextureShaders() {
        this.textureShadersProgramId = loadShaders(textureVertexShaderSource, textureFragmentShaderSource)
    }

    private fun loadLayerShaders() {
        this.layerShadersProgramId = loadShaders(layerVertexShaderSource, layerFragmentShaderSource)
    }

    override fun destroyDevice(): RetCode {
        glDisableVertexAttribArray(0U)
        glDisableVertexAttribArray(1U)
        glDisableVertexAttribArray(2U)
        glDeleteBuffers(1, cValuesOf(ebo))
        layerQuadOpenGlData.close()
        decalQuadOpenGlData.close()
        return RetCode.OK
    }

    override fun displayFrame() {
        Glfw.currentContext?.swapBuffers()
    }

    override fun prepareDrawing() {
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    override fun drawLayerQuad(layer: LayerDesc) {
        val offset = layer.offset
        val scale = layer.scale
        val tint = layer.tint
        val color = tint.toFloatArray()
        val vertices = floatArrayOf(
            // positions          // colors           // texture coords
            1f, 1f, 0.0f, *color, 1.0f * scale.x + offset.x, 0.0f * scale.y + offset.y, // top right
            1f, -1f, 0.0f, *color, 1.0f * scale.x + offset.x, 1.0f * scale.y + offset.y, // bottom right
            -1f, -1f, 0.0f, *color, 0.0f * scale.x + offset.x, 1.0f * scale.y + offset.y, // bottom left
            -1f, 1f, 0.0f, *color, 0.0f * scale.x + offset.x, 0.0f * scale.y + offset.y  // top left
        )

        // skip this frame, it's identical
        /*if (layerQuadOpenGlData.vertices.containsKey(layer.id) &&
            vertices contentEquals layerQuadOpenGlData.vertices[layer.id]!!
        ) {
            return
        }*/

        val strideLen = (3 + 4 + 2) * Float.SIZE_BYTES
        val (vao, vbo) = layerQuadOpenGlData
//        layerQuadOpenGlData.vertices[layer.id] = vertices

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
        glVertexAttribPointer(0u, 3, GL_FLOAT, false, strideLen, null)
        glEnableVertexAttribArray(0u)
        // color attribute
        glVertexAttribPointer(
            1u,
            4,
            GL_FLOAT,
            false,
            strideLen,
            (3L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(1u)
        // texture coord attribute
        glVertexAttribPointer(
            2u,
            2,
            GL_FLOAT,
            false,
            strideLen,
            (7L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(2u)

        glUseProgram(this.layerShadersProgramId)
        glBindVertexArray(vao)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, null)
    }

    override fun drawDecalQuad(decal: DecalInstance) {
//        println("draw decal quad started")
//        var t = getTimeNanos()

        val color = decal.tint.toFloatArray()
        val vertices = floatArrayOf(
            // positions                    // colors   // texture coords
            decal.pos[2].x, decal.pos[2].y, *color, decal.uv[2].x, decal.uv[2].y, 0.0f, decal.w[2], // top right
            decal.pos[1].x, decal.pos[1].y, *color, decal.uv[1].x, decal.uv[1].y, 0.0f, decal.w[1], // bottom right
            decal.pos[0].x, decal.pos[0].y, *color, decal.uv[0].x, decal.uv[0].y, 0.0f, decal.w[0], // bottom left
            decal.pos[3].x, decal.pos[3].y, *color, decal.uv[3].x, decal.uv[3].y, 0.0f, decal.w[3]  // top left
        ).map { kotlin.math.round(it * roundPrecision) / roundPrecision }.toFloatArray()

        // skip this frame, it's identical
        if (decal.decal.dirty.not() &&
            decalQuadOpenGlData.vertices.containsKey(decal.decal) &&
            vertices contentEquals decalQuadOpenGlData.vertices[decal.decal]!!
        ) {
//            println("1: ${getTimeNanos() - t}")
            return
        }

        val strideLen = (2 + 4 + 4) * Float.SIZE_BYTES
        val (vao, vbo) = decalQuadOpenGlData
        decalQuadOpenGlData.vertices[decal.decal] = vertices
        decal.decal.dirty = false

//        println("1: ${getTimeNanos() - t}")//2800 ~ 4800
//        t = getTimeNanos()

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

//        println("3: ${getTimeNanos() - t}")// 12300 ~ 1970200
//        t = getTimeNanos()

        // position attribute
        glVertexAttribPointer(0u, 2, GL_FLOAT, false, strideLen, null)
        glEnableVertexAttribArray(0u)
        // color attribute
        glVertexAttribPointer(
            1u,
            4,
            GL_FLOAT,
            false,
            strideLen,
            (2L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(1u)
        // texture coord attribute
        glVertexAttribPointer(
            2u,
            4,
            GL_FLOAT,
            false,
            strideLen,
            (6L * Float.SIZE_BYTES).toCPointer<CPointed>()
        )
        glEnableVertexAttribArray(2u)

//        println("4: ${getTimeNanos() - t}")
//        t = getTimeNanos()

        glBindTexture(GL_TEXTURE_2D, decal.decal.id)
//        applyTexture(decal.decal.id)
        glUseProgram(this.textureShadersProgramId)
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, null)

//        println("7: ${getTimeNanos() - t}")
//        println("draw decal quad started")
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

        return programId
    }

    companion object {
        const val roundPrecision = 10000.0f
    }
}