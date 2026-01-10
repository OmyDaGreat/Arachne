package xyz.malefic.arachne.ai

import xyz.malefic.arachne.math.Vector2
import kotlin.math.abs
import kotlin.math.sqrt

data class PathNode(
    val x: Int,
    val y: Int,
    var g: Float = Float.MAX_VALUE,
    var h: Float = 0f,
    var parent: PathNode? = null,
) {
    val f: Float get() = g + h

    fun reset() {
        g = Float.MAX_VALUE
        h = 0f
        parent = null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathNode) return false
        return x == other.x && y == other.y
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

class PathfindingGrid(
    val width: Int,
    val height: Int,
) {
    private val nodes = Array(height) { y -> Array(width) { x -> PathNode(x, y) } }
    private val walkable = Array(height) { BooleanArray(width) { true } }

    fun setWalkable(
        x: Int,
        y: Int,
        walkable: Boolean,
    ) {
        if (isInBounds(x, y)) {
            this.walkable[y][x] = walkable
        }
    }

    fun isWalkable(
        x: Int,
        y: Int,
    ) = isInBounds(x, y) && walkable[y][x]

    fun isInBounds(
        x: Int,
        y: Int,
    ) = x >= 0 && x < width && y >= 0 && y < height

    fun getNode(
        x: Int,
        y: Int,
    ) = if (isInBounds(x, y)) nodes[y][x] else null

    fun reset() {
        for (y in 0 until height) {
            for (x in 0 until width) {
                nodes[y][x].reset()
            }
        }
    }

    fun setRegion(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
        walkable: Boolean,
    ) {
        for (y in startY..endY) {
            for (x in startX..endX) {
                setWalkable(x, y, walkable)
            }
        }
    }
}

enum class Heuristic {
    MANHATTAN,
    EUCLIDEAN,
    DIAGONAL,
}

class AStarPathfinder(
    private val grid: PathfindingGrid,
    private val allowDiagonal: Boolean = false,
    private val heuristic: Heuristic = Heuristic.MANHATTAN,
) {
    private val openSet = mutableListOf<PathNode>()
    private val closedSet = mutableSetOf<PathNode>()

    fun findPath(
        startX: Int,
        startY: Int,
        endX: Int,
        endY: Int,
    ): List<Vector2>? {
        grid.reset()
        openSet.clear()
        closedSet.clear()

        val startNode = grid.getNode(startX, startY) ?: return null
        val endNode = grid.getNode(endX, endY) ?: return null

        if (!grid.isWalkable(endX, endY)) return null

        startNode.g = 0f
        startNode.h = calculateHeuristic(startNode, endNode)
        openSet.add(startNode)

        while (openSet.isNotEmpty()) {
            val current = openSet.minByOrNull { it.f } ?: break
            openSet.remove(current)

            if (current == endNode) {
                return reconstructPath(endNode)
            }

            closedSet.add(current)

            for (neighbor in getNeighbors(current)) {
                if (closedSet.contains(neighbor)) continue
                if (!grid.isWalkable(neighbor.x, neighbor.y)) continue

                val tentativeG = current.g + getDistance(current, neighbor)

                if (tentativeG < neighbor.g) {
                    neighbor.parent = current
                    neighbor.g = tentativeG
                    neighbor.h = calculateHeuristic(neighbor, endNode)

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor)
                    }
                }
            }
        }

        return null
    }

    private fun getNeighbors(node: PathNode): List<PathNode> {
        val neighbors = mutableListOf<PathNode>()
        val directions =
            if (allowDiagonal) {
                listOf(
                    -1 to 0,
                    1 to 0,
                    0 to -1,
                    0 to 1, // Cardinal
                    -1 to -1,
                    -1 to 1,
                    1 to -1,
                    1 to 1, // Diagonal
                )
            } else {
                listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
            }

        for ((dx, dy) in directions) {
            val x = node.x + dx
            val y = node.y + dy
            grid.getNode(x, y)?.let { neighbors.add(it) }
        }

        return neighbors
    }

    private fun calculateHeuristic(
        from: PathNode,
        to: PathNode,
    ): Float {
        val dx = abs(from.x - to.x).toFloat()
        val dy = abs(from.y - to.y).toFloat()

        return when (heuristic) {
            Heuristic.MANHATTAN -> {
                dx + dy
            }

            Heuristic.EUCLIDEAN -> {
                sqrt(dx * dx + dy * dy)
            }

            Heuristic.DIAGONAL -> {
                val diagonal = minOf(dx, dy)
                val straight = maxOf(dx, dy) - diagonal
                diagonal * 1.4f + straight
            }
        }
    }

    private fun getDistance(
        from: PathNode,
        to: PathNode,
    ): Float {
        val dx = abs(from.x - to.x)
        val dy = abs(from.y - to.y)
        return if (dx == 1 && dy == 1) 1.4f else 1f
    }

    private fun reconstructPath(endNode: PathNode): List<Vector2> {
        val path = mutableListOf<Vector2>()
        var current: PathNode? = endNode

        while (current != null) {
            path.add(0, Vector2(current.x.toFloat(), current.y.toFloat()))
            current = current.parent
        }

        return path
    }

    fun smoothPath(path: List<Vector2>): List<Vector2> {
        if (path.size <= 2) return path

        val smoothed = mutableListOf(path.first())
        var current = 0

        while (current < path.size - 1) {
            var farthest = current + 1

            for (i in current + 2 until path.size) {
                if (hasLineOfSight(path[current], path[i])) {
                    farthest = i
                } else {
                    break
                }
            }

            smoothed.add(path[farthest])
            current = farthest
        }

        return smoothed
    }

    private fun hasLineOfSight(
        from: Vector2,
        to: Vector2,
    ): Boolean {
        var x0 = from.x.toInt()
        var y0 = from.y.toInt()
        val x1 = to.x.toInt()
        val y1 = to.y.toInt()

        val dx = abs(x1 - x0)
        val dy = abs(y1 - y0)
        val sx = if (x0 < x1) 1 else -1
        val sy = if (y0 < y1) 1 else -1
        var err = dx - dy

        while (true) {
            if (!grid.isWalkable(x0, y0)) return false
            if (x0 == x1 && y0 == y1) break

            val e2 = 2 * err
            if (e2 > -dy) {
                err -= dy
                x0 += sx
            }
            if (e2 < dx) {
                err += dx
                y0 += sy
            }
        }

        return true
    }
}
