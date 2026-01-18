package xyz.malefic.arachne.core

import xyz.malefic.arachne.graphics.Animator
import xyz.malefic.arachne.graphics.ParticleEmitter
import xyz.malefic.arachne.graphics.Sprite
import xyz.malefic.arachne.graphics.SpriteRect
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2
import xyz.malefic.arachne.physics.BoxCollider
import xyz.malefic.arachne.physics.CapsuleCollider
import xyz.malefic.arachne.physics.CircleCollider
import xyz.malefic.arachne.physics.RigidBody

/**
 * DSL marker to prevent misuse of nested DSL builders.
 */
@DslMarker
annotation class EntityDsl

/**
 * Entity builder class providing a clean DSL for entity creation.
 *
 * Example:
 * ```kotlin
 * val player = entity {
 *     tag = "Player"
 *     layer = "characters"
 *
 *     transform {
 *         position = Vector2(400f, 300f)
 *         rotation = 0f
 *     }
 *
 *     sprite {
 *         texture = "player.png"
 *         width = 32f
 *         height = 32f
 *     }
 *
 *     circleCollider(radius = 16f)
 *
 *     rigidBody {
 *         mass = 1f
 *         useGravity = true
 *     }
 * }
 * ```
 */
@EntityDsl
class EntityBuilder {
    private val entity = Entity()

    /**
     * Set the entity's tag for identification.
     */
    var tag: String
        get() = entity.tag
        set(value) {
            entity.tag = value
        }

    /**
     * Set the entity's layer for rendering/collision filtering.
     */
    var layer: String
        get() = entity.layer
        set(value) {
            entity.layer = value
        }

    /**
     * Set the entity's active state.
     */
    var active: Boolean
        get() = entity.active
        set(value) {
            entity.active = value
        }

    /**
     * Get the entity's unique ID.
     */
    val id: String
        get() = entity.id

    // Transform component

    /**
     * Add a Transform component with a builder.
     */
    fun transform(builder: Transform.() -> Unit = {}) {
        val transform = Transform()
        transform.apply(builder)
        entity.add(transform)
    }

    /**
     * Add a Transform component with parameters.
     */
    fun transform(
        position: Vector2 = Vector2.ZERO.copy(),
        rotation: Float = 0f,
        scale: Vector2 = Vector2.ONE.copy(),
    ) {
        entity.add(Transform(position, rotation, scale))
    }

    // Sprite component

    /**
     * Add a Sprite component with a builder.
     */
    fun sprite(builder: Sprite.() -> Unit) {
        val sprite =
            Sprite(
                texture = "",
                width = 32f,
                height = 32f,
            )
        sprite.apply(builder)
        entity.add(sprite)
    }

    /**
     * Add a Sprite component with parameters.
     */
    fun sprite(
        texture: String,
        width: Float = 32f,
        height: Float = 32f,
        offset: Vector2 = Vector2.ZERO.copy(),
        flipX: Boolean = false,
        flipY: Boolean = false,
        opacity: Float = 1f,
        tint: String? = null,
        sourceRect: SpriteRect? = null,
    ) {
        entity.add(Sprite(texture, width, height, offset, flipX, flipY, opacity, tint, sourceRect))
    }

    // Collider components

    /**
     * Add a CircleCollider component.
     */
    fun circleCollider(
        radius: Float,
        offset: Vector2 = Vector2.ZERO.copy(),
        isTrigger: Boolean = false,
        layer: Int = 1,
        mask: Int = -1,
    ) {
        entity.add(CircleCollider(radius, offset, isTrigger, layer, mask))
    }

    /**
     * Add a BoxCollider component.
     */
    fun boxCollider(
        size: Vector2,
        offset: Vector2 = Vector2.ZERO.copy(),
        isTrigger: Boolean = false,
        layer: Int = 1,
        mask: Int = -1,
    ) {
        entity.add(BoxCollider(size, offset, isTrigger, layer, mask))
    }

