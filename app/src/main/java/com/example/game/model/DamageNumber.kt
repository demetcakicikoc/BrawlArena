package com.example.game.model

import androidx.compose.ui.graphics.Color

data class DamageNumber(
    val id: Long,
    var position: Vector2D,
    val text: String,
    val color: Color,
    var alpha: Float = 1f,
    var lifetime: Float = 0.85f,
    val maxLifetime: Float = 0.85f
) {
    fun update(dt: Float): Boolean {
        lifetime -= dt
        position = Vector2D(position.x, position.y - 45f * dt)
        alpha = (lifetime / maxLifetime).coerceIn(0f, 1f)
        return lifetime > 0f
    }
}
