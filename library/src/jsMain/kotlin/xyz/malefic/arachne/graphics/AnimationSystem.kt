package xyz.malefic.arachne.graphics

import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.core.System

class AnimationSystem : System {
    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        entities.forEach { entity ->
            val animator = entity.get<Animator>() ?: return@forEach
            val sprite = entity.get<Sprite>() ?: return@forEach

            animator.update(deltaTime.toFloat(), sprite)
        }
    }
}
