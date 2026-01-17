package xyz.malefic.arachne.math

import xyz.malefic.arachne.core.Component

data class Transform(
    var position: Vector2 = Vector2.ZERO.copy(),
    var rotation: Float = 0f,
    var scale: Vector2 = Vector2.ONE.copy(),
) : Component {
    fun translate(offset: Vector2) {
        position += offset
    }

    fun rotate(angle: Float) {
        rotation += angle
    }

    fun lookAt(target: Vector2) {
        rotation = position.angle(target)
    }

    fun localToWorld(localPoint: Vector2): Vector2 {
        var point = Vector2(localPoint.x * scale.x, localPoint.y * scale.y)
        point = point.rotate(rotation)
        return point + position
    }

    fun worldToLocal(worldPoint: Vector2): Vector2 {
        var point = worldPoint - position
        point = point.rotate(-rotation)
        return Vector2(point.x / scale.x, point.y / scale.y)
    }

    fun copy() = Transform(position.copy(), rotation, scale.copy())
}
