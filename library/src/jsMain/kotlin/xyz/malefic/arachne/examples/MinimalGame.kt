package xyz.malefic.arachne.examples

import xyz.malefic.arachne.core.*
import xyz.malefic.arachne.debug.GameDebug
import xyz.malefic.arachne.graphics.*
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2

/**
 * A minimal example game demonstrating the core features of Arachne.
 * Shows basic player movement and automated rendering with RenderSystem.
 */
class MinimalGame : Game() {
    private val world = World()
    private lateinit var player: Entity

    override fun create() {
        GameDebug.showFPS = true
        camera.position = Vector2(400f, 300f)

        // Setup automated rendering system
        world.addSystem(RenderSystem(ctx, camera))

        // Create player entity
        player =
            Entity().apply {
                tag = "player"
                add(Transform(position = Vector2(400f, 300f)))
                add(Sprite(texture = "player.png", width = 64f, height = 64f))
            }
        world.addEntity(player)
    }

    override fun update(deltaTime: Double) {
        super.update(deltaTime)
        val transform = player.get<Transform>()
        if (transform != null) {
            val speed = 200f
            if (Input.isKeyDown("ArrowLeft")) transform.position.x -= speed * deltaTime.toFloat()
            if (Input.isKeyDown("ArrowRight")) transform.position.x += speed * deltaTime.toFloat()
            camera.follow(transform.position, lerp = 0.1f)
        }
        world.update(deltaTime)
    }

    override fun render() {
        super.render()
        // RenderSystem handles sprite rendering automatically
        GameDebug.drawDebugInfo(ctx, getFPS(), world)
    }
}
