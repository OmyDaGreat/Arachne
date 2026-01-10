package xyz.malefic.arachne.physics

import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.math.Vector2

data class Collision(
    val entityA: Entity,
    val entityB: Entity,
    val colliderA: Collider,
    val colliderB: Collider,
    val normal: Vector2,
    val penetration: Float,
    val point: Vector2,
) {
    fun getOtherEntity(entity: Entity) = if (entity == entityA) entityB else entityA

    fun getOtherCollider(collider: Collider) = if (collider == colliderA) colliderB else colliderA
}

data class RaycastHit(
    val entity: Entity,
    val collider: Collider,
    val point: Vector2,
    val normal: Vector2,
    val distance: Float,
)

data class CollisionInfo(
    val collision: Collision,
    val isNew: Boolean,
)
