package xyz.malefic.arachne.math

data class AABB(
    val min: Vector2,
    val max: Vector2,
) {
    val width get() = max.x - min.x
    val height get() = max.y - min.y
    val center get() = Vector2((min.x + max.x) / 2, (min.y + max.y) / 2)

    fun intersects(other: AABB) =
        min.x <= other.max.x &&
            max.x >= other.min.x &&
            min.y <= other.max.y &&
            max.y >= other.min.y

    fun contains(point: Vector2) =
        point.x >= min.x && point.x <= max.x &&
            point.y >= min.y && point.y <= max.y

    fun expand(amount: Float) =
        AABB(
            Vector2(min.x - amount, min.y - amount),
            Vector2(max.x + amount, max.y + amount),
        )

    companion object {
        fun fromCenterAndSize(
            center: Vector2,
            size: Vector2,
        ) = AABB(
            Vector2(center.x - size.x / 2, center.y - size.y / 2),
            Vector2(center.x + size.x / 2, center.y + size.y / 2),
        )

        fun fromRectangle(rect: Rectangle) =
            AABB(
                Vector2(rect.x, rect.y),
                Vector2(rect.x + rect.width, rect.y + rect.height),
            )
    }
}
