package xyz.malefic.arachne.graphics

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.assets.AssetManager
import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.core.System
import xyz.malefic.arachne.math.Transform

class RenderSystem(
    private val ctx: CanvasRenderingContext2D,
    private val camera: Camera,
) : System {
    var backgroundColor: String = "#000000"
    var clearScreen: Boolean = true

    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        if (clearScreen) {
            ctx.fillStyle = backgroundColor
            ctx.fillRect(0.0, 0.0, camera.width.toDouble(), camera.height.toDouble())
        }

        camera.applyTransform(ctx)

        // Sort entities by layer and z-index if available
        val sortedEntities =
            entities
                .filter { it.active }
                .sortedWith(
                    compareBy(
                        { it.layer },
                        { it.get<Transform>()?.position?.y ?: 0f },
                    ),
                )

        sortedEntities.forEach { entity ->
            val transform = entity.get<Transform>() ?: return@forEach
            val sprite = entity.get<Sprite>() ?: return@forEach

            renderSprite(sprite, transform)
        }

        camera.resetTransform(ctx)
    }

    private fun renderSprite(
        sprite: Sprite,
        transform: Transform,
    ) {
        val texture = AssetManager.getTexture(sprite.texture) ?: return

        ctx.save()

        // Apply transform
        ctx.translate(transform.position.x.toDouble(), transform.position.y.toDouble())
        ctx.rotate(transform.rotation.toDouble())

        // Apply scale and flip
        val scaleX = transform.scale.x * if (sprite.flipX) -1 else 1
        val scaleY = transform.scale.y * if (sprite.flipY) -1 else 1
        ctx.scale(scaleX.toDouble(), scaleY.toDouble())

        // Apply opacity
        ctx.globalAlpha = sprite.opacity.toDouble()

        // Calculate drawing position (centered on sprite)
        val drawX = (-sprite.width / 2 + sprite.offset.x).toDouble()
        val drawY = (-sprite.height / 2 + sprite.offset.y).toDouble()

        // Draw sprite
        if (sprite.sourceRect != null) {
            val src = sprite.sourceRect!!
            ctx.drawImage(
                texture,
                src.x.toDouble(),
                src.y.toDouble(),
                src.width.toDouble(),
                src.height.toDouble(),
                drawX,
                drawY,
                sprite.width.toDouble(),
                sprite.height.toDouble(),
            )
        } else {
            ctx.drawImage(
                texture,
                drawX,
                drawY,
                sprite.width.toDouble(),
                sprite.height.toDouble(),
            )
        }

        ctx.restore()
    }
}
