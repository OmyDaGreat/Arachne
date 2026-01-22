package xyz.malefic.arachne.examples

import xyz.malefic.arachne.animation.TweenSystem
import xyz.malefic.arachne.core.*
import xyz.malefic.arachne.debug.GameDebug
import xyz.malefic.arachne.graphics.*
import xyz.malefic.arachne.input.Input
import xyz.malefic.arachne.math.Transform
import xyz.malefic.arachne.math.Vector2
import xyz.malefic.arachne.physics.*
import xyz.malefic.arachne.utils.ObjectPool
import kotlin.math.sin

/**
 * A Galaga-inspired arcade shooter demonstrating:
 * - Wave-based enemy spawning
 * - Bullet pattern system
 * - Formation flying enemies
 * - Object pooling for bullets
 * - Score tracking
 * - Collision detection with layers
 */
class GalagaGame : Game() {
    private val world = World()
    private lateinit var player: Entity
    private val bulletPool =
        ObjectPool(
            factory = { createBullet() },
            initialCapacity = 100,
        )

    private var score = 0
    private var wave = 1
    private var enemyCount = 0
    private var spawnTimer = 0.0
    private val spawnInterval = 2.0

    // Game bounds
    private val gameWidth = 800f
    private val gameHeight = 600f

    override fun create() {
        GameDebug.showFPS = true
        camera.position = Vector2(gameWidth / 2, gameHeight / 2)

        // Setup systems
        val physicsSystem = PhysicsSystem(gravity = Vector2.ZERO)
        world.addSystem(physicsSystem)
        world.addSystem(AnimationSystem())
        world.addSystem(TweenSystem())

        // Setup rendering
        val renderSystem = RenderSystem(ctx, camera)
        renderSystem.backgroundColor = "#000011"
        world.addSystem(renderSystem)
        world.addSystem(ParticleRenderSystem())

        // Create player
        createPlayer()

        // Setup collision callbacks
        physicsSystem.onCollisionEnter.add { collision ->
            handleCollision(collision)
        }

        // Spawn initial wave
        spawnEnemyWave()
    }

    private fun createPlayer() {
        player =
            entity {
                tag = "player"
                layer = "1"
                transform(position = Vector2(gameWidth / 2, gameHeight - 80f))
                sprite(texture = "player.png", width = 32f, height = 32f)
                boxCollider(size = Vector2(32f, 32f))
                rigidBody(mass = 1f, drag = 5f, useGravity = false)
                particleEmitter {
                    emissionRate = 5f
                    startColor = "#4ecdc4"
                    endColor = "#0066ff"
                    startSize = 2f
                    particleLifetime = 0.5f
                    velocity = Vector2(0f, 50f)
                    velocityVariation = Vector2(10f, 5f)
                }
            }
        world.addEntity(player)
    }

    private fun createBullet() =
        Entity().apply {
            tag = "bullet"
            layer = "2"
            active = false
            add(Transform(position = Vector2.ZERO))
            add(Sprite(texture = "bullet.png", width = 8f, height = 16f))
            add(BoxCollider(size = Vector2(8f, 16f)))
            add(RigidBody(mass = 0.1f, drag = 0f, useGravity = false))
        }

    private fun spawnEnemyWave() {
        val columns = 8
        val rows = 3
        val spacingX = 60f
        val spacingY = 50f
        val startX = (gameWidth - (columns - 1) * spacingX) / 2
        val startY = 100f

        for (row in 0 until rows) {
            for (col in 0 until columns) {
                val x = startX + col * spacingX
                val y = startY + row * spacingY
                createEnemy(Vector2(x, y), row)
            }
        }

        enemyCount = columns * rows
    }

    private fun createEnemy(
        position: Vector2,
        type: Int,
    ) {
        val enemy =
            entity {
                tag = "enemy"
                layer = "1"
                transform(position = position)

                // Different sprites based on type/row
                val enemyType =
                    when (type) {
                        0 -> "elite"
                        1 -> "grunt"
                        else -> "scout"
                    }
                sprite(texture = "enemy_$enemyType.png", width = 28f, height = 28f)

                boxCollider(size = Vector2(28f, 28f))
                rigidBody(mass = 0.5f, drag = 2f, useGravity = false)

                particleEmitter {
                    emissionRate = 0f
                    startColor = "#ff6b6b"
                    endColor = "#ffd93d"
                    startSize = 4f
                    particleLifetime = 0.8f
                }
            }

        // Add custom data component for enemy behavior
        enemy.add(EnemyData(position, type))
        world.addEntity(enemy)
    }

