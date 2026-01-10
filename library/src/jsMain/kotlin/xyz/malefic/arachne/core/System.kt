package xyz.malefic.arachne.core

interface System {
    fun update(
        deltaTime: Double,
        entities: List<Entity>,
    )
}

abstract class ComponentSystem<T : Component>(
    private val componentType: kotlin.reflect.KClass<T>,
) : System {
    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        val filteredEntities = entities.filter { it.active && it.has(componentType) }
        updateEntities(deltaTime, filteredEntities)
    }

    abstract fun updateEntities(
        deltaTime: Double,
        entities: List<Entity>,
    )
}
