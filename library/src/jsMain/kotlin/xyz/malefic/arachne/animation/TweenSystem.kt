package xyz.malefic.arachne.animation

import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.core.System

class TweenSystem : System {
    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        entities.forEach { entity ->
            val tween = entity.get<Tween>() ?: return@forEach
            tween.update(deltaTime.toFloat())

            // Remove completed tweens
            if (tween.isComplete) {
                entity.remove<Tween>()
            }
        }
    }
}
