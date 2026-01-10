package xyz.malefic.arachne.scene

import xyz.malefic.arachne.core.World

abstract class Scene {
    val world = World()
    var active = true

    abstract fun onCreate()

    open fun onDestroy() {
        world.clear()
    }

    open fun update(deltaTime: Double) {
        if (active) {
            world.update(deltaTime)
        }
    }

    open fun onEnter() {}

    open fun onExit() {}
}
