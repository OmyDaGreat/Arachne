package xyz.malefic.arachne.graphics

import xyz.malefic.arachne.core.Entity
import xyz.malefic.arachne.core.System
import xyz.malefic.arachne.math.Transform

class ParticleRenderSystem : System {
    override fun update(
        deltaTime: Double,
        entities: List<Entity>,
    ) {
        entities.forEach { entity ->
            val emitter = entity.get<ParticleEmitter>() ?: return@forEach
            val transform = entity.get<Transform>()

            emitter.update(deltaTime.toFloat(), transform ?: Transform())
        }
    }
}
