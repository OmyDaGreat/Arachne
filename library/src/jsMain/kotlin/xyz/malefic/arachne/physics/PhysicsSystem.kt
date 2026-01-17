package xyz.malefic.arachne.physics

import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.core.System
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class PhysicsSystem(
    var gravity: Vector2 = Vector2(0f, 980f),
    var iterations: Int = 4,
) : System {
    private val collisionPairs = mutableSetOf<Pair<Entity, Entity>>()
    private val previousCollisions = mutableMapOf<Pair<Entity, Entity>, Collision>()
    val onCollisionEnter = mutableListOf<(Collision) -> Unit>()
    val onCollisionStay = mutableListOf<(Collision) -> Unit>()
    val onCollisionExit = mutableListOf<(Entity, Entity) -> Unit>()

    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        val dt = deltaTime.toFloat()

        // Apply forces
        applyForces(entities, dt)

        // Update velocities and positions
        updatePhysics(entities, dt)

        // Detect and resolve collisions
        val currentCollisions = detectCollisions(entities)
        resolveCollisions(currentCollisions, entities)

        // Handle collision callbacks
        handleCollisionCallbacks(currentCollisions)
    }

    private fun applyForces(
        entities: List<Entity>,
        deltaTime: Float,
    ) {
        entities.forEach { entity ->
            val rb = entity.get<RigidBody>() ?: return@forEach
            if (rb.isKinematic || rb.isStatic) return@forEach

            // Apply gravity
            if (rb.useGravity) {
                rb.acceleration += gravity * rb.gravityScale
            }

            // Apply drag
            rb.velocity *= 1f - rb.drag
            rb.angularVelocity *= 1f - rb.angularDrag
        }
    }

    private fun updatePhysics(
        entities: List<Entity>,
        deltaTime: Float,
    ) {
        entities.forEach { entity ->
            val rb = entity.get<RigidBody>() ?: return@forEach
            val transform = entity.get<Transform>() ?: return@forEach

            if (rb.isStatic) return@forEach

            // Update velocity
            rb.velocity += (rb.acceleration * deltaTime)

            // Update position
            if (!rb.isKinematic) {
                transform.position += (rb.velocity * deltaTime)
                transform.rotation += rb.angularVelocity * deltaTime
            }

            // Reset acceleration
            rb.acceleration = Vector2.ZERO.copy()
        }
    }

    private fun detectCollisions(entities: List<Entity>): List<Collision> {
        val collisions = mutableListOf<Collision>()
        val physicsEntities = entities.filter { it.has<Collider>() && it.has<Transform>() }

        for (i in physicsEntities.indices) {
            for (j in i + 1 until physicsEntities.size) {
                val entityA = physicsEntities[i]
                val entityB = physicsEntities[j]

                val colliderA = entityA.get<Collider>()!!
                val colliderB = entityB.get<Collider>()!!
                val transformA = entityA.get<Transform>()!!
                val transformB = entityB.get<Transform>()!!

                // Layer mask check
                if ((colliderA.layer and colliderB.mask) == 0 ||
                    (colliderB.layer and colliderA.mask) == 0
                ) {
                    continue
                }

                // Broad phase - AABB check
                if (!colliderA.getBounds(transformA).intersects(colliderB.getBounds(transformB))) {
                    continue
                }

                // Narrow phase - detailed collision
                val collision = checkCollision(entityA, entityB, colliderA, colliderB, transformA, transformB)
                if (collision != null) {
                    collisions.add(collision)
                }
            }
        }

        return collisions
    }

    private fun checkCollision(
        entityA: Entity,
        entityB: Entity,
        colliderA: Collider,
        colliderB: Collider,
        transformA: Transform,
        transformB: Transform,
    ): Collision? =
        when (colliderA) {
            is CircleCollider if colliderB is CircleCollider -> {
                checkCircleCircle(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }

            is BoxCollider if colliderB is BoxCollider -> {
                checkBoxBox(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }

            is CircleCollider if colliderB is BoxCollider -> {
                checkCircleBox(entityA, entityB, colliderA, colliderB, transformA, transformB)
            }

            is BoxCollider if colliderB is CircleCollider -> {
                checkCircleBox(entityB, entityA, colliderB, colliderA, transformB, transformA)?.let {
                    it.copy(
                        entityA = entityA,
                        entityB = entityB,
                        colliderA = colliderA,
                        colliderB = colliderB,
                        normal = -it.normal,
                    )
                }
            }

            else -> {
                null
            }
        }

    private fun checkCircleCircle(
        entityA: Entity,
        entityB: Entity,
        colliderA: CircleCollider,
        colliderB: CircleCollider,
        transformA: Transform,
        transformB: Transform,
    ): Collision? {
        val centerA = transformA.position + colliderA.offset
        val centerB = transformB.position + colliderB.offset
        val distance = centerA.distance(centerB)
        val radiusSum = colliderA.radius + colliderB.radius

        if (distance < radiusSum) {
            val normal = (centerB - centerA).normalized()
            val penetration = radiusSum - distance
            val point = centerA + (normal * colliderA.radius)

            return Collision(entityA, entityB, colliderA, colliderB, normal, penetration, point)
        }

        return null
    }

    private fun checkBoxBox(
        entityA: Entity,
        entityB: Entity,
        colliderA: BoxCollider,
        colliderB: BoxCollider,
        transformA: Transform,
        transformB: Transform,
    ): Collision? {
        val rectA = colliderA.toRectangle(transformA)
        val rectB = colliderB.toRectangle(transformB)

        if (!rectA.intersects(rectB)) return null

        val overlapX = min(rectA.right - rectB.left, rectB.right - rectA.left)
        val overlapY = min(rectA.bottom - rectB.top, rectB.bottom - rectA.top)

        val normal: Vector2
        val penetration: Float

        if (overlapX < overlapY) {
            penetration = overlapX
            normal = if (rectA.centerX < rectB.centerX) Vector2(-1f, 0f) else Vector2(1f, 0f)
        } else {
            penetration = overlapY
            normal = if (rectA.centerY < rectB.centerY) Vector2(0f, -1f) else Vector2(0f, 1f)
        }

        val point =
            Vector2(
                (rectA.centerX + rectB.centerX) / 2,
                (rectA.centerY + rectB.centerY) / 2,
            )

        return Collision(entityA, entityB, colliderA, colliderB, normal, penetration, point)
    }

    private fun checkCircleBox(
        entityA: Entity,
        entityB: Entity,
        colliderA: CircleCollider,
        colliderB: BoxCollider,
        transformA: Transform,
        transformB: Transform,
    ): Collision? {
        val circleCenter = transformA.position + colliderA.offset
        val rect = colliderB.toRectangle(transformB)

        val closestX = circleCenter.x.coerceIn(rect.left, rect.right)
        val closestY = circleCenter.y.coerceIn(rect.top, rect.bottom)
        val closest = Vector2(closestX, closestY)

        val distance = circleCenter.distance(closest)

        if (distance < colliderA.radius) {
            val normal = (circleCenter - closest).normalized()
            val penetration = colliderA.radius - distance

            return Collision(entityA, entityB, colliderA, colliderB, normal, penetration, closest)
        }

        return null
    }

    private fun resolveCollisions(
        collisions: List<Collision>,
        entities: List<Entity>,
    ) {
        repeat(iterations) {
            collisions.forEach { collision ->
                if (collision.colliderA.isTrigger || collision.colliderB.isTrigger) {
                    return@forEach
                }

                val rbA = collision.entityA.get<RigidBody>()
                val rbB = collision.entityB.get<RigidBody>()
                val transformA = collision.entityA.get<Transform>()!!
                val transformB = collision.entityB.get<Transform>()!!

                // Position correction
                val correction = collision.normal * (collision.penetration / iterations)

                when {
                    rbA?.isStatic == true && rbB?.isStatic == true -> {
                        return@forEach
                    }

                    rbA?.isStatic == true -> {
                        transformB.position += correction
                    }

                    rbB?.isStatic == true -> {
                        transformA.position -= correction
                    }

                    rbA != null && rbB != null -> {
                        val totalMass = rbA.mass + rbB.mass
                        transformA.position -= (correction * (rbB.mass / totalMass))
                        transformB.position += (correction * (rbA.mass / totalMass))
                    }
                }

                // Velocity resolution
                if (rbA != null && rbB != null && !rbA.isStatic && !rbB.isStatic) {
                    val relativeVelocity = rbB.velocity - rbA.velocity
                    val velocityAlongNormal = relativeVelocity.dot(collision.normal)

                    if (velocityAlongNormal < 0) return@forEach

                    val restitution = 0.3f
                    val impulseScalar = -(1 + restitution) * velocityAlongNormal
                    val impulse = collision.normal * impulseScalar

                    if (!rbA.isKinematic) {
                        rbA.velocity = rbA.velocity - (impulse / rbA.mass)
                    }
                    if (!rbB.isKinematic) {
                        rbB.velocity = rbB.velocity + (impulse / rbB.mass)
                    }
                }
            }
        }
    }

    private fun handleCollisionCallbacks(currentCollisions: List<Collision>) {
        val currentPairs = currentCollisions.map { Pair(it.entityA, it.entityB) }.toSet()

        // Collision Enter
        currentCollisions.forEach { collision ->
            val pair = Pair(collision.entityA, collision.entityB)
            if (!previousCollisions.containsKey(pair)) {
                onCollisionEnter.forEach { it(collision) }
            } else {
                onCollisionStay.forEach { it(collision) }
            }
        }

        // Collision Exit
        previousCollisions.keys.forEach { pair ->
            if (!currentPairs.contains(pair)) {
                onCollisionExit.forEach { it(pair.first, pair.second) }
            }
        }

        // Update previous collisions
        previousCollisions.clear()
        currentCollisions.forEach { collision ->
            previousCollisions[Pair(collision.entityA, collision.entityB)] = collision
        }
    }

    fun raycast(
        origin: Vector2,
        direction: Vector2,
        maxDistance: Float,
        entities: List<Entity>,
    ): RaycastHit? {
        val normalizedDir = direction.normalized()
        var closestHit: RaycastHit? = null
        var closestDistance = maxDistance

        entities.forEach { entity ->
            val collider = entity.get<Collider>() ?: return@forEach
            val transform = entity.get<Transform>() ?: return@forEach

            val hit = raycastCollider(origin, normalizedDir, maxDistance, collider, transform, entity)
            if (hit != null && hit.distance < closestDistance) {
                closestHit = hit
                closestDistance = hit.distance
            }
        }

        return closestHit
    }

    private fun raycastCollider(
        origin: Vector2,
        direction: Vector2,
        maxDistance: Float,
        collider: Collider,
        transform: Transform,
        entity: Entity,
    ): RaycastHit? =
        when (collider) {
            is CircleCollider -> raycastCircle(origin, direction, maxDistance, collider, transform, entity)
            is BoxCollider -> raycastBox(origin, direction, maxDistance, collider, transform, entity)
            else -> null
        }

    private fun raycastCircle(
        origin: Vector2,
        direction: Vector2,
        maxDistance: Float,
        collider: CircleCollider,
        transform: Transform,
        entity: Entity,
    ): RaycastHit? {
        val center = transform.position + collider.offset
        val oc = origin - center
        val a = direction.dot(direction)
        val b = 2.0f * oc.dot(direction)
        val c = oc.dot(oc) - collider.radius * collider.radius
        val discriminant = b * b - 4 * a * c

        if (discriminant < 0) return null

        val t = (-b - sqrt(discriminant)) / (2 * a)
        if (t !in 0.0..maxDistance.toDouble()) return null

        val point = origin + (direction * t)
        val normal = (point - center).normalized()

        return RaycastHit(entity, collider, point, normal, t)
    }

    private fun raycastBox(
        origin: Vector2,
        direction: Vector2,
        maxDistance: Float,
        collider: BoxCollider,
        transform: Transform,
        entity: Entity,
    ): RaycastHit? {
        val rect = collider.toRectangle(transform)
        val invDir = Vector2(1f / direction.x, 1f / direction.y)

        val t1 = (rect.left - origin.x) * invDir.x
        val t2 = (rect.right - origin.x) * invDir.x
        val t3 = (rect.top - origin.y) * invDir.y
        val t4 = (rect.bottom - origin.y) * invDir.y

        val tmin = maxOf(minOf(t1, t2), minOf(t3, t4))
        val tmax = minOf(maxOf(t1, t2), maxOf(t3, t4))

        if (tmax < 0 || tmin > tmax || tmin > maxDistance) return null

        val t = if (tmin >= 0) tmin else tmax
        val point = origin + (direction * t)

        val normal =
            when {
                abs(point.x - rect.left) < 0.01f -> Vector2(-1f, 0f)
                abs(point.x - rect.right) < 0.01f -> Vector2(1f, 0f)
                abs(point.y - rect.top) < 0.01f -> Vector2(0f, -1f)
                else -> Vector2(0f, 1f)
            }

        return RaycastHit(entity, collider, point, normal, t)
    }
}
