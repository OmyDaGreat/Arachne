package xyz.malefic.arachne.core

import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import xyz.malefic.arachne.graphics.Camera
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.scene.SceneManager

abstract class Game(
    val canvas: HTMLCanvasElement,
) {
    val ctx: CanvasRenderingContext2D = canvas.getContext("2d") as CanvasRenderingContext2D
    val camera = Camera()
    val sceneManager = SceneManager()
    protected val clock = GameClock()

    init {
        camera.setSize(canvas.width.toFloat(), canvas.height.toFloat())
        Input.initialize(canvas)
    }

    abstract fun create()

    open fun update(deltaTime: Double) {
        Input.update()
        sceneManager.update(deltaTime)
    }

    open fun render() {
        ctx.fillStyle = "#000000"
        ctx.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())
    }

    fun start(): () -> Unit {
        create()

        return createGameLoop(
            canvas = canvas,
            targetFPS = 60,
            fixedTimestep = false,
            onUpdate = { deltaTime ->
                clock.update(window.performance.now())
                update(deltaTime)
            },
            onRender = {
                render()
            },
        )
    }

    fun getFPS() = clock.fps

    fun getDeltaTime() = clock.deltaTime

    fun getElapsedTime() = clock.elapsedTime
}
