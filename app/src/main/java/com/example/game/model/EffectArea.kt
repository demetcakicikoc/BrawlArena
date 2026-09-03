package com.example.game.model

import androidx.compose.ui.graphics.Color

data class EffectArea(
    val id: String,
    val ownerId: String,
    val ownerTeam: Team,
    val center: Vector2D,
    val radius: Float,
    var duration: Float,
    val maxDuration: Float,
    val damagePerSecond: Int = 0,
    val slowFactor: Float = 1f, // e.g. 0.5f means 50% slow
    val color: Color,
    val isThornPatch: Boolean = false,
    var tickTimer: Float = 0f
) {
    fun update(dt: Float): Boolean {
        duration -= dt
        tickTimer += dt
        return duration > 0f
    }
}
