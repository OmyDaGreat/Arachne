package xyz.malefic.arachne.core

import kotlin.reflect.KClass

class World {
    private val entities = mutableListOf<Entity>()
    private val systems = mutableListOf<System>()
    private val entitiesToAdd = mutableListOf<Entity>()
    private val entitiesToRemove = mutableListOf<Entity>()

    fun addEntity(entity: Entity) {
        entitiesToAdd.add(entity)
    }

    fun removeEntity(entity: Entity) {
        entitiesToRemove.add(entity)
    }

    fun removeEntity(id: String) {
        entities.find { it.id == id }?.let { removeEntity(it) }
    }

    fun getEntity(id: String) = entities.find { it.id == id }

    fun getEntitiesByTag(tag: String) = entities.filter { it.tag == tag }

    fun getEntitiesByLayer(layer: String) = entities.filter { it.layer == layer }

    fun addSystem(system: System) {
        systems.add(system)
    }

    fun removeSystem(system: System) {
        systems.remove(system)
    }

    fun update(deltaTime: Double) {
        // Add pending entities
        entities.addAll(entitiesToAdd)
        entitiesToAdd.clear()

        // Remove pending entities
        entities.removeAll(entitiesToRemove)
        entitiesToRemove.clear()

        // Update all systems
        systems.forEach { system ->
            system.update(deltaTime, entities)
        }
    }

    fun <T : Component> getEntitiesWith(vararg componentTypes: KClass<out T>): List<Entity> =
        entities.filter { entity ->
            entity.active && componentTypes.all { entity.has(it) }
        }

    inline fun <reified T : Component> getEntitiesWith(): List<Entity> = getEntitiesWith(T::class)

    fun getEntities() = entities.toList()

    fun clear() {
        entities.clear()
        systems.clear()
        entitiesToAdd.clear()
        entitiesToRemove.clear()
    }

    fun entityCount() = entities.size
}
