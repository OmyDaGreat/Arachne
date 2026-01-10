package xyz.malefic.arachne.graphics

import xyz.malefic.arachne.math.Rectangle

data class Tile(
    val id: Int,
    val sourceRect: Rectangle,
    val properties: Map<String, String> = emptyMap(),
) {
    val isSolid: Boolean
        get() = properties["solid"]?.toBoolean() ?: false
}

data class Tileset(
    val texture: String,
    val tileWidth: Int,
    val tileHeight: Int,
    val columns: Int,
    val tileCount: Int,
) {
    private val tiles = mutableMapOf<Int, Tile>()

    init {
        // Generate tiles from tileset
        for (id in 0 until tileCount) {
            val x = (id % columns) * tileWidth
            val y = (id / columns) * tileHeight
            tiles[id] =
                Tile(
                    id = id,
                    sourceRect =
                        Rectangle(
                            x.toFloat(),
                            y.toFloat(),
                            tileWidth.toFloat(),
                            tileHeight.toFloat(),
                        ),
                )
        }
    }

    fun getTile(id: Int) = tiles[id]

    fun setTileProperty(
        id: Int,
        key: String,
        value: String,
    ) {
        tiles[id]?.let {
            tiles[id] = it.copy(properties = it.properties + (key to value))
        }
    }
}
