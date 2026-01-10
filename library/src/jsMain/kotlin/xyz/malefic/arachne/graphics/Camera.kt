package xyz.malefic.arachne.graphics

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.math.Rectangle
import xyz.malefic.arachne.math.Vector2
import kotlin.random.Random

class Camera(
    var position: Vector2 = Vector2.ZERO.copy(),
    var zoom: Float = 1f,
    var rotation: Float = 0f,
) {
    var width: Float = 800f
    var height: Float = 600f

    // Camera shake
    private var shakeIntensity = 0f
    private var shakeDuration = 0f
    private var shakeTimer = 0f
    private val shakeOffset = Vector2.ZERO.copy()

    val bounds: Rectangle
        get() {
            val w = width / zoom
            val h = height / zoom
            return Rectangle(
                position.x - w / 2,
                position.y - h / 2,
                w,
                h,
            )
        }

    fun setSize(
        w: Float,
        h: Float,
    ) {
        width = w
        height = h
    }

    fun screenToWorld(screenPos: Vector2): Vector2 {
        val x = (screenPos.x - width / 2) / zoom + position.x
        val y = (screenPos.y - height / 2) / zoom + position.y
        return Vector2(x, y)
    }

    fun worldToScreen(worldPos: Vector2): Vector2 {
        val x = (worldPos.x - position.x) * zoom + width / 2
        val y = (worldPos.y - position.y) * zoom + height / 2
        return Vector2(x, y)
    }

    fun follow(
        target: Vector2,
        lerp: Float = 1f,
    ) {
        position =
            if (lerp >= 1f) {
                target.copy()
            } else {
                position.lerp(target, lerp)
            }
    }

    fun shake(
        intensity: Float,
        duration: Float,
    ) {
        shakeIntensity = intensity
        shakeDuration = duration
        shakeTimer = 0f
    }

    fun update(deltaTime: Float) {
        if (shakeTimer < shakeDuration) {
            shakeTimer += deltaTime
            val t = shakeTimer / shakeDuration
            val currentIntensity = shakeIntensity * (1f - t)

            shakeOffset.x = (Random.nextFloat() - 0.5f) * 2f * currentIntensity
            shakeOffset.y = (Random.nextFloat() - 0.5f) * 2f * currentIntensity
        } else {
            shakeOffset.x = 0f
            shakeOffset.y = 0f
        }
    }

    fun applyTransform(ctx: CanvasRenderingContext2D) {
        ctx.save()
        ctx.translate(width / 2.0, height / 2.0)
        ctx.scale(zoom.toDouble(), zoom.toDouble())
        ctx.rotate(rotation.toDouble())
        ctx.translate(
            -(position.x + shakeOffset.x).toDouble(),
            -(position.y + shakeOffset.y).toDouble(),
        )
    }

    fun resetTransform(ctx: CanvasRenderingContext2D) {
        ctx.restore()
    }

    fun contains(worldPos: Vector2) = bounds.contains(worldPos)

    fun intersects(rect: Rectangle) = bounds.intersects(rect)
}
