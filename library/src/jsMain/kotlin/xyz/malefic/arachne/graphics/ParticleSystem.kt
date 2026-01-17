package xyz.malefic.arachne.graphics

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.MathUtils
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2
import xyz.malefic.arachne.utils.ObjectPool
import kotlin.math.cos
import kotlin.math.sin

data class Particle(
    var position: Vector2 = Vector2.ZERO.copy(),
    var velocity: Vector2 = Vector2.ZERO.copy(),
    var lifetime: Float = 0f,
    var maxLifetime: Float = 1f,
    var size: Float = 1f,
    var startSize: Float = 1f,
    var endSize: Float = 1f,
    var color: String = "#ffffff",
    var startColor: String = "#ffffff",
    var endColor: String = "#ffffff",
    var alpha: Float = 1f,
    var rotation: Float = 0f,
    var angularVelocity: Float = 0f,
    var active: Boolean = false,
) {
    fun update(
        deltaTime: Float,
        gravity: Vector2,
    ) {
        if (!active) return

        lifetime += deltaTime
        if (lifetime >= maxLifetime) {
            active = false
            return
        }

        // Update physics
        velocity += gravity * deltaTime
        position += velocity * deltaTime
        rotation += angularVelocity * deltaTime

        // Update size and alpha
        val t = lifetime / maxLifetime
        size = MathUtils.lerp(startSize, endSize, t)
        alpha = 1f - t
    }

    fun reset() {
        position = Vector2.ZERO.copy()
        velocity = Vector2.ZERO.copy()
        lifetime = 0f
        active = false
        rotation = 0f
        angularVelocity = 0f
    }
}

enum class EmitterShape {
    POINT,
    CIRCLE,
    RECTANGLE,
    CONE,
}

data class ParticleEmitter(
    var emissionRate: Float = 10f,
    var maxParticles: Int = 100,
    var particleLifetime: Float = 1f,
    var lifetimeVariation: Float = 0f,
    var startColor: String = "#ffffff",
    var endColor: String = "#ffffff",
    var startSize: Float = 5f,
    var endSize: Float = 0f,
    var sizeVariation: Float = 0f,
    var velocity: Vector2 = Vector2(0f, -100f),
    var velocityVariation: Vector2 = Vector2(50f, 50f),
    var spread: Float = 0f,
    var gravity: Vector2 = Vector2(0f, 0f),
    var angularVelocity: Float = 0f,
    var angularVelocityVariation: Float = 0f,
    var emitterShape: EmitterShape = EmitterShape.POINT,
    var shapeSize: Vector2 = Vector2(10f, 10f),
    var isPlaying: Boolean = false,
    var loop: Boolean = true,
    var duration: Float = -1f,
    var burstCount: Int = 0,
) : Component {
    private val particles = mutableListOf<Particle>()
    private val particlePool =
        ObjectPool(
            factory = { Particle() },
            reset = { it.reset() },
            initialCapacity = maxParticles,
        )
    private var emissionTimer = 0f
    private var durationTimer = 0f

    init {
        repeat(maxParticles) {
            particles.add(particlePool.obtain())
        }
    }

    fun play() {
        isPlaying = true
        durationTimer = 0f
    }

    fun stop() {
        isPlaying = false
    }

    fun emit(count: Int = 1) {
        repeat(count) {
            emitParticle()
        }
    }

    fun burst() {
        if (burstCount > 0) {
            emit(burstCount)
        }
    }

    fun update(
        deltaTime: Float,
        transform: Transform,
    ) {
        if (isPlaying) {
            durationTimer += deltaTime

            if (duration > 0 && durationTimer >= duration) {
                if (loop) {
                    durationTimer = 0f
                } else {
                    isPlaying = false
                }
            }

            if (isPlaying) {
                emissionTimer += deltaTime
                val particlesToEmit = (emissionTimer * emissionRate).toInt()
                if (particlesToEmit > 0) {
                    repeat(particlesToEmit) {
                        emitParticle(transform)
                    }
                    emissionTimer -= particlesToEmit / emissionRate
                }
            }
        }

        // Update all active particles
        particles.forEach { particle ->
            if (particle.active) {
                particle.update(deltaTime, gravity)
            }
        }
    }

    private fun emitParticle(transform: Transform? = null) {
        val particle = particles.firstOrNull { !it.active } ?: return

        // Set initial position based on emitter shape
        val spawnPos = transform?.position ?: Vector2.ZERO.copy()
        particle.position =
            when (emitterShape) {
                EmitterShape.POINT -> {
                    spawnPos
                }

                EmitterShape.CIRCLE -> {
                    val angle = MathUtils.random(0f, MathUtils.TWO_PI)
                    val radius = MathUtils.random(0f, shapeSize.x)
                    spawnPos + Vector2(cos(angle) * radius, sin(angle) * radius)
                }

                EmitterShape.RECTANGLE -> {
                    spawnPos +
                        Vector2(
                            MathUtils.random(-shapeSize.x / 2, shapeSize.x / 2),
                            MathUtils.random(-shapeSize.y / 2, shapeSize.y / 2),
                        )
                }

                EmitterShape.CONE -> {
                    val angle =
                        (transform?.rotation ?: 0f) +
                            MathUtils.random(-spread / 2, spread / 2)
                    val distance = MathUtils.random(0f, shapeSize.x)
                    spawnPos + Vector2(cos(angle) * distance, sin(angle) * distance)
                }
            }

        // Set velocity with variation
        val baseVel = velocity.copy()
        val varX = MathUtils.random(-velocityVariation.x, velocityVariation.x)
        val varY = MathUtils.random(-velocityVariation.y, velocityVariation.y)
        particle.velocity = Vector2(baseVel.x + varX, baseVel.y + varY)

        // Apply spread angle
        if (spread > 0) {
            val angle = MathUtils.random(-spread / 2, spread / 2)
            particle.velocity = particle.velocity.rotate(angle)
        }

        // Set lifetime
        val lifeVar = MathUtils.random(-lifetimeVariation, lifetimeVariation)
        particle.maxLifetime = (particleLifetime + lifeVar).coerceAtLeast(0.1f)
        particle.lifetime = 0f

        // Set size
        val sizeVar = MathUtils.random(-sizeVariation, sizeVariation)
        particle.startSize = (startSize + sizeVar).coerceAtLeast(0.1f)
        particle.endSize = endSize

        // Set color
        particle.startColor = startColor
        particle.endColor = endColor
        particle.color = startColor

        // Set angular velocity
        val angVelVar = MathUtils.random(-angularVelocityVariation, angularVelocityVariation)
        particle.angularVelocity = angularVelocity + angVelVar

        particle.active = true
    }

    fun render(ctx: CanvasRenderingContext2D) {
        particles.forEach { particle ->
            if (!particle.active) return@forEach

            ctx.save()
            ctx.globalAlpha = particle.alpha.toDouble()
            ctx.fillStyle = particle.color
            ctx.translate(particle.position.x.toDouble(), particle.position.y.toDouble())
            ctx.rotate(particle.rotation.toDouble())

            // Draw as circle
            ctx.beginPath()
            ctx.arc(0.0, 0.0, particle.size.toDouble(), 0.0, MathUtils.TWO_PI.toDouble())
            ctx.fill()

            ctx.restore()
        }
    }

    fun clear() {
        particles.forEach { it.active = false }
    }

    fun getActiveParticleCount() = particles.count { it.active }
}
