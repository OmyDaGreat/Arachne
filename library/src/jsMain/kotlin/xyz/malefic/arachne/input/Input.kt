package xyz.malefic.arachne.input

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.events.WheelEvent
import xyz.malefic.arachne.math.Vector2

object Input {
    private val keysDown = mutableSetOf<String>()
    private val keysPressed = mutableSetOf<String>()
    private val keysReleased = mutableSetOf<String>()

    private val mouseButtonsDown = mutableSetOf<Short>()
    private val mouseButtonsPressed = mutableSetOf<Short>()
    private val mouseButtonsReleased = mutableSetOf<Short>()

    private var mousePosition = Vector2()
    private var mouseScroll = 0f
    private var canvas: HTMLCanvasElement? = null

    private var initialized = false

    fun initialize(targetCanvas: HTMLCanvasElement? = null) {
        if (initialized) return
        initialized = true
        canvas = targetCanvas

        // Initialize touch input as well
        TouchInput.initialize(targetCanvas)

        // Keyboard events
        window.addEventListener("keydown", { event ->
            event as KeyboardEvent
            val key = event.key
            if (!keysDown.contains(key)) {
                keysPressed.add(key)
            }
            keysDown.add(key)
        })

        window.addEventListener("keyup", { event ->
            event as KeyboardEvent
            val key = event.key
            keysDown.remove(key)
            keysReleased.add(key)
        })

        // Mouse events
        val target = targetCanvas ?: document

        target.addEventListener("mousemove", { event ->
            event as MouseEvent
            if (targetCanvas != null) {
                val rect = targetCanvas.getBoundingClientRect()
                mousePosition.x = (event.clientX - rect.left).toFloat()
                mousePosition.y = (event.clientY - rect.top).toFloat()
            } else {
                mousePosition.x = event.clientX.toFloat()
                mousePosition.y = event.clientY.toFloat()
            }
        })

        target.addEventListener("mousedown", { event ->
            event as MouseEvent
            val button = event.button
            if (!mouseButtonsDown.contains(button)) {
                mouseButtonsPressed.add(button)
            }
            mouseButtonsDown.add(button)
        })

        target.addEventListener("mouseup", { event ->
            event as MouseEvent
            val button = event.button
            mouseButtonsDown.remove(button)
            mouseButtonsReleased.add(button)
        })

        target.addEventListener("wheel", { event ->
            event as WheelEvent
            mouseScroll = event.deltaY.toFloat()
        })
    }

    fun update() {
        keysPressed.clear()
        keysReleased.clear()
        mouseButtonsPressed.clear()
        mouseButtonsReleased.clear()
        mouseScroll = 0f
        TouchInput.update()
    }

    // Keyboard
    fun isKeyDown(key: String) = keysDown.contains(key)

    fun isKeyPressed(key: String) = keysPressed.contains(key)

    fun isKeyReleased(key: String) = keysReleased.contains(key)

    // Mouse
    fun getMousePosition() = mousePosition.copy()

    fun getMouseX() = mousePosition.x

    fun getMouseY() = mousePosition.y

    fun isMouseButtonDown(button: Short = 0) = mouseButtonsDown.contains(button)

    fun isMouseButtonPressed(button: Short = 0) = mouseButtonsPressed.contains(button)

    fun isMouseButtonReleased(button: Short = 0) = mouseButtonsReleased.contains(button)

    fun getMouseScroll() = mouseScroll

    // Mouse button constants
    const val MOUSE_LEFT: Short = 0
    const val MOUSE_MIDDLE: Short = 1
    const val MOUSE_RIGHT: Short = 2
}
