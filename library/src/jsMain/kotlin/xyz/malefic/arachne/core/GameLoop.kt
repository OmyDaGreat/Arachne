package xyz.malefic.arachne.core

import androidx.compose.runtime.*
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.min

class GameClock {
    var deltaTime: Double = 0.0
        private set
    var elapsedTime: Double = 0.0
        private set
    var fps: Int = 0
        private set

    private var lastFrameTime: Double = 0.0
    private var frameCount = 0
    private var fpsUpdateTime = 0.0

    fun update(currentTime: Double) {
        deltaTime = if (lastFrameTime > 0) (currentTime - lastFrameTime) / 1000.0 else 0.0
        lastFrameTime = currentTime
        elapsedTime += deltaTime

        // Update FPS counter
        frameCount++
        fpsUpdateTime += deltaTime
        if (fpsUpdateTime >= 1.0) {
            fps = frameCount
            frameCount = 0
            fpsUpdateTime = 0.0
        }
    }

    fun reset() {
        deltaTime = 0.0
        elapsedTime = 0.0
        fps = 0
        lastFrameTime = 0.0
        frameCount = 0
        fpsUpdateTime = 0.0
    }
}

@Composable
fun GameLoop(
    canvas: HTMLCanvasElement,
    targetFPS: Int = 60,
    fixedTimestep: Boolean = false,
    onUpdate: (deltaTime: Double) -> Unit,
    onRender: (ctx: CanvasRenderingContext2D) -> Unit,
) {
    val clock = remember { GameClock() }
    val ctx = remember { canvas.getContext("2d") as CanvasRenderingContext2D }
    val fixedDeltaTime = 1.0 / targetFPS
    var accumulator by remember { mutableStateOf(0.0) }

    DisposableEffect(Unit) {
        var requestId: Int? = null
        var running = true

        fun gameLoop(currentTime: Double) {
            if (!running) return

            clock.update(currentTime)

            if (fixedTimestep) {
                accumulator += clock.deltaTime
                while (accumulator >= fixedDeltaTime) {
                    onUpdate(fixedDeltaTime)
                    accumulator -= fixedDeltaTime
                }
            } else {
                val cappedDelta = min(clock.deltaTime, 0.1)
                onUpdate(cappedDelta)
            }

            onRender(ctx)

            requestId =
                window.requestAnimationFrame { time ->
                    gameLoop(time)
                }
        }

        requestId =
            window.requestAnimationFrame { time ->
                gameLoop(time)
            }

        onDispose {
            running = false
            requestId.let { window.cancelAnimationFrame(it) }
        }
    }
}

fun createGameLoop(
    canvas: HTMLCanvasElement,
    targetFPS: Int = 60,
    fixedTimestep: Boolean = false,
    onUpdate: (deltaTime: Double) -> Unit,
    onRender: (ctx: CanvasRenderingContext2D) -> Unit,
): () -> Unit {
    val clock = GameClock()
    val ctx = canvas.getContext("2d") as CanvasRenderingContext2D
    val fixedDeltaTime = 1.0 / targetFPS
    var accumulator = 0.0
    var requestId: Int?
    var running = true

    fun gameLoop(currentTime: Double) {
        if (!running) return

        clock.update(currentTime)

        if (fixedTimestep) {
            accumulator += clock.deltaTime
            while (accumulator >= fixedDeltaTime) {
                onUpdate(fixedDeltaTime)
                accumulator -= fixedDeltaTime
            }
        } else {
            val cappedDelta = min(clock.deltaTime, 0.1)
            onUpdate(cappedDelta)
        }

        onRender(ctx)

        requestId =
            window.requestAnimationFrame { time ->
                gameLoop(time)
            }
    }

    requestId =
        window.requestAnimationFrame { time ->
            gameLoop(time)
        }

    return {
        running = false
        requestId.let { window.cancelAnimationFrame(it) }
    }
}
