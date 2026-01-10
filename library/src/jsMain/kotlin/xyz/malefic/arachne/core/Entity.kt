package xyz.malefic.arachne.core

import kotlin.reflect.KClass

class Entity(
    val id: String = generateId(),
) {
    private val components = mutableMapOf<KClass<*>, Component>()
    var active = true
    var tag: String = ""
    var layer: String = "default"

    fun <T : Component> add(component: T): Entity {
        components[component::class] = component
        return this
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Component> get(type: KClass<T>): T? = components[type] as? T

    fun <T : Component> has(type: KClass<T>) = components.containsKey(type)

    fun <T : Component> remove(type: KClass<T>) {
        components.remove(type)
    }

    fun getAll(): List<Component> = components.values.toList()

    fun clear() {
        components.clear()
    }

    inline fun <reified T : Component> get(): T? = get(T::class)

    inline fun <reified T : Component> has(): Boolean = has(T::class)

    inline fun <reified T : Component> remove() = remove(T::class)

    companion object {
        private var nextId = 0

        fun generateId() = "entity_${nextId++}"
    }
}
