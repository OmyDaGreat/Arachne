package xyz.malefic.arachne.examples

import org.w3c.dom.HTMLCanvasElement
import xyz.malefic.arachne.core.*
import xyz.malefic.arachne.debug.GameDebug
import xyz.malefic.arachne.graphics.*
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2

/**
 * A minimal example game demonstrating the core features of Arachne.
 */
class MinimalGame(
    canvas: HTMLCanvasElement,
) : Game(canvas) {
    private val world = World()
    private lateinit var player: Entity

    override fun create() {
        GameDebug.showFPS = true
        camera.position = Vector2(400f, 300f)

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
        camera.applyTransform(ctx)
        world.getEntitiesWith(Transform::class, Sprite::class).forEach { entity ->
            val t = entity.get<Transform>()!!
            val s = entity.get<Sprite>()!!
            ctx.fillStyle = "#ff6b6b"
            ctx.fillRect(
                (t.position.x - s.width / 2).toDouble(),
                (t.position.y - s.height / 2).toDouble(),
                s.width.toDouble(),
                s.height.toDouble(),
            )
        }
        camera.resetTransform(ctx)
        GameDebug.drawDebugInfo(ctx, getFPS(), world)
    }
}
