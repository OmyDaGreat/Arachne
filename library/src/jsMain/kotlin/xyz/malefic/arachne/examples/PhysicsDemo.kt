package xyz.malefic.arachne.examples

import xyz.malefic.arachne.animation.TweenSystem
import xyz.malefic.arachne.audio.AudioManager
import xyz.malefic.arachne.core.*
import xyz.malefic.arachne.debug.GameDebug
import xyz.malefic.arachne.graphics.*
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2
import xyz.malefic.arachne.physics.*

/**
 * Phase 2 demonstration showcasing:
 * - Physics simulation with RigidBody
 * - Collision detection with various colliders
 * - Particle effects
 * - Audio playback
 * - Camera shake
 * - Tweening animations
 */
class PhysicsDemo : Game() {
    private val world = World()
    private lateinit var player: Entity

    override fun create() {
        GameDebug.showFPS = true
        GameDebug.showColliders = true

        // Create physics system
        val physicsSystem = PhysicsSystem(gravity = Vector2(0f, 500f))
        world.addSystem(physicsSystem)
        world.addSystem(RenderSystem(ctx, camera))
        world.addSystem(AnimationSystem())
        world.addSystem(TweenSystem())
        world.addSystem(ParticleRenderSystem())

        // Create ground
        createGround()

        // Create player
        createPlayer()

        // Create some boxes
        for (i in 0..3) {
            createBox(Vector2(200f + i * 100f, 200f))
        }

        // Physics collision callbacks
        physicsSystem.onCollisionEnter.add { collision ->
            if (collision.entityA.tag == "player" || collision.entityB.tag == "player") {
                camera.shake(5f, 0.2f)
            }
        }
    }

    private fun createGround() {
        world.addEntity {
            tag = "ground"
            transform(position = Vector2(400f, 550f))
            sprite(texture = "ground.png", width = 800f, height = 50f)
            boxCollider(size = Vector2(800f, 50f))
            rigidBody(isStatic = true)
        }
    }

    private fun createPlayer() {
        player =
            entity {
                tag = "player"
                transform(position = Vector2(400f, 100f))
                sprite(texture = "player.png", width = 32f, height = 32f)
                circleCollider(radius = 16f)
                rigidBody(mass = 1f, drag = 0.1f)
                particleEmitter {
                    emissionRate = 10f
                    startColor = "#ff6b6b"
                    endColor = "#ffd93d"
                    startSize = 3f
                    velocity = Vector2(0f, 50f)
                    velocityVariation = Vector2(20f, 10f)
                }
            }
        world.addEntity(player)
    }

    private fun createBox(position: Vector2) {
        world.addEntity {
            tag = "box"
            transform(position = position)
            sprite(texture = "box.png", width = 40f, height = 40f)
            boxCollider(size = Vector2(40f, 40f))
            rigidBody(mass = 0.5f, drag = 0.05f)
        }
    }

    override fun update(deltaTime: Double) {
        super.update(deltaTime)
        camera.update(deltaTime.toFloat())

        val playerTransform = player.get<Transform>()
        val playerRb = player.get<RigidBody>()

        if (playerTransform != null && playerRb != null) {
            val moveSpeed = 300f

            if (Input.isKeyDown("ArrowLeft") || Input.isKeyDown("a")) {
                playerRb.addForce(Vector2(-moveSpeed, 0f))
            }
            if (Input.isKeyDown("ArrowRight") || Input.isKeyDown("d")) {
                playerRb.addForce(Vector2(moveSpeed, 0f))
            }
            if (Input.isKeyPressed("Space") || Input.isKeyPressed("w")) {
                playerRb.addImpulse(Vector2(0f, -300f))
                AudioManager.playSound("jump.mp3", volume = 0.5f)
                player.get<ParticleEmitter>()?.burst()
            }

            camera.follow(playerTransform.position, lerp = 0.1f)
        }

        world.update(deltaTime)
    }

    override fun render() {
        ctx.fillStyle = "#1a1a2e"
        ctx.fillRect(0.0, 0.0, canvas.width.toDouble(), canvas.height.toDouble())

        camera.applyTransform(ctx)

        world.getEntitiesWith(Transform::class, Sprite::class).forEach { entity ->
            val t = entity.get<Transform>()!!
            val s = entity.get<Sprite>()!!

            ctx.save()
            ctx.translate(t.position.x.toDouble(), t.position.y.toDouble())
            ctx.rotate(t.rotation.toDouble())

            ctx.fillStyle =
                when (entity.tag) {
                    "player" -> "#ff6b6b"
                    "box" -> "#4ecdc4"
                    "ground" -> "#95a99c"
                    else -> "#ffffff"
                }
            ctx.fillRect(
                (-s.width / 2).toDouble(),
                (-s.height / 2).toDouble(),
                s.width.toDouble(),
                s.height.toDouble(),
            )
            ctx.restore()
        }

        world.getEntitiesWith(Transform::class, ParticleEmitter::class).forEach { entity ->
            entity.get<ParticleEmitter>()?.render(ctx)
        }

        camera.resetTransform(ctx)

        GameDebug.drawDebugInfo(ctx, getFPS(), world)
        GameDebug.drawColliders(ctx, world, camera)
        GameDebug.drawGrid(ctx, camera)

        ctx.fillStyle = "white"
        ctx.font = "14px monospace"
        ctx.fillText("WASD/Arrows to move, Space to jump", 10.0, canvas.height - 20.0)
    }
}
