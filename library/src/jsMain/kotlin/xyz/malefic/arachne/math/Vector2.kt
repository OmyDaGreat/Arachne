package xyz.malefic.arachne.math

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector2(
    var x: Float = 0f,
    var y: Float = 0f,
) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    operator fun times(scalar: Float) = Vector2(x * scalar, y * scalar)

    operator fun div(scalar: Float) = Vector2(x / scalar, y / scalar)

    operator fun unaryMinus() = Vector2(-x, -y)

    fun magnitude() = sqrt(x * x + y * y)

    fun sqrMagnitude() = x * x + y * y

    fun normalize() {
        val mag = magnitude()
        if (mag > 0) {
            x /= mag
            y /= mag
        }
    }

    fun normalized(): Vector2 {
        val mag = magnitude()
        return if (mag > 0) Vector2(x / mag, y / mag) else Vector2()
    }

    fun dot(other: Vector2) = x * other.x + y * other.y

    fun cross(other: Vector2) = x * other.y - y * other.x

    fun distance(other: Vector2) = (this - other).magnitude()

    fun sqrDistance(other: Vector2) = (this - other).sqrMagnitude()

    fun angle() = atan2(y, x)

    fun angle(other: Vector2) = atan2(other.y - y, other.x - x)

    fun rotate(angle: Float): Vector2 {
        val cos = cos(angle)
        val sin = sin(angle)
        return Vector2(x * cos - y * sin, x * sin + y * cos)
    }

    fun lerp(
        other: Vector2,
        t: Float,
    ): Vector2 {
        val clampedT = t.coerceIn(0f, 1f)
        return Vector2(
            x + (other.x - x) * clampedT,
            y + (other.y - y) * clampedT,
        )
    }

    fun copy() = Vector2(x, y)

    fun set(
        newX: Float,
        newY: Float,
    ) {
        x = newX
        y = newY
    }

    companion object {
        val ZERO = Vector2(0f, 0f)
        val ONE = Vector2(1f, 1f)
        val UP = Vector2(0f, -1f)
        val DOWN = Vector2(0f, 1f)
        val LEFT = Vector2(-1f, 0f)
        val RIGHT = Vector2(1f, 0f)

        fun fromAngle(
            angle: Float,
            magnitude: Float = 1f,
        ) = Vector2(cos(angle) * magnitude, sin(angle) * magnitude)
    }
}
