package xyz.malefic.arachne.physics

import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.Vector2

data class RigidBody(
    var velocity: Vector2 = Vector2.ZERO.copy(),
    var acceleration: Vector2 = Vector2.ZERO.copy(),
    var mass: Float = 1f,
    var drag: Float = 0.01f,
    var angularVelocity: Float = 0f,
    var angularDrag: Float = 0.01f,
    var isKinematic: Boolean = false,
    var useGravity: Boolean = true,
    var gravityScale: Float = 1f,
    var isStatic: Boolean = false,
) : Component {
    fun addForce(force: Vector2) {
        if (!isKinematic && !isStatic) {
            acceleration = acceleration + (force / mass)
        }
    }

    fun addImpulse(impulse: Vector2) {
        if (!isKinematic && !isStatic) {
            velocity = velocity + (impulse / mass)
        }
    }

    fun addTorque(torque: Float) {
        if (!isKinematic && !isStatic) {
            angularVelocity += torque / mass
        }
    }
}
