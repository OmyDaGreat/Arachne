package xyz.malefic.arachne.input

import kotlinx.browser.document
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.TouchEvent
import xyz.malefic.arachne.math.Vector2

enum class TouchPhase {
    BEGAN,
    MOVED,
    STATIONARY,
    ENDED,
    CANCELLED,
}

data class TouchData(
    val id: Int,
    val position: Vector2,
    val phase: TouchPhase,
    val deltaPosition: Vector2 = Vector2.ZERO.copy(),
    val tapCount: Int = 0,
)

object TouchInput {
    private val touches = mutableMapOf<Int, TouchData>()
    private val previousTouches = mutableMapOf<Int, TouchData>()
    private var canvas: HTMLCanvasElement? = null
    private var initialized = false

    fun initialize(targetCanvas: HTMLCanvasElement? = null) {
        if (initialized) return
        initialized = true
        canvas = targetCanvas

        val target = targetCanvas ?: document

        target.addEventListener(
            "touchstart",
            { event ->
                handleTouchEvent(event as TouchEvent, TouchPhase.BEGAN)
            },
        )

        target.addEventListener(
            "touchmove",
            { event ->
                handleTouchEvent(event as TouchEvent, TouchPhase.MOVED)
                event.preventDefault()
            },
        )

        target.addEventListener(
            "touchend",
            { event ->
                handleTouchEvent(event as TouchEvent, TouchPhase.ENDED)
            },
        )

        target.addEventListener(
            "touchcancel",
            { event ->
                handleTouchEvent(event as TouchEvent, TouchPhase.CANCELLED)
            },
        )
    }

    private fun handleTouchEvent(
        event: TouchEvent,
        phase: TouchPhase,
    ) {
        event.preventDefault()

        val changedTouches = event.changedTouches
        for (i in 0 until changedTouches.length) {
            val touch = changedTouches.item(i) ?: continue
            val id = touch.identifier

            val position =
                if (canvas != null) {
                    val rect = canvas!!.getBoundingClientRect()
                    Vector2(
                        (touch.clientX - rect.left).toFloat(),
                        (touch.clientY - rect.top).toFloat(),
                    )
                } else {
                    Vector2(touch.clientX.toFloat(), touch.clientY.toFloat())
                }

            val previousTouch = previousTouches[id]
            val deltaPosition = previousTouch?.let { position - it.position } ?: Vector2.ZERO.copy()

            touches[id] = TouchData(id, position, phase, deltaPosition)

            if (phase == TouchPhase.ENDED || phase == TouchPhase.CANCELLED) {
                previousTouches.remove(id)
            } else {
                previousTouches[id] = TouchData(id, position, phase)
            }
        }
    }

    fun update() {
        // Clean up ended touches
        touches.values.removeAll { it.phase == TouchPhase.ENDED || it.phase == TouchPhase.CANCELLED }

        // Update stationary touches
        touches.forEach { (id, touch) ->
            if (touch.phase == TouchPhase.MOVED) {
                touches[id] = touch.copy(phase = TouchPhase.STATIONARY)
            }
        }
    }

    fun getTouchCount() = touches.size

    fun getTouch(index: Int): TouchData? = touches.values.elementAtOrNull(index)

    fun getTouchById(id: Int) = touches[id]

    fun getTouches() = touches.values.toList()

    fun isTouching() = touches.isNotEmpty()

    fun getPrimaryTouch() = touches.values.firstOrNull()
}
