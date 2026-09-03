package com.example.game.model

import androidx.compose.ui.graphics.Color

sealed class Obstacle(
    val id: String,
    val bounds: Rect2D
) {
    class Wall(
        id: String,
        bounds: Rect2D,
        val isDestructible: Boolean = true,
        var currentHp: Int = 1000
    ) : Obstacle(id, bounds)

    class Bush(
        id: String,
        bounds: Rect2D
    ) : Obstacle(id, bounds)

    class GemSpawner(
        id: String,
        bounds: Rect2D,
        var spawnTimer: Float = 0f,
        val spawnInterval: Float = 5.5f
    ) : Obstacle(id, bounds)
}

data class Rect2D(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerX: Float get() = (left + right) / 2f
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(point: Vector2D): Boolean {
        return point.x in left..right && point.y in top..bottom
    }

    fun intersectsCircle(center: Vector2D, radius: Float): Boolean {
        val closestX = center.x.coerceIn(left, right)
        val closestY = center.y.coerceIn(top, bottom)
        val dx = center.x - closestX
        val dy = center.y - closestY
        return (dx * dx + dy * dy) <= (radius * radius)
    }
}
