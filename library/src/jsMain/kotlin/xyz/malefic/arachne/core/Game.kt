package xyz.malefic.arachne.core

import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import xyz.malefic.arachne.graphics.Camera
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.scene.SceneManager

abstract class Game {
    lateinit var canvas: HTMLCanvasElement
        internal set
    lateinit var ctx: CanvasRenderingContext2D
        internal set
    val camera = Camera()
    val sceneManager = SceneManager()
    val clock = GameClock()

    internal fun initializeCanvas(canvasElement: HTMLCanvasElement) {
        canvas = canvasElement
        ctx = canvas.getContext("2d") as CanvasRenderingContext2D
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
