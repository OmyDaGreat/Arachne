package xyz.malefic.arachne.core

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.toAttrs
import kotlinx.browser.window
import org.jetbrains.compose.web.css.px
import org.jetbrains.compose.web.dom.Canvas

/**
 * A composable function that integrates an Arachne game into Kobweb as a UI component.
 *
 * This allows you to embed games directly in your Kobweb pages inside Box, Column, or any other layout.
 *
 * Simple usage:
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     GameCanvas(
 *         width = 800,
 *         height = 600,
 *         gameFactory = { MyGame() }
 *     )
 * }
 * ```
 *
 * With game instance access:
 * ```kotlin
 * var myGame by remember { mutableStateOf<MyGame?>(null) }
 * Column(Modifier.fillMaxSize()) {
 *     myGame?.let { Text("FPS: ${it.getFPS()}") }
 *     GameCanvas(
 *         width = 800,
 *         height = 600,
 *         onGameCreated = { game -> myGame = game },
 *         gameFactory = { MyGame() }
 *     )
 * }
 * ```
 *
 * @param modifier Modifier for styling the canvas (size, borders, etc.)
 * @param width Canvas width in pixels
 * @param height Canvas height in pixels
 * @param targetFPS Target frames per second (default: 60)
 * @param fixedTimestep Whether to use fixed timestep for physics (default: false)
 * @param onGameCreated Optional callback when the game instance is created
 * @param gameFactory Factory function that creates your Game instance (canvas is automatically managed)
 */
@Composable
fun <T : Game> GameCanvas(
    modifier: Modifier = Modifier,
    width: Int = 800,
    height: Int = 600,
    targetFPS: Int = 60,
    fixedTimestep: Boolean = false,
    onGameCreated: (T) -> Unit = {},
    gameFactory: () -> T,
) {
    var game by remember { mutableStateOf<T?>(null) }

    Canvas(
        attrs =
            modifier
                .width(width.px)
                .height(height.px)
                .toAttrs {
                    attr("width", width.toString())
                    attr("height", height.toString())
                    ref { canvas ->
                        // This callback is called when the canvas element is attached to the DOM
                        if (game == null) {
                            val gameInstance = gameFactory()
                            gameInstance.initializeCanvas(canvas)
                            game = gameInstance
                            gameInstance.create()
                            onGameCreated(gameInstance)
                        }
                        onDispose {
                            // Cleanup will happen in DisposableEffect below
                        }
                    }
                },
    )

    DisposableEffect(game) {
        val gameInstance = game
        if (gameInstance != null) {
            var requestId: Int? = null
            var running = true

            fun gameLoop(currentTime: Double) {
                if (!running) return

                gameInstance.clock.update(currentTime)

                val deltaTime =
                    if (fixedTimestep) {
                        1.0 / targetFPS
                    } else {
                        kotlin.math.min(gameInstance.clock.deltaTime, 0.1)
                    }

                gameInstance.update(deltaTime)
                gameInstance.render()

                requestId = window.requestAnimationFrame { time -> gameLoop(time) }
            }

            requestId = window.requestAnimationFrame { time -> gameLoop(time) }

            onDispose {
                running = false
                requestId.let { window.cancelAnimationFrame(it) }
            }
        } else {
            onDispose {}
        }
    }
}
