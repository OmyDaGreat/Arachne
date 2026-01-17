package xyz.malefic.arachne.animation

import xyz.malefic.arachne.math.MathUtils
import kotlin.math.pow
import kotlin.math.sin

object Easing {
    // Linear
    fun linear(t: Float) = t

    // Quadratic
    fun easeInQuad(t: Float) = t * t

    fun easeOutQuad(t: Float) = t * (2f - t)

    fun easeInOutQuad(t: Float) = if (t < 0.5f) 2f * t * t else -1f + (4f - 2f * t) * t

    // Cubic
    fun easeInCubic(t: Float) = t * t * t

    fun easeOutCubic(t: Float): Float {
        var t1 = t
        t1 -= 1f
        return t1 * t1 * t1 + 1f
    }

    fun easeInOutCubic(t: Float): Float {
        var t1 = t
        return if (t1 < 0.5f) {
            4f * t1 * t1 * t1
        } else {
            t1 -= 1f
            t1 * (2f * t - 2f) * (2f * t - 2f) + 1f
        }
    }

    // Quartic
    fun easeInQuart(t: Float) = t * t * t * t

    fun easeOutQuart(t: Float): Float {
        var t1 = t
        t1 -= 1f
        return 1f - t1 * t1 * t1 * t1
    }

    fun easeInOutQuart(t: Float): Float {
        var t1 = t
        return if (t1 < 0.5f) {
            8f * t1 * t1 * t1 * t1
        } else {
            t1 -= 1f
            1f - 8f * t1 * t1 * t1 * t1
        }
    }

    // Quintic
    fun easeInQuint(t: Float) = t * t * t * t * t

    fun easeOutQuint(t: Float): Float {
        var t1 = t
        t1 -= 1f
        return 1f + t1 * t1 * t1 * t1 * t1
    }

    fun easeInOutQuint(t: Float): Float {
        var t1 = t
        return if (t1 < 0.5f) {
            16f * t1 * t1 * t1 * t1 * t1
        } else {
            t1 -= 1f
            1f + 16f * t1 * t1 * t1 * t1 * t1
        }
    }

    // Sine
    fun easeInSine(t: Float) = 1f - kotlin.math.cos(t * MathUtils.HALF_PI)

    fun easeOutSine(t: Float) = sin(t * MathUtils.HALF_PI)

    fun easeInOutSine(t: Float) = -(kotlin.math.cos(MathUtils.PI * t) - 1f) / 2f

    // Exponential
    fun easeInExpo(t: Float) = if (t == 0f) 0f else 2f.pow(10f * t - 10f)

    fun easeOutExpo(t: Float) = if (t == 1f) 1f else 1f - 2f.pow(-10f * t)

    fun easeInOutExpo(t: Float) =
        when {
            t == 0f -> 0f
            t == 1f -> 1f
            t < 0.5f -> 2f.pow(20f * t - 10f) / 2f
            else -> (2f - 2f.pow(-20f * t + 10f)) / 2f
        }

    // Elastic
    fun easeInElastic(t: Float): Float {
        val c4 = (2f * MathUtils.PI) / 3f
        return when (t) {
            0f -> 0f
            1f -> 1f
            else -> -(2f.pow(10f * t - 10f)) * sin((t * 10f - 10.75f) * c4)
        }
    }

    fun easeOutElastic(t: Float): Float {
        val c4 = (2f * MathUtils.PI) / 3f
        return when (t) {
            0f -> 0f
            1f -> 1f
            else -> 2f.pow(-10f * t) * sin((t * 10f - 0.75f) * c4) + 1f
        }
    }

    fun easeInOutElastic(t: Float): Float {
        val c5 = (2f * MathUtils.PI) / 4.5f
        return when {
            t == 0f -> 0f
            t == 1f -> 1f
            t < 0.5f -> -(2f.pow(20f * t - 10f) * sin((20f * t - 11.125f) * c5)) / 2f
            else -> (2f.pow(-20f * t + 10f) * sin((20f * t - 11.125f) * c5)) / 2f + 1f
        }
    }

    // Back
    fun easeInBack(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return c3 * t * t * t - c1 * t * t
    }

    fun easeOutBack(t: Float): Float {
        val c1 = 1.70158f
        val c3 = c1 + 1f
        return 1f + c3 * (t - 1f).pow(3) + c1 * (t - 1f).pow(2)
    }

    fun easeInOutBack(t: Float): Float {
        val c1 = 1.70158f
        val c2 = c1 * 1.525f
        return if (t < 0.5f) {
            ((2f * t).pow(2) * ((c2 + 1f) * 2f * t - c2)) / 2f
        } else {
            ((2f * t - 2f).pow(2) * ((c2 + 1f) * (t * 2f - 2f) + c2) + 2f) / 2f
        }
    }

    // Bounce
    fun easeOutBounce(t: Float): Float {
        val n1 = 7.5625f
        val d1 = 2.75f

        return when {
            t < 1f / d1 -> n1 * t * t
            t < 2f / d1 -> n1 * (t - 1.5f / d1) * (t - 1.5f / d1) + 0.75f
            t < 2.5f / d1 -> n1 * (t - 2.25f / d1) * (t - 2.25f / d1) + 0.9375f
            else -> n1 * (t - 2.625f / d1) * (t - 2.625f / d1) + 0.984375f
        }
    }

    fun easeInBounce(t: Float) = 1f - easeOutBounce(1f - t)

    fun easeInOutBounce(t: Float) =
        if (t < 0.5f) {
            (1f - easeOutBounce(1f - 2f * t)) / 2f
        } else {
            (1f + easeOutBounce(2f * t - 1f)) / 2f
        }
}
