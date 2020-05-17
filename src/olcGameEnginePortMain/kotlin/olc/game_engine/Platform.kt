package olc.game_engine

import cglfw.GLFW_CURSOR
import cglfw.GLFW_CURSOR_HIDDEN
import cglfw.GLFW_STICKY_KEYS
import cglfw.glfwSetInputMode
import com.kgl.glfw.*
import com.kgl.opengl.GL_TRUE
import com.kgl.opengl.glViewport
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.asStableRef

interface Platform {
    fun applicationStartUp(): RetCode
    fun applicationCleanUp(): RetCode
    fun threadStartUp(): RetCode
    fun threadCleanUp(): RetCode
    fun createGraphics(fullScreen: Boolean, enableVSYNC: Boolean, viewPos: Vi2d, viewSize: Vi2d): RetCode
    fun createWindowPane(windowPos: Vi2d, windowSize: Vi2d, fullScreen: Boolean): RetCode
    fun setWindowTitle(s: String): RetCode
    fun startSystemEventLoop(): RetCode
    fun handleSystemEvent(): RetCode
    fun shouldClose(): Boolean
}

@ExperimentalUnsignedTypes
class PlatformGlfwImpl(
    private val pge: PixelGameEngineImpl,
    private val renderer: Renderer
) : Platform {
    private lateinit var window: Window

    private val stableRefList = mutableListOf<StableRef<Any>>()

    override fun applicationStartUp(): RetCode {
        println("pge::applicationStartUp called")
        Glfw.init()
        return RetCode.OK
    }

    override fun applicationCleanUp(): RetCode {
        println("pge::applicationCleanUp called")
        Glfw.terminate()
        stableRefList.forEach { it.dispose() }
        return RetCode.OK
    }

    override fun threadStartUp() = RetCode.OK

    override fun threadCleanUp(): RetCode {
        println("pge::threadCleanUp called")
        renderer.destroyDevice()
        window.close()
        return RetCode.OK
    }

    override fun createGraphics(fullScreen: Boolean, enableVSYNC: Boolean, viewPos: Vi2d, viewSize: Vi2d): RetCode {
        println("pge::createGraphics called")
        return renderer.createDevice(params = emptyList(), fullScreen = fullScreen, vSync = enableVSYNC)
    }

    override fun createWindowPane(windowPos: Vi2d, windowSize: Vi2d, fullScreen: Boolean): RetCode {
        println("pge::createWindowPane called")
        this.window = Window(windowSize.x, windowSize.y, "") {
            samples = 4
            contextVersionMajor = 3
            contextVersionMinor = 3
            openGLProfile = OpenGLProfile.Core
            maximized = fullScreen

            if (kotlin.native.Platform.osFamily == OsFamily.MACOSX) {
                openGLForwardCompat = true
            }
        }

        // Have to take values from framebuffer to make little macbook happy
        pge.windowSize.x = window.frameBufferSize.first
        pge.windowSize.y = window.frameBufferSize.second
        pge.vViewSize = Vi2d(pge.windowSize.x, pge.windowSize.y)
        pge.olcUpdateViewport()

        Glfw.currentContext = window

        val pgeWindowSizeStableRef = StableRef.create(pge.windowSize).also { stableRefList.add(it) }.asCPointer()
        val pgeUpdateViewportStableRef =
            StableRef.create(pge::olcUpdateViewport).also { stableRefList.add(it) }.asCPointer()

        window.setFrameBufferCallback { _, width, height ->
            val pgeWindowSize = pgeWindowSizeStableRef.asStableRef<Vi2d>().get()
            val pgeUpdateViewport = pgeUpdateViewportStableRef.asStableRef<() -> Unit>().get()
            pgeWindowSize.x = width
            pgeWindowSize.y = height
            pgeUpdateViewport()
            glViewport(0, 0, width, height)
        }

        // Ensure we can capture the escape key being pressed below
        glfwSetInputMode(window.ptr, GLFW_STICKY_KEYS, GL_TRUE.toInt())
        // Hide the mouse and enable unlimited mouvement
        glfwSetInputMode(window.ptr, GLFW_CURSOR, GLFW_CURSOR_HIDDEN)

        // Keyboard keys handler
        val keyNewStateStableRef = StableRef.create(pge.keyNewState).also { stableRefList.add(it) }.asCPointer()
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
        val mouseNewStateStableRef = StableRef.create(pge.mouseNewState).also { stableRefList.add(it) }.asCPointer()
        val mouseKeysStableRef = StableRef.create(mouseKeysMap).also { stableRefList.add(it) }.asCPointer()
        val mouseMove = StableRef.create(pge::updateMouse).also { stableRefList.add(it) }.asCPointer()
        val mouseScroll = StableRef.create(pge::updateMouseWheel).also { stableRefList.add(it) }.asCPointer()
        val focusStableRef = StableRef.create(pge.focusState).also { stableRefList.add(it) }.asCPointer()

        window.setMouseButtonCallback { _, mouseButton, action, _ ->
            val mouseNewState = mouseNewStateStableRef.asStableRef<Array<Boolean>>().get()
            val mouseKeysMap = mouseKeysStableRef.asStableRef<Map<MouseButton, Int>>().get()
//            println("$mouseButton: $action")
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
            val focusState = focusStableRef.asStableRef<PixelGameEngineImpl.FocusState>().get()
            focusState.bHasMouseFocus = b
        }
        window.setFocusCallback { _, b ->
            val focusState = focusStableRef.asStableRef<PixelGameEngineImpl.FocusState>().get()
            focusState.bHasInputFocus = b
        }

        return RetCode.OK
    }

    override fun setWindowTitle(s: String): RetCode {
        this.window.setTitle(s)
        return RetCode.OK
    }

    override fun startSystemEventLoop() = RetCode.OK

    override fun handleSystemEvent(): RetCode {
        Glfw.pollEvents()
        pge.olcRefreshKeyboardAndMouseState()
        return RetCode.OK
    }

    override fun shouldClose(): Boolean = window.shouldClose

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
            KeyboardKey.LEFT_ALT to Key.ALT.ordinal,

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
        mapOf(MouseButton.LEFT to 1, MouseButton.RIGHT to 2, MouseButton.MIDDLE to 3)

}