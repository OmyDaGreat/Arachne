package xyz.malefic.arachne.math

import kotlin.math.abs
import kotlin.random.Random

object MathUtils {
    const val PI = kotlin.math.PI.toFloat()
    const val TWO_PI = (kotlin.math.PI * 2).toFloat()
    const val HALF_PI = (kotlin.math.PI / 2).toFloat()
    const val DEG_TO_RAD = (kotlin.math.PI / 180).toFloat()
    const val RAD_TO_DEG = (180 / kotlin.math.PI).toFloat()
    const val EPSILON = 0.000001f

    fun lerp(
        start: Float,
        end: Float,
        t: Float,
    ) = start + (end - start) * t

    fun clamp(
        value: Float,
        min: Float,
        max: Float,
    ) = when {
        value < min -> min
        value > max -> max
        else -> value
    }

    fun clamp01(value: Float) = clamp(value, 0f, 1f)

    fun radiansToDegrees(radians: Float) = radians * RAD_TO_DEG

    fun degreesToRadians(degrees: Float) = degrees * DEG_TO_RAD

    fun approximately(
        a: Float,
        b: Float,
        epsilon: Float = EPSILON,
    ) = abs(a - b) < epsilon

    fun sign(value: Float) =
        when {
            value > 0 -> 1f
            value < 0 -> -1f
            else -> 0f
        }

    fun smoothstep(
        edge0: Float,
        edge1: Float,
        x: Float,
    ): Float {
        val t = clamp01((x - edge0) / (edge1 - edge0))
        return t * t * (3f - 2f * t)
    }

    fun pingPong(
        t: Float,
        length: Float,
    ): Float {
        val normalizedT = t % (length * 2)
        return if (normalizedT < length) normalizedT else length * 2 - normalizedT
    }

    fun random(
        min: Float,
        max: Float,
    ) = Random.nextFloat() * (max - min) + min

    fun randomInt(
        min: Int,
        max: Int,
    ) = Random.nextInt(min, max + 1)

    fun randomBoolean() = Random.nextBoolean()
}
