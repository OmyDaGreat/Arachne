package xyz.malefic.arachne.graphics

import xyz.malefic.arachne.core.Component

data class Animation(
    val name: String,
    val frames: List<SpriteRect>,
    val frameDuration: Float,
    val loop: Boolean = true,
)

data class Animator(
    val animations: MutableMap<String, Animation> = mutableMapOf(),
    var currentAnimation: String? = null,
    var currentFrame: Int = 0,
    var frameTime: Float = 0f,
    var playing: Boolean = true,
) : Component {
    fun addAnimation(animation: Animation) {
        animations[animation.name] = animation
    }

    fun play(
        name: String,
        restart: Boolean = false,
    ) {
        if (currentAnimation == name && !restart) return

        currentAnimation = name
        currentFrame = 0
        frameTime = 0f
        playing = true
    }

    fun stop() {
        playing = false
    }

    fun pause() {
        playing = false
    }

    fun resume() {
        playing = true
    }

    fun update(
        deltaTime: Float,
        sprite: Sprite,
    ) {
        if (!playing) return

        val animation = currentAnimation?.let { animations[it] } ?: return

        frameTime += deltaTime

        if (frameTime >= animation.frameDuration) {
            frameTime = 0f
            currentFrame++

            if (currentFrame >= animation.frames.size) {
                if (animation.loop) {
                    currentFrame = 0
                } else {
                    currentFrame = animation.frames.size - 1
                    playing = false
                }
            }
        }

        if (currentFrame < animation.frames.size) {
            sprite.sourceRect = animation.frames[currentFrame]
        }
    }

    fun isFinished(): Boolean {
        val animation = currentAnimation?.let { animations[it] } ?: return true
        return !animation.loop && currentFrame >= animation.frames.size - 1 && !playing
    }
}
