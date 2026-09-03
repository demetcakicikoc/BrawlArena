package com.example.game.model

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class ProjectileType {
    BOLT_BULLET,
    BOLT_SUPER_BULLET,
    SHELIA_PELLET,
    SHELIA_SUPER_PELLET,
    EL_GRANDE_PUNCH,
    SPICA_BOMB,
    SPICA_NEEDLE,
    SPICA_SUPER_BOMB
}

data class Projectile(
    val id: String = UUID.randomUUID().toString(),
    val type: ProjectileType,
    val ownerId: String,
    val ownerTeam: Team,
    var position: Vector2D,
    val velocity: Vector2D,
    val damage: Int,
    val maxRange: Float,
    val radius: Float = 14f,
    val penetratesWalls: Boolean = false,
    val penetratesEnemies: Boolean = false,
    val destroysWalls: Boolean = false,
    val knockbackPower: Float = 0f,
    val superChargeAmount: Float = 0.12f,
    val color: Color = Color.Yellow,
    var distanceTraveled: Float = 0f,
    var isDead: Boolean = false,
    val hitBrawlerIds: MutableSet<String> = mutableSetOf()
) {
    fun update(dt: Float) {
        val step = velocity * dt
        position += step
        distanceTraveled += step.length()
        if (distanceTraveled >= maxRange) {
            isDead = true
        }
    }
}
