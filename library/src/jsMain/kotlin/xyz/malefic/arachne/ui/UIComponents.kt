package xyz.malefic.arachne.ui

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.math.Rectangle
import xyz.malefic.arachne.math.Vector2

data class UIElement(
    var bounds: Rectangle,
    var visible: Boolean = true,
    var enabled: Boolean = true,
    var onClick: (() -> Unit)? = null,
) {
    fun contains(point: Vector2) = bounds.contains(point)

    fun render(ctx: CanvasRenderingContext2D) {
        // Base rendering - override in subclasses
    }
}

class HealthBar(
    position: Vector2,
    val width: Float,
    val height: Float,
    var current: Float,
    var max: Float,
    var foregroundColor: String = "#00ff00",
    var backgroundColor: String = "#ff0000",
    var borderColor: String = "#ffffff",
    var showText: Boolean = true,
) {
    val bounds = Rectangle(position.x, position.y, width, height)

    fun render(ctx: CanvasRenderingContext2D) {
        // Background
        ctx.fillStyle = backgroundColor
        ctx.fillRect(
            bounds.x.toDouble(),
            bounds.y.toDouble(),
            bounds.width.toDouble(),
            bounds.height.toDouble(),
        )

        // Foreground
        val fillWidth = (current / max) * width
        ctx.fillStyle = foregroundColor
        ctx.fillRect(
            bounds.x.toDouble(),
            bounds.y.toDouble(),
            fillWidth.toDouble(),
            bounds.height.toDouble(),
        )

        // Border
        ctx.strokeStyle = borderColor
        ctx.lineWidth = 2.0
        ctx.strokeRect(
            bounds.x.toDouble(),
            bounds.y.toDouble(),
            bounds.width.toDouble(),
            bounds.height.toDouble(),
        )

        // Text
        if (showText) {
            ctx.fillStyle = "#ffffff"
            ctx.font = "12px monospace"
            ctx.asDynamic().textAlign = "center"
            ctx.asDynamic().textBaseline = "middle"
            val text = "${current.toInt()}/${max.toInt()}"
            ctx.fillText(
                text,
                (bounds.x + bounds.width / 2).toDouble(),
                (bounds.y + bounds.height / 2).toDouble(),
            )
        }
    }

    fun update(
        newCurrent: Float,
        newMax: Float = max,
    ) {
        current = newCurrent.coerceIn(0f, newMax)
        max = newMax
    }
}

class ProgressBar(
    position: Vector2,
    val width: Float,
    val height: Float,
    var progress: Float = 0f,
    var color: String = "#4ecdc4",
    var backgroundColor: String = "#2a2a2a",
) {
    val bounds = Rectangle(position.x, position.y, width, height)

    fun render(ctx: CanvasRenderingContext2D) {
        // Background
        ctx.fillStyle = backgroundColor
        ctx.fillRect(
            bounds.x.toDouble(),
            bounds.y.toDouble(),
            bounds.width.toDouble(),
            bounds.height.toDouble(),
        )

        // Progress
        val fillWidth = progress.coerceIn(0f, 1f) * width
        ctx.fillStyle = color
        ctx.fillRect(
            bounds.x.toDouble(),
            bounds.y.toDouble(),
            fillWidth.toDouble(),
            bounds.height.toDouble(),
        )
    }

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 1f)
    }
}

data class Item(
    val id: String,
    val name: String,
    val description: String = "",
    val icon: String? = null,
    val stackable: Boolean = false,
    var quantity: Int = 1,
    val properties: Map<String, Any> = emptyMap(),
)

class Inventory(
    val capacity: Int = 20,
) {
    private val items = mutableListOf<Item>()

    fun addItem(item: Item): Boolean {
        if (item.stackable) {
            val existing = items.find { it.id == item.id }
            if (existing != null) {
                existing.quantity += item.quantity
                return true
            }
        }

        if (items.size < capacity) {
            items.add(item)
            return true
        }

        return false
    }

    fun removeItem(
        itemId: String,
        quantity: Int = 1,
    ): Boolean {
        val item = items.find { it.id == itemId } ?: return false

        if (item.stackable && item.quantity > quantity) {
            item.quantity -= quantity
            return true
        } else {
            items.remove(item)
            return true
        }
    }

    fun getItem(itemId: String) = items.find { it.id == itemId }

    fun hasItem(
        itemId: String,
        quantity: Int = 1,
    ): Boolean {
        val item = items.find { it.id == itemId } ?: return false
        return item.quantity >= quantity
    }

    fun getItems() = items.toList()

    fun clear() = items.clear()

    fun isFull() = items.size >= capacity
}
