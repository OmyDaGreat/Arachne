package xyz.malefic.arachne.utils

class ObjectPool<T>(
    private val factory: () -> T,
    private val reset: (T) -> Unit = {},
    initialCapacity: Int = 10,
) {
    private val pool = ArrayDeque<T>(initialCapacity)

    init {
        repeat(initialCapacity) {
            pool.add(factory())
        }
    }

    fun obtain(): T =
        if (pool.isNotEmpty()) {
            pool.removeFirst()
        } else {
            factory()
        }

    fun free(item: T) {
        reset(item)
        pool.add(item)
    }

    fun freeAll(items: List<T>) {
        items.forEach { free(it) }
    }

    fun clear() {
        pool.clear()
    }

    fun size() = pool.size
}
