package xyz.malefic.arachne.graphics

import org.w3c.dom.CanvasRenderingContext2D
import xyz.malefic.arachne.assets.AssetManager
import xyz.malefic.arachne.core.Component
import xyz.malefic.arachne.math.Rectangle
import xyz.malefic.arachne.math.Vector2

data class TilemapLayer(
    val name: String,
    val tiles: Array<IntArray>,
    var visible: Boolean = true,
    var alpha: Float = 1f,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TilemapLayer) return false
        if (name != other.name) return false
        if (!tiles.contentDeepEquals(other.tiles)) return false
        if (visible != other.visible) return false
        if (alpha != other.alpha) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + tiles.contentDeepHashCode()
        result = 31 * result + visible.hashCode()
        result = 31 * result + alpha.hashCode()
        return result
    }
}

data class Tilemap(
    val width: Int,
    val height: Int,
    val tileWidth: Int,
    val tileHeight: Int,
    val tileset: Tileset,
    val layers: MutableList<TilemapLayer> = mutableListOf(),
) : Component {
    fun getTileAt(
        x: Int,
        y: Int,
        layerIndex: Int = 0,
    ): Int {
        if (x !in 0..<width || y < 0 || y >= height) return -1
        if (layerIndex < 0 || layerIndex >= layers.size) return -1
        return layers[layerIndex].tiles[y][x]
    }

    fun setTileAt(
        x: Int,
        y: Int,
        tileId: Int,
        layerIndex: Int = 0,
    ) {
        if (x !in 0..<width || y < 0 || y >= height) return
        if (layerIndex < 0 || layerIndex >= layers.size) return
        layers[layerIndex].tiles[y][x] = tileId
    }

    fun worldToTile(position: Vector2): Pair<Int, Int> {
        val tileX = (position.x / tileWidth).toInt()
        val tileY = (position.y / tileHeight).toInt()
        return Pair(tileX, tileY)
    }

    fun tileToWorld(
        x: Int,
        y: Int,
    ): Vector2 =
        Vector2(
            x * tileWidth.toFloat() + tileWidth / 2f,
            y * tileHeight.toFloat() + tileHeight / 2f,
        )

    fun isTileSolid(
        x: Int,
        y: Int,
        layerIndex: Int = 0,
    ): Boolean {
        val tileId = getTileAt(x, y, layerIndex)
        if (tileId < 0) return false
        return tileset.getTile(tileId)?.isSolid ?: false
    }

    fun getTileBounds(
        x: Int,
        y: Int,
    ): Rectangle =
        Rectangle(
            x * tileWidth.toFloat(),
            y * tileHeight.toFloat(),
            tileWidth.toFloat(),
            tileHeight.toFloat(),
        )

    fun render(
        ctx: CanvasRenderingContext2D,
        camera: Camera,
    ) {
        val texture = AssetManager.getTexture(tileset.texture) ?: return

        // Calculate visible tile range
        val startX = ((camera.bounds.x / tileWidth).toInt() - 1).coerceAtLeast(0)
        val startY = ((camera.bounds.y / tileHeight).toInt() - 1).coerceAtLeast(0)
        val endX = ((camera.bounds.right / tileWidth).toInt() + 1).coerceAtMost(width - 1)
        val endY = ((camera.bounds.bottom / tileHeight).toInt() + 1).coerceAtMost(height - 1)

        layers.forEach { layer ->
            if (!layer.visible) return@forEach

            ctx.save()
            ctx.globalAlpha = layer.alpha.toDouble()

            for (y in startY..endY) {
                for (x in startX..endX) {
                    val tileId = layer.tiles[y][x]
                    if (tileId < 0) continue

                    val tile = tileset.getTile(tileId) ?: continue
                    val destX = x * tileWidth.toFloat()
                    val destY = y * tileHeight.toFloat()

                    ctx.drawImage(
                        texture,
                        tile.sourceRect.x.toDouble(),
                        tile.sourceRect.y.toDouble(),
                        tile.sourceRect.width.toDouble(),
                        tile.sourceRect.height.toDouble(),
                        destX.toDouble(),
                        destY.toDouble(),
                        tileWidth.toDouble(),
                        tileHeight.toDouble(),
                    )
                }
            }

            ctx.restore()
        }
    }

    companion object {
        fun createEmpty(
            width: Int,
            height: Int,
            tileWidth: Int,
            tileHeight: Int,
            tileset: Tileset,
            layerCount: Int = 1,
        ): Tilemap {
            val layers = mutableListOf<TilemapLayer>()
            repeat(layerCount) { index ->
                val tiles = Array(height) { IntArray(width) { -1 } }
                layers.add(TilemapLayer("Layer $index", tiles))
            }
            return Tilemap(width, height, tileWidth, tileHeight, tileset, layers)
        }
    }
}
