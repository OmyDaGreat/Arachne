# Arachne - Game Development Library for Kobweb

[![Maven Central](https://img.shields.io/maven-central/v/xyz.malefic/arachne)](https://central.sonatype.com/artifact/xyz.malefic/arachne)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A comprehensive 2D game development library built on top of Kotlin/JS, designed to work seamlessly with the Kobweb framework. Arachne provides a complete Entity Component System (ECS), physics, rendering, input handling, and more to help you build browser-based games.

## ✨ Features

### Phase 1 - Core Foundation (Available Now!)

- **Entity Component System (ECS)**: Flexible and efficient entity management with components and systems
- **Game Loop**: Smooth game loop with both fixed and variable timestep support
- **Math Utilities**: Comprehensive 2D math library (Vector2, Transform, Rectangle, AABB)
- **Input Management**: Keyboard and mouse input handling with state tracking
- **Asset Manager**: Image loading with progress tracking
- **Sprite Rendering**: Hardware-accelerated sprite rendering with transforms
- **Camera System**: 2D camera with zoom, rotation, and following capabilities
- **Scene Management**: Organize your game into scenes with lifecycle management
- **Animation System**: Sprite sheet animation with frame-based animations
- **Debug Tools**: FPS counter, entity count, grid overlay, and more
- **Object Pooling**: Efficient object reuse for better performance

## 📦 Installation

Add Arachne to your Kotlin/JS or Kobweb project:

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("xyz.malefic:arachne:1.0.0")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'xyz.malefic:arachne:1.0.0'
}
```

## 🚀 Quick Start

Here's a minimal example to get you started:

```kotlin
class MyGame(canvas: HTMLCanvasElement) : Game(canvas) {
    private val world = World()
    private val renderSystem = RenderSystem(ctx, camera)
    
    override fun create() {
        // Load assets
        AssetManager.loadTexture("player.png")
        
        // Create a player entity
        val player = Entity().apply {
            add(Transform(position = Vector2(400f, 300f)))
            add(Sprite(texture = "player.png", width = 64f, height = 64f))
        }
        
        world.addEntity(player)
        world.addSystem(renderSystem)
    }
    
    override fun update(deltaTime: Double) {
        super.update(deltaTime)
        
        // Handle input
        val transform = world.getEntities()[0].get<Transform>()
        if (Input.isKeyDown("ArrowLeft")) {
            transform?.position?.x = (transform.position.x - 200f * deltaTime.toFloat())
        }
        if (Input.isKeyDown("ArrowRight")) {
            transform?.position?.x = (transform.position.x + 200f * deltaTime.toFloat())
        }
        
        world.update(deltaTime)
    }
    
    override fun render() {
        super.render()
        camera.applyTransform(ctx)
        world.update(0.0) // Trigger render system
        camera.resetTransform(ctx)
    }
}

fun main() {
    val canvas = document.getElementById("gameCanvas") as HTMLCanvasElement
    val game = MyGame(canvas)
    game.start()
}
```

## 📚 Core Concepts

### Entity Component System

Arachne uses an ECS architecture where:
- **Entities** are containers for components
- **Components** hold data (Transform, Sprite, RigidBody, etc.)
- **Systems** contain logic and operate on entities with specific components

```kotlin
// Create an entity
val entity = Entity()

// Add components
entity.add(Transform(position = Vector2(100f, 100f)))
entity.add(Sprite(texture = "sprite.png"))

// Access components
val transform = entity.get<Transform>()
transform?.position?.x = 200f

// Check for components
if (entity.has<Sprite>()) {
    // Do something
}
```

### World Management

The World manages all entities and systems:

```kotlin
val world = World()

// Add entities
world.addEntity(player)
world.addEntity(enemy)

// Add systems
world.addSystem(PhysicsSystem())
world.addSystem(RenderSystem(ctx, camera))

// Update all systems
world.update(deltaTime)

// Query entities
val movableEntities = world.getEntitiesWith<Transform, Velocity>()
```

### Input Handling

```kotlin
// Initialize input (usually done automatically by Game class)
Input.initialize(canvas)

// In your update loop
Input.update()

// Check keyboard state
if (Input.isKeyDown("Space")) {
    // Jump!
}

if (Input.isKeyPressed("e")) {
    // Interact (only once per press)
}

// Check mouse state
val mousePos = Input.getMousePosition()
if (Input.isMouseButtonPressed(Input.MOUSE_LEFT)) {
    // Handle click
}
```

### Camera System

```kotlin
val camera = Camera()
camera.setSize(800f, 600f)
camera.zoom = 2f // Zoom in 2x

// Follow a target
camera.follow(playerPosition, lerp = 0.1f)

// Convert between world and screen coordinates
val worldPos = camera.screenToWorld(mouseScreenPos)
val screenPos = camera.worldToScreen(entityWorldPos)
```

### Scene Management

```kotlin
class MenuScene : Scene() {
    override fun onCreate() {
        // Initialize scene
    }
    
    override fun onEnter() {
        // Called when scene becomes active
    }
    
    override fun update(deltaTime: Double) {
        super.update(deltaTime)
        // Update logic
    }
}

// Use the scene manager
val sceneManager = SceneManager()
sceneManager.addScene("menu", MenuScene())
sceneManager.addScene("game", GameScene())
sceneManager.loadScene("menu")
```

### Animation

```kotlin
// Create animations
val walkAnimation = Animation(
    name = "walk",
    frames = listOf(
        SpriteRect(0, 0, 32, 32),
        SpriteRect(32, 0, 32, 32),
        SpriteRect(64, 0, 32, 32),
    ),
    frameDuration = 0.1f,
    loop = true
)

// Add animator component
val animator = Animator()
animator.addAnimation(walkAnimation)
entity.add(animator)

// Play animation
animator.play("walk")

// Add animation system to world
world.addSystem(AnimationSystem())
```

## 🎮 Examples

Check out the `/examples` directory for complete game examples:

- **Basic Sprite Movement** - Simple sprite that responds to keyboard input
- **Platformer Demo** - A basic platformer with physics and collision
- **Top-Down Shooter** - Enemies, bullets, and more

## 🔧 Advanced Features

### Custom Components

```kotlin
data class Health(var current: Int, val max: Int) : Component

data class Velocity(var x: Float = 0f, var y: Float = 0f) : Component
```

### Custom Systems

```kotlin
class MovementSystem : System {
    override fun update(deltaTime: Double, entities: List<Entity>) {
        entities.forEach { entity ->
            val transform = entity.get<Transform>() ?: return@forEach
            val velocity = entity.get<Velocity>() ?: return@forEach
            
            transform.position.x += velocity.x * deltaTime.toFloat()
            transform.position.y += velocity.y * deltaTime.toFloat()
        }
    }
}
```

### Debug Tools

```kotlin
// Enable debug features
GameDebug.showFPS = true
GameDebug.showEntityCount = true
GameDebug.showGrid = true

// Draw debug info
GameDebug.drawDebugInfo(ctx, game.getFPS(), world)
GameDebug.drawGrid(ctx, camera, gridSize = 32f)
```

## 📖 API Documentation

Full API documentation is available at: [https://omydagreat.github.io/Arachne/](https://omydagreat.github.io/Arachne/)

## 🗺️ Roadmap

### Phase 2 - Game Mechanics (Coming Soon)
- Physics system with rigid bodies and colliders
- Particle system
- Audio manager
- Collision detection and response
- More animation features

### Phase 3 - Polish & Tools
- Tilemap support
- UI components (health bars, buttons, inventories)
- Save/Load system
- Pathfinding (A*)
- Tweening library

### Phase 4 - Advanced Features
- Shader support
- Network/multiplayer
- Advanced particle effects
- Scene transitions
- In-game editor tools

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👏 Acknowledgments

- Built with [Kotlin/JS](https://kotlinlang.org/docs/js-overview.html)
- Designed to work with [Kobweb](https://github.com/varabyte/kobweb)
- Inspired by game engines like Unity, LibGDX, and Phaser

## 📧 Contact

Om Gupta - [@OmyDaGreat](https://github.com/OmyDaGreat) - ogupta4242@gmail.com

Project Link: [https://github.com/OmyDaGreat/Arachne](https://github.com/OmyDaGreat/Arachne)

---

Made with ❤️ by [Malefic](https://malefic.xyz)