    private fun shootBullet(
        position: Vector2,
        velocity: Vector2,
    ) {
        val bullet = bulletPool.obtain()
        bullet.active = true
        val transform = bullet.get<Transform>()
        if (transform != null) {
            transform.position.x = position.x
            transform.position.y = position.y
        }
        bullet.get<RigidBody>()?.let { rb ->
            rb.velocity.x = velocity.x
            rb.velocity.y = velocity.y
        }
        if (!world.getEntities().contains(bullet)) {
            world.addEntity(bullet)
        }
    }

    private fun handleCollision(collision: Collision) {
        val a = collision.entityA
        val b = collision.entityB

        // Bullet hits enemy
        if ((a.tag == "bullet" && b.tag == "enemy") || (a.tag == "enemy" && b.tag == "bullet")) {
            val bullet = if (a.tag == "bullet") a else b
            val enemy = if (a.tag == "enemy") a else b

            // Deactivate bullet
            bullet.active = false
            bulletPool.free(bullet)

            // Destroy enemy with particle burst
            enemy.get<ParticleEmitter>()?.burst()
            world.removeEntity(enemy)

            enemyCount--
            score += 100
            camera.shake(3f, 0.1f)

            // Spawn new wave if all enemies defeated
            if (enemyCount <= 0) {
                wave++
                spawnEnemyWave()
            }
        }

        // Enemy hits player
        if ((a.tag == "player" && b.tag == "enemy") || (a.tag == "enemy" && b.tag == "player")) {
            camera.shake(10f, 0.5f)
            // In a real game, would handle player death here
        }
    }

    override fun update(deltaTime: Double) {
        super.update(deltaTime)
        camera.update(deltaTime.toFloat())

        // Player movement
        updatePlayer(deltaTime)

        // Enemy movement
        updateEnemies(deltaTime)

        // Cleanup bullets out of bounds
        cleanupBullets()

        world.update(deltaTime)
    }

    private fun updatePlayer(deltaTime: Double) {
        val transform = player.get<Transform>()
        val rb = player.get<RigidBody>()

        if (transform != null && rb != null) {
            val moveSpeed = 400f

            // Horizontal movement
            if (Input.isKeyDown("ArrowLeft") || Input.isKeyDown("a")) {
                rb.addForce(Vector2(-moveSpeed, 0f))
            }
            if (Input.isKeyDown("ArrowRight") || Input.isKeyDown("d")) {
                rb.addForce(Vector2(moveSpeed, 0f))
            }

            // Clamp player to screen bounds
            transform.position.x = transform.position.x.coerceIn(32f, gameWidth - 32f)

            // Shooting
            if (Input.isKeyPressed("Space")) {
                shootBullet(
                    transform.position + Vector2(0f, -20f),
                    Vector2(0f, -400f),
                )
            }
        }
    }

    private fun updateEnemies(deltaTime: Double) {
        val time = clock.elapsedTime

        world.getEntitiesWith(Transform::class, EnemyData::class).forEach { enemy ->
            val transform = enemy.get<Transform>()!!
            val data = enemy.get<EnemyData>()!!

            // Formation flying with sine wave
            val offsetX = sin(time * 2.0 + data.formationOffset).toFloat() * 30f
            val targetX = data.homePosition.x + offsetX

            // Smooth movement towards target
            val diffX = targetX - transform.position.x
            transform.position.x += diffX * deltaTime.toFloat() * 2f

            // Occasional dive attack
            if (time % 5.0 < deltaTime && enemy.active) {
                enemy.get<RigidBody>()?.addImpulse(Vector2(0f, 200f))
            }
        }
    }

    private fun cleanupBullets() {
        world.getEntitiesWith(Transform::class).forEach { entity ->
            if (entity.tag == "bullet") {
                val transform = entity.get<Transform>()!!

                // Remove bullets that left the screen
                if (transform.position.y < -50f || transform.position.y > gameHeight + 50f) {
                    entity.active = false
                    bulletPool.free(entity)
                }
            }
        }
    }

    override fun render() {
        super.render()
        // RenderSystem and ParticleRenderSystem handle rendering automatically

        // Draw UI
        drawUI()

        // Debug info
        GameDebug.drawDebugInfo(ctx, getFPS(), world)
    }

    private fun drawUI() {
        ctx.fillStyle = "white"
        ctx.font = "20px monospace"
        ctx.fillText("SCORE: $score", 20.0, 30.0)
        ctx.fillText("WAVE: $wave", 20.0, 60.0)

        ctx.font = "14px monospace"
        ctx.fillText("Arrow Keys/AD to move, Space to shoot", 20.0, gameHeight - 20.0)
    }
}

/**
 * Custom component to store enemy-specific data
 */
data class EnemyData(
    val homePosition: Vector2,
    val type: Int,
    val formationOffset: Double = kotlin.random.Random.nextDouble() * kotlin.math.PI * 2,
) : Component
