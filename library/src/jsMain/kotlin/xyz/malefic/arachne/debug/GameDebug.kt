package xyz.malefic.arachne.debug

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.core.World
import xyz.malefic.arachne.graphics.Camera
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.physics.Collider

object GameDebug {
    var showFPS = false
    var showEntityCount = false
    var showColliders = false
    var showGrid = false
    var showVelocity = false

    fun drawDebugInfo(
        ctx: CanvasRenderingContext2D,
        fps: Int,
        world: World,
        x: Double = 10.0,
        y: Double = 20.0,
    ) {
        ctx.save()
        ctx.fillStyle = "white"
        ctx.strokeStyle = "black"
        ctx.lineWidth = 2.0
        ctx.font = "16px monospace"

        var offsetY = y

        if (showFPS) {
            val text = "FPS: $fps"
            ctx.strokeText(text, x, offsetY)
            ctx.fillText(text, x, offsetY)
            offsetY += 20
        }

        if (showEntityCount) {
            val text = "Entities: ${world.entityCount()}"
            ctx.strokeText(text, x, offsetY)
            ctx.fillText(text, x, offsetY)
            offsetY += 20
        }

        ctx.restore()
    }

    fun drawColliders(
        ctx: CanvasRenderingContext2D,
        world: World,
        camera: Camera,
    ) {
        if (!showColliders) return

        camera.applyTransform(ctx)
        ctx.save()

        world.getEntities().forEach { entity ->
            val collider = entity.get<Collider>() ?: return@forEach
            val transform = entity.get<Transform>() ?: return@forEach

            ctx.strokeStyle = if (collider.isTrigger) "rgba(255, 255, 0, 0.5)" else "rgba(0, 255, 0, 0.5)"
            ctx.lineWidth = 2.0 / camera.zoom

            when (collider) {
                is xyz.malefic.arachne.physics.CircleCollider -> {
                    val center = transform.position + collider.offset
                    ctx.beginPath()
                    ctx.arc(
                        center.x.toDouble(),
                        center.y.toDouble(),
                        collider.radius.toDouble(),
                        0.0,
                        2 * kotlin.math.PI,
                    )
                    ctx.stroke()
                }

                is xyz.malefic.arachne.physics.BoxCollider -> {
                    val rect = collider.toRectangle(transform)
                    ctx.strokeRect(
                        rect.x.toDouble(),
                        rect.y.toDouble(),
                        rect.width.toDouble(),
                        rect.height.toDouble(),
                    )
                }

                else -> {}
            }
        }

        ctx.restore()
        camera.resetTransform(ctx)
    }

    fun drawVelocities(
        ctx: CanvasRenderingContext2D,
        world: World,
        camera: Camera,
    ) {
        if (!showVelocity) return

        camera.applyTransform(ctx)
        ctx.save()

        world.getEntities().forEach { entity ->
            val rb = entity.get<xyz.malefic.arachne.physics.RigidBody>() ?: return@forEach
            val transform = entity.get<Transform>() ?: return@forEach

            if (rb.velocity.magnitude() < 0.1f) return@forEach

            ctx.strokeStyle = "rgba(255, 0, 0, 0.8)"
            ctx.lineWidth = 2.0 / camera.zoom

            ctx.beginPath()
            ctx.moveTo(transform.position.x.toDouble(), transform.position.y.toDouble())
            val end = transform.position + rb.velocity * 0.1f
            ctx.lineTo(end.x.toDouble(), end.y.toDouble())
            ctx.stroke()

            // Arrow head
            val angle = rb.velocity.angle()
            val arrowSize = 5f / camera.zoom
            ctx.beginPath()
            ctx.moveTo(end.x.toDouble(), end.y.toDouble())
            val left =
                end +
                    xyz.malefic.arachne.math.Vector2
                        .fromAngle(angle + 2.8f, arrowSize)
            val right =
                end +
                    xyz.malefic.arachne.math.Vector2
                        .fromAngle(angle - 2.8f, arrowSize)
            ctx.lineTo(left.x.toDouble(), left.y.toDouble())
            ctx.moveTo(end.x.toDouble(), end.y.toDouble())
            ctx.lineTo(right.x.toDouble(), right.y.toDouble())
            ctx.stroke()
        }

        ctx.restore()
        camera.resetTransform(ctx)
    }

    fun drawGrid(
        ctx: CanvasRenderingContext2D,
        camera: Camera,
        gridSize: Float = 32f,
    ) {
        if (!showGrid) return

        ctx.save()
        camera.applyTransform(ctx)

        val bounds = camera.bounds
        val startX = (bounds.x / gridSize).toInt() * gridSize
        val startY = (bounds.y / gridSize).toInt() * gridSize
        val endX = startX + bounds.width + gridSize
        val endY = startY + bounds.height + gridSize

        ctx.strokeStyle = "rgba(255, 255, 255, 0.1)"
        ctx.lineWidth = 1.0 / camera.zoom

        // Draw vertical lines
        var x = startX
        while (x <= endX) {
            ctx.beginPath()
            ctx.moveTo(x.toDouble(), startY.toDouble())
            ctx.lineTo(x.toDouble(), endY.toDouble())
            ctx.stroke()
            x += gridSize
        }

        // Draw horizontal lines
        var y = startY
        while (y <= endY) {
            ctx.beginPath()
            ctx.moveTo(startX.toDouble(), y.toDouble())
            ctx.lineTo(endX.toDouble(), y.toDouble())
            ctx.stroke()
            y += gridSize
        }

        camera.resetTransform(ctx)
        ctx.restore()
    }
}
