package xyz.malefic.arachne.physics

import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.AABB
import xyz.malefic.arachne.math.Rectangle
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2

sealed class Collider : Component {
    abstract val offset: Vector2
    abstract val isTrigger: Boolean
    abstract val layer: Int
    abstract val mask: Int

    abstract fun getBounds(transform: Transform): AABB

    fun overlaps(
        other: Collider,
        transformA: Transform,
        transformB: Transform,
    ): Boolean {
        val boundsA = getBounds(transformA)
        val boundsB = other.getBounds(transformB)
        return boundsA.intersects(boundsB)
    }
}

data class CircleCollider(
    val radius: Float,
    override val offset: Vector2 = Vector2.ZERO.copy(),
    override val isTrigger: Boolean = false,
    override val layer: Int = 1,
    override val mask: Int = -1,
) : Collider() {
    override fun getBounds(transform: Transform): AABB {
        val center = transform.position + offset
        return AABB.fromCenterAndSize(center, Vector2(radius * 2, radius * 2))
    }
}

data class BoxCollider(
    val size: Vector2,
    override val offset: Vector2 = Vector2.ZERO.copy(),
    override val isTrigger: Boolean = false,
    override val layer: Int = 1,
    override val mask: Int = -1,
) : Collider() {
    override fun getBounds(transform: Transform): AABB {
        val center = transform.position + offset
        val halfSize = Vector2(size.x * transform.scale.x / 2, size.y * transform.scale.y / 2)
        return AABB(
            Vector2(center.x - halfSize.x, center.y - halfSize.y),
            Vector2(center.x + halfSize.x, center.y + halfSize.y),
        )
    }

    fun toRectangle(transform: Transform): Rectangle {
        val center = transform.position + offset
        val scaledSize = Vector2(size.x * transform.scale.x, size.y * transform.scale.y)
        return Rectangle(
            center.x - scaledSize.x / 2,
            center.y - scaledSize.y / 2,
            scaledSize.x,
            scaledSize.y,
        )
    }
}

data class CapsuleCollider(
    val radius: Float,
    val height: Float,
    override val offset: Vector2 = Vector2.ZERO.copy(),
    override val isTrigger: Boolean = false,
    override val layer: Int = 1,
    override val mask: Int = -1,
) : Collider() {
    override fun getBounds(transform: Transform): AABB {
        val center = transform.position + offset
        val halfHeight = height / 2
        return AABB(
            Vector2(center.x - radius, center.y - halfHeight - radius),
            Vector2(center.x + radius, center.y + halfHeight + radius),
        )
    }
}
