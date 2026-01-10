package xyz.malefic.arachne.graphics

import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.Vector2

data class Sprite(
    var texture: String,
    var width: Float = 32f,
    var height: Float = 32f,
    var offset: Vector2 = Vector2.ZERO.copy(),
    var flipX: Boolean = false,
    var flipY: Boolean = false,
    var opacity: Float = 1f,
    var tint: String? = null,
    var sourceRect: SpriteRect? = null,
) : Component

data class SpriteRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int,
)