    /**
     * Add a BoxCollider component with width and height.
     */
    fun boxCollider(
        width: Float,
        height: Float,
        offset: Vector2 = Vector2.ZERO.copy(),
        isTrigger: Boolean = false,
        layer: Int = 1,
        mask: Int = -1,
    ) {
        entity.add(BoxCollider(Vector2(width, height), offset, isTrigger, layer, mask))
    }

    /**
     * Add a CapsuleCollider component.
     */
    fun capsuleCollider(
        radius: Float,
        height: Float,
        offset: Vector2 = Vector2.ZERO.copy(),
        isTrigger: Boolean = false,
        layer: Int = 1,
        mask: Int = -1,
    ) {
        entity.add(CapsuleCollider(radius, height, offset, isTrigger, layer, mask))
    }

    // RigidBody component

    /**
     * Add a RigidBody component with a builder.
     */
    fun rigidBody(builder: RigidBody.() -> Unit = {}) {
        val rigidBody = RigidBody()
        rigidBody.apply(builder)
        entity.add(rigidBody)
    }

    /**
     * Add a RigidBody component with parameters.
     */
    fun rigidBody(
        velocity: Vector2 = Vector2.ZERO.copy(),
        acceleration: Vector2 = Vector2.ZERO.copy(),
        mass: Float = 1f,
        drag: Float = 0.01f,
        angularVelocity: Float = 0f,
        angularDrag: Float = 0.01f,
        isKinematic: Boolean = false,
        useGravity: Boolean = true,
        gravityScale: Float = 1f,
        isStatic: Boolean = false,
    ) {
        entity.add(
            RigidBody(
                velocity,
                acceleration,
                mass,
                drag,
                angularVelocity,
                angularDrag,
                isKinematic,
                useGravity,
                gravityScale,
                isStatic,
            ),
        )
    }

    // ParticleEmitter component

    /**
     * Add a ParticleEmitter component with a builder.
     */
    fun particleEmitter(builder: ParticleEmitter.() -> Unit) {
        val emitter =
            ParticleEmitter(
                emissionRate = 10f,
                startColor = "#ffffff",
                endColor = "#ffffff",
            )
        emitter.apply(builder)
        entity.add(emitter)
    }

    // Animator component

    /**
     * Add an Animator component with a builder.
     */
    fun animator(builder: Animator.() -> Unit = {}) {
        val animator = Animator()
        animator.apply(builder)
        entity.add(animator)
    }

    /**
     * Add a custom component to the entity.
     */
    fun <T : Component> component(component: T) {
        entity.add(component)
    }

    /**
     * Add a custom component with a builder.
     */
    fun <T : Component> component(
        component: T,
        builder: T.() -> Unit,
    ) {
        component.apply(builder)
        entity.add(component)
    }

    /**
     * Build and return the configured entity.
     */
    fun build(): Entity = entity
}

/**
 * Create an entity using the DSL.
 *
 * Example:
 * ```kotlin
 * val player = entity {
 *     tag = "Player"
 *     transform(position = Vector2(400f, 300f))
 *     sprite(texture = "player.png", width = 32f, height = 32f)
 *     circleCollider(radius = 16f)
 *     rigidBody(mass = 1f)
 * }
 * ```
 */
fun entity(builder: EntityBuilder.() -> Unit): Entity {
    val entityBuilder = EntityBuilder()
    entityBuilder.builder()
    return entityBuilder.build()
}

/**
 * Extension function to add entities to world using DSL.
 *
 * Example:
 * ```kotlin
 * world.addEntity {
 *     tag = "Enemy"
 *     transform(position = Vector2(200f, 200f))
 *     sprite(texture = "enemy.png")
 * }
 * ```
 */
fun World.addEntity(builder: EntityBuilder.() -> Unit): Entity {
    val entity = entity(builder)
    addEntity(entity)
    return entity
}
