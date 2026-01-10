package xyz.malefic.arachne.animation

import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.Vector2

sealed class TweenTarget {
    data class FloatValue(
        var value: Float,
        val setter: (Float) -> Unit,
    ) : TweenTarget()

    data class Vector2Value(
        var value: Vector2,
        val setter: (Vector2) -> Unit,
    ) : TweenTarget()
}

data class Tween(
    val target: TweenTarget,
    val endValue: Any,
    val duration: Float,
    val easing: (Float) -> Float = Easing::linear,
    val delay: Float = 0f,
    val onComplete: (() -> Unit)? = null,
    val onUpdate: ((Float) -> Unit)? = null,
) : Component {
    private var elapsed = 0f
    private var delayElapsed = 0f
    var isPlaying = true
        private set
    var isComplete = false
        private set

    fun update(deltaTime: Float) {
        if (!isPlaying || isComplete) return

        // Handle delay
        if (delayElapsed < delay) {
            delayElapsed += deltaTime
            return
        }

        elapsed += deltaTime
        val t = (elapsed / duration).coerceIn(0f, 1f)
        val easedT = easing(t)

        when (target) {
            is TweenTarget.FloatValue -> {
                val start = target.value
                val end = endValue as Float
                val current = start + (end - start) * easedT
                target.setter(current)
                onUpdate?.invoke(current)
            }

            is TweenTarget.Vector2Value -> {
                val start = target.value
                val end = endValue as Vector2
                val current = start.lerp(end, easedT)
                target.setter(current)
                onUpdate?.invoke(easedT)
            }
        }

        if (t >= 1f) {
            isComplete = true
            isPlaying = false
            onComplete?.invoke()
        }
    }

    fun pause() {
        isPlaying = false
    }

    fun resume() {
        if (!isComplete) {
            isPlaying = true
        }
    }

    fun stop() {
        isPlaying = false
        isComplete = true
    }

    fun restart() {
        elapsed = 0f
        delayElapsed = 0f
        isPlaying = true
        isComplete = false
    }
}

object TweenBuilder {
    fun tweenFloat(
        getter: () -> Float,
        setter: (Float) -> Unit,
        endValue: Float,
        duration: Float,
        easing: (Float) -> Float = Easing::linear,
        delay: Float = 0f,
        onComplete: (() -> Unit)? = null,
        onUpdate: ((Float) -> Unit)? = null,
    ): Tween {
        val target = TweenTarget.FloatValue(getter(), setter)
        return Tween(target, endValue, duration, easing, delay, onComplete, onUpdate)
    }

    fun tweenVector2(
        getter: () -> Vector2,
        setter: (Vector2) -> Unit,
        endValue: Vector2,
        duration: Float,
        easing: (Float) -> Float = Easing::linear,
        delay: Float = 0f,
        onComplete: (() -> Unit)? = null,
        onUpdate: ((Float) -> Unit)? = null,
    ): Tween {
        val target = TweenTarget.Vector2Value(getter(), setter)
        return Tween(target, endValue, duration, easing, delay, onComplete, onUpdate)
    }
}
