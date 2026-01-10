package xyz.malefic.arachne.math

data class Rectangle(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float,
) {
    val left get() = x
    val right get() = x + width
    val top get() = y
    val bottom get() = y + height
    val centerX get() = x + width / 2
    val centerY get() = y + height / 2
    val center get() = Vector2(centerX, centerY)

    fun contains(point: Vector2) =
        point.x >= x && point.x <= x + width &&
            point.y >= y && point.y <= y + height

    fun intersects(other: Rectangle) =
        x < other.x + other.width &&
            x + width > other.x &&
            y < other.y + other.height &&
            y + height > other.y

    fun copy() = Rectangle(x, y, width, height)

    companion object {
        fun fromCenter(
            center: Vector2,
            width: Float,
            height: Float,
        ) = Rectangle(center.x - width / 2, center.y - height / 2, width, height)
    }
}
