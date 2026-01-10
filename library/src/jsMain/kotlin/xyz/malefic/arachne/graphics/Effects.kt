package xyz.malefic.arachne.graphics

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.core.Component

data class ShaderEffect(
    val name: String,
    var enabled: Boolean = true,
    val parameters: MutableMap<String, Any> = mutableMapOf(),
) : Component

object CanvasEffects {
    fun applyGrayscale(ctx: CanvasRenderingContext2D) {
        ctx.asDynamic().filter = "grayscale(100%)"
    }

    fun applySepia(ctx: CanvasRenderingContext2D) {
        ctx.asDynamic().filter = "sepia(100%)"
    }

    fun applyBlur(
        ctx: CanvasRenderingContext2D,
        amount: Int = 5,
    ) {
        ctx.asDynamic().filter = "blur(${amount}px)"
    }

    fun applyBrightness(
        ctx: CanvasRenderingContext2D,
        amount: Float = 1.5f,
    ) {
        ctx.asDynamic().filter = "brightness($amount)"
    }

    fun applyContrast(
        ctx: CanvasRenderingContext2D,
        amount: Float = 1.5f,
    ) {
        ctx.asDynamic().filter = "contrast($amount)"
    }

    fun applyHueRotate(
        ctx: CanvasRenderingContext2D,
        degrees: Int = 90,
    ) {
        ctx.asDynamic().filter = "hue-rotate(${degrees}deg)"
    }

    fun applyInvert(ctx: CanvasRenderingContext2D) {
        ctx.asDynamic().filter = "invert(100%)"
    }

    fun applySaturate(
        ctx: CanvasRenderingContext2D,
        amount: Float = 2f,
    ) {
        ctx.asDynamic().filter = "saturate($amount)"
    }

    fun combineFilters(
        ctx: CanvasRenderingContext2D,
        filters: List<String>,
    ) {
        ctx.asDynamic().filter = filters.joinToString(" ")
    }

    fun resetFilters(ctx: CanvasRenderingContext2D) {
        ctx.asDynamic().filter = "none"
    }

    fun applyVignette(
        ctx: CanvasRenderingContext2D,
        width: Double,
        height: Double,
        intensity: Float = 0.5f,
    ) {
        val gradient = ctx.createRadialGradient(width / 2, height / 2, 0.0, width / 2, height / 2, width / 2)
        gradient.addColorStop(0.0, "rgba(0, 0, 0, 0)")
        gradient.addColorStop(1.0, "rgba(0, 0, 0, $intensity)")

        ctx.save()
        ctx.fillStyle = gradient
        ctx.fillRect(0.0, 0.0, width, height)
        ctx.restore()
    }

    fun applyNoise(
        ctx: CanvasRenderingContext2D,
        width: Double,
        height: Double,
        intensity: Float = 0.1f,
    ) {
        val imageData = ctx.getImageData(0.0, 0.0, width, height)
        val data = imageData.data

        for (i in 0 until data.length step 4) {
            val noise = (kotlin.random.Random.nextFloat() - 0.5f) * 2 * intensity * 255
            data.asDynamic()[i] = (data.asDynamic()[i] + noise).toInt().coerceIn(0, 255)
            data.asDynamic()[i + 1] = (data.asDynamic()[i + 1] + noise).toInt().coerceIn(0, 255)
            data.asDynamic()[i + 2] = (data.asDynamic()[i + 2] + noise).toInt().coerceIn(0, 255)
        }

        ctx.putImageData(imageData, 0.0, 0.0)
    }

    fun applyPixelate(
        ctx: CanvasRenderingContext2D,
        width: Double,
        height: Double,
        pixelSize: Int = 8,
    ) {
        val tempCanvas = kotlinx.browser.document.createElement("canvas") as org.w3c.dom.HTMLCanvasElement
        tempCanvas.width = width.toInt()
        tempCanvas.height = height.toInt()
        val tempCtx = tempCanvas.getContext("2d") as CanvasRenderingContext2D

        tempCtx.drawImage(ctx.canvas, 0.0, 0.0)

        ctx.asDynamic().imageSmoothingEnabled = false
        val scaledWidth = (width / pixelSize).toInt()
        val scaledHeight = (height / pixelSize).toInt()

        ctx.drawImage(tempCanvas, 0.0, 0.0, width, height, 0.0, 0.0, scaledWidth.toDouble(), scaledHeight.toDouble())
        ctx.drawImage(
            ctx.canvas,
            0.0,
            0.0,
            scaledWidth.toDouble(),
            scaledHeight.toDouble(),
            0.0,
            0.0,
            width,
            height,
        )
        ctx.asDynamic().imageSmoothingEnabled = true
    }
}

class PostProcessing {
    private val effects = mutableListOf<(CanvasRenderingContext2D, Double, Double) -> Unit>()

    fun addEffect(effect: (CanvasRenderingContext2D, Double, Double) -> Unit) {
        effects.add(effect)
    }

    fun removeEffect(effect: (CanvasRenderingContext2D, Double, Double) -> Unit) {
        effects.remove(effect)
    }

    fun clearEffects() {
        effects.clear()
    }

    fun apply(
        ctx: CanvasRenderingContext2D,
        width: Double,
        height: Double,
    ) {
        effects.forEach { it(ctx, width, height) }
    }
}
